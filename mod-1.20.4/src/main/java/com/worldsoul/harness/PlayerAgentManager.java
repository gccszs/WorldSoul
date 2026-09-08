package com.worldsoul.harness;

import com.mojang.brigadier.ParseResults;
import com.worldsoul.WorldSoulMod;
import com.worldsoul.command.CommandPolicy;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.WorldSavePath;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Connects one save-global Harness Agent and isolated online player child Agents. */
public final class PlayerAgentManager {
    private static final int MAX_REGENERATION_ATTEMPTS = 2;
    private final HarnessClient client;
    private final Map<UUID, PlayerState> players = new ConcurrentHashMap<>();
    private final Map<String, String> preferredModes = new ConcurrentHashMap<>();
    private final Map<MinecraftServer, String> worlds = new ConcurrentHashMap<>();

    public PlayerAgentManager(HarnessClient client) { this.client = client; }

    public void onPlayerJoin(ServerPlayerEntity player, MinecraftServer server) {
        if (!client.getSettings().isEnabled() || players.containsKey(player.getUuid())) return;
        String worldId = worlds.computeIfAbsent(server, PlayerAgentManager::worldId);
        String mode = preferredModes.getOrDefault(modeKey(worldId, player.getUuid()),
                player.isCreative() ? "deity" : "guide");
        client.ensureHealthy()
                .thenCompose(ignored -> client.playerOnline(
                        worldId, player.getUuidAsString(), player.getName().getString(), mode, snapshot(player)))
                .whenComplete((usage, error) -> server.execute(() -> {
                    if (error != null) {
                        WorldSoulMod.LOGGER.error("Failed to connect WorldSoul player Agent for {}",
                                player.getName().getString(), error);
                        player.sendMessage(Text.literal("[WorldSoul] Harness 暂不可用。")
                                .formatted(Formatting.RED), false);
                        return;
                    }
                    PlayerState state = new PlayerState(worldId, mode);
                    players.put(player.getUuid(), state);
                    player.sendMessage(Text.literal("[WorldSoul] 伴生灵已就绪，使用 @AI 开始对话。")
                            .formatted(Formatting.AQUA), false);
                    sendUsageWarning(player, state, usage);
                }));
    }

    public void onPlayerLeave(ServerPlayerEntity player) {
        PlayerState state = players.remove(player.getUuid());
        if (state == null) return;
        client.playerOffline(state.worldId, player.getUuidAsString()).whenComplete((ignored, error) -> {
            if (error != null) WorldSoulMod.LOGGER.warn("Failed to stop WorldSoul player Agent", error);
        });
    }

    public void onServerStopping(MinecraftServer server) {
        String worldId = worlds.remove(server);
        if (worldId == null) return;
        players.entrySet().removeIf(entry -> entry.getValue().worldId.equals(worldId));
        preferredModes.keySet().removeIf(key -> key.startsWith(worldId + "\0"));
        client.worldOffline(worldId).whenComplete((ignored, error) -> {
            if (error != null) WorldSoulMod.LOGGER.warn("Failed to stop WorldSoul save Agent {}", worldId, error);
        });
    }

    public void chat(ServerPlayerEntity player, String message) {
        PlayerState state = players.get(player.getUuid());
        MinecraftServer server = player.getServer();
        if (state == null) {
            player.sendMessage(Text.literal("[WorldSoul] Agent 尚未就绪，请稍后再试。")
                    .formatted(Formatting.YELLOW), false);
            return;
        }
        if (state.disabled) {
            player.sendMessage(Text.literal("[WorldSoul] 该 Agent 已达到 Token 用量上限。")
                    .formatted(Formatting.RED), false);
            return;
        }
        player.sendMessage(Text.literal("[WorldSoul] 思考中…").formatted(Formatting.GRAY), false);
        client.chat(state.worldId, player.getUuidAsString(), message, snapshot(player)).whenComplete((result, error) ->
                server.execute(() -> {
                    if (error != null) {
                        WorldSoulMod.LOGGER.error("WorldSoul chat failed for {}", player.getName().getString(), error);
                        player.sendMessage(Text.literal("[WorldSoul] 对话暂时失败，请查看服务日志。")
                                .formatted(Formatting.RED), false);
                        return;
                    }
                    handleReply(player, state, result, 0);
                }));
    }

    public void voice(ServerPlayerEntity player, byte[] waveAudio) {
        PlayerState state = players.get(player.getUuid());
        if (state == null || state.disabled) return;
        MinecraftServer server = player.getServer();
        PlayerSnapshot playerSnapshot = snapshot(player);
        player.sendMessage(Text.literal("[WorldSoul] 正在识别语音…").formatted(Formatting.GRAY), false);
        client.updatePlayerState(state.worldId, player.getUuidAsString(), playerSnapshot)
                .thenCompose(ignored -> client.transcribeVoice(state.worldId, player.getUuidAsString(), waveAudio))
                .whenComplete((transcript, error) ->
                server.execute(() -> {
                    if (error != null) {
                        WorldSoulMod.LOGGER.error("WorldSoul voice request failed for {}",
                                player.getName().getString(), error);
                        player.sendMessage(Text.literal("[WorldSoul] 语音识别暂时失败。")
                                .formatted(Formatting.RED), false);
                        return;
                    }
                    player.sendMessage(Text.literal("[WorldSoul] 你说：").formatted(Formatting.DARK_AQUA)
                            .append(Text.literal(transcript).formatted(Formatting.WHITE)), false);
                    player.sendMessage(Text.literal("[WorldSoul] 思考中…").formatted(Formatting.GRAY), false);
                    client.chat(state.worldId, player.getUuidAsString(), transcript, playerSnapshot)
                            .whenComplete((result, chatError) -> server.execute(() -> {
                                if (chatError != null) {
                                    WorldSoulMod.LOGGER.error("WorldSoul voice Agent request failed for {}",
                                            player.getName().getString(), chatError);
                                    player.sendMessage(Text.literal("[WorldSoul] 对话暂时失败，请查看服务日志。")
                                            .formatted(Formatting.RED), false);
                                    return;
                                }
                                handleReply(player, state, result, 0);
                            }));
                }));
    }

    public void setMode(ServerPlayerEntity player, String mode) {
        if (!AgentMode.isSupported(mode)) {
            player.sendMessage(Text.literal("[WorldSoul] 模式必须是 guide、deity 或 troll。")
                    .formatted(Formatting.RED), false);
            return;
        }
        PlayerState state = players.get(player.getUuid());
        if (state == null) {
            player.sendMessage(Text.literal("[WorldSoul] Agent 尚未就绪。")
                    .formatted(Formatting.YELLOW), false);
            return;
        }
        client.setMode(state.worldId, player.getUuidAsString(), mode).whenComplete((usage, error) ->
                player.getServer().execute(() -> {
                    if (error != null) {
                        WorldSoulMod.LOGGER.error("WorldSoul mode switch failed for {}",
                                player.getName().getString(), error);
                        player.sendMessage(Text.literal("[WorldSoul] 模式切换失败。")
                                .formatted(Formatting.RED), false);
                        return;
                    }
                    state.mode = mode;
                    preferredModes.put(modeKey(state.worldId, player.getUuid()), mode);
                    sendUsageWarning(player, state, usage);
                    player.sendMessage(Text.literal("[WorldSoul] Agent 模式已切换为 " + mode + "。")
                            .formatted(Formatting.AQUA), false);
                }));
    }

    private void handleReply(
            ServerPlayerEntity player, PlayerState state, HarnessClient.ChatResult reply, int regenerationAttempt) {
        sendUsageWarning(player, state, reply.getUsage());
        List<HarnessClient.CommandProposal> commands = reply.getCommands();
        if (commands.isEmpty()) {
            sendSpeech(player, reply.getSpeech());
            return;
        }
        processCommand(player, state, commands, 0, reply.getSpeech(), regenerationAttempt);
    }

    private void processCommand(
            ServerPlayerEntity player,
            PlayerState state,
            List<HarnessClient.CommandProposal> commands,
            int index,
            String completionSpeech,
            int regenerationAttempt) {
        if (index >= commands.size()) {
            sendSpeech(player, completionSpeech);
            return;
        }
        HarnessClient.CommandProposal proposal = commands.get(index);
        CommandExecution execution = validateAndExecute(player, state, proposal.getCommand());
        if (!execution.accepted) {
            WorldSoulMod.LOGGER.warn("Rejected private WorldSoul command proposal {}: {}",
                    proposal.getId(), execution.error);
        }
        client.commandResult(
                state.worldId, player.getUuidAsString(), proposal.getId(), execution.accepted, execution.error)
                .whenComplete((feedback, error) -> player.getServer().execute(() -> {
                    if (error != null) {
                        WorldSoulMod.LOGGER.error("Failed to return private command validation result", error);
                        return;
                    }
                    sendUsageWarning(player, state, feedback.getUsage());
                    if (!execution.accepted) {
                        if (regenerationAttempt >= MAX_REGENERATION_ATTEMPTS) {
                            WorldSoulMod.LOGGER.warn("WorldSoul command regeneration limit reached for {}",
                                    player.getName().getString());
                            return;
                        }
                        handleReply(player, state, feedback, regenerationAttempt + 1);
                        return;
                    }
                    processCommand(player, state, commands, index + 1, completionSpeech, regenerationAttempt);
                }));
    }

    private static CommandExecution validateAndExecute(
            ServerPlayerEntity player, PlayerState state, String command) {
        CommandPolicy.Validation policy = CommandPolicy.validate(command, "deity".equals(state.mode));
        if (!policy.isAccepted()) return CommandExecution.reject(policy.getError());
        MinecraftServer server = player.getServer();
        ServerCommandSource source = player.getCommandSource().withLevel(2).withSilent();
        ParseResults<ServerCommandSource> parsed = server.getCommandManager().getDispatcher().parse(command, source);
        if (parsed.getReader().canRead() || !parsed.getExceptions().isEmpty()) {
            return CommandExecution.reject("Brigadier could not parse the complete command at cursor "
                    + parsed.getReader().getCursor());
        }
        try {
            server.getCommandManager().executeWithPrefix(source, command);
            return CommandExecution.accept();
        } catch (RuntimeException error) {
            String detail = error.getMessage();
            return CommandExecution.reject(detail == null || detail.isBlank()
                    ? error.getClass().getSimpleName() : detail);
        }
    }

    private static void sendSpeech(ServerPlayerEntity player, String speech) {
        if (speech == null || speech.isBlank()) return;
        player.sendMessage(Text.literal("[WorldSoul] ").formatted(Formatting.AQUA)
                .append(Text.literal(speech).formatted(Formatting.WHITE)), false);
    }

    private static void sendUsageWarning(
            ServerPlayerEntity player, PlayerState state, HarnessClient.UsageStatus usage) {
        if (usage == null || usage.getState().equals(state.lastUsageState)) return;
        state.lastUsageState = usage.getState();
        state.disabled = usage.isDisabled();
        if ("warning".equals(usage.getState())) {
            player.sendMessage(Text.literal("[WorldSoul] Token 用量已接近上限。")
                    .formatted(Formatting.GOLD), false);
        } else if ("exhausted".equals(usage.getState())) {
            player.sendMessage(Text.literal("[WorldSoul] Token 用量已达到上限，该 Agent 已停用。")
                    .formatted(Formatting.RED), false);
        }
    }

    private static String worldId(MinecraftServer server) {
        String path = server.getSavePath(WorldSavePath.ROOT).toAbsolutePath().normalize().toString();
        return UUID.nameUUIDFromBytes(path.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private static PlayerSnapshot snapshot(ServerPlayerEntity player) {
        List<PlayerSnapshot.InventoryStack> inventory = new ArrayList<>();
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (!stack.isEmpty()) inventory.add(inventoryStack(slot, stack));
        }
        List<PlayerSnapshot.EquipmentStack> equipment = new ArrayList<>();
        addEquipment(equipment, "head", player.getEquippedStack(EquipmentSlot.HEAD));
        addEquipment(equipment, "chest", player.getEquippedStack(EquipmentSlot.CHEST));
        addEquipment(equipment, "legs", player.getEquippedStack(EquipmentSlot.LEGS));
        addEquipment(equipment, "feet", player.getEquippedStack(EquipmentSlot.FEET));
        addEquipment(equipment, "mainhand", player.getEquippedStack(EquipmentSlot.MAINHAND));
        addEquipment(equipment, "offhand", player.getEquippedStack(EquipmentSlot.OFFHAND));
        return new PlayerSnapshot(
                player.getWorld().getRegistryKey().getValue().toString(),
                player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch(),
                player.getHorizontalFacing().asString(), player.getHealth(), player.getMaxHealth(),
                player.getHungerManager().getFoodLevel(), player.interactionManager.getGameMode().getName(),
                player.isOnGround(), player.getInventory().selectedSlot, inventory, equipment);
    }

    private static PlayerSnapshot.InventoryStack inventoryStack(int slot, ItemStack stack) {
        return new PlayerSnapshot.InventoryStack(slot, Registries.ITEM.getId(stack.getItem()).toString(),
                stack.getCount(), stack.isDamageable() ? stack.getDamage() : null,
                stack.isDamageable() ? stack.getMaxDamage() : null);
    }

    private static void addEquipment(
            List<PlayerSnapshot.EquipmentStack> equipment, String slot, ItemStack stack) {
        if (stack.isEmpty()) return;
        equipment.add(new PlayerSnapshot.EquipmentStack(slot, Registries.ITEM.getId(stack.getItem()).toString(),
                stack.getCount(), stack.isDamageable() ? stack.getDamage() : null,
                stack.isDamageable() ? stack.getMaxDamage() : null));
    }

    private static String modeKey(String worldId, UUID playerId) { return worldId + "\0" + playerId; }

    private static final class PlayerState {
        private final String worldId;
        private String mode;
        private String lastUsageState = "normal";
        private boolean disabled;

        private PlayerState(String worldId, String mode) { this.worldId = worldId; this.mode = mode; }
    }

    private static final class CommandExecution {
        private final boolean accepted;
        private final String error;

        private CommandExecution(boolean accepted, String error) { this.accepted = accepted; this.error = error; }
        private static CommandExecution accept() { return new CommandExecution(true, null); }
        private static CommandExecution reject(String error) { return new CommandExecution(false, error); }
    }
}
