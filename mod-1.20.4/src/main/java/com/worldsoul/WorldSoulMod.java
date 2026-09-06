package com.worldsoul;

import com.worldsoul.chat.AiChatListener;
import com.worldsoul.config.HarnessSettings;
import com.worldsoul.harness.HarnessClient;
import com.worldsoul.harness.PlayerAgentManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server/common Fabric entrypoint.
 */
public final class WorldSoulMod implements ModInitializer {

    public static final String MOD_ID = "worldsoul";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Identifier VOICE_CHANNEL = new Identifier(MOD_ID, "voice");
    private static final int MAX_VOICE_PACKET_BYTES = 600_000;

    private static HarnessClient harnessClient;
    private static PlayerAgentManager playerAgentManager;

    @Override
    public void onInitialize() {
        HarnessSettings settings = HarnessSettings.load();
        harnessClient = new HarnessClient(settings);
        playerAgentManager = new PlayerAgentManager(harnessClient);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            // Smoke-test: always greet on join so a loaded mod is obvious even if harness is down.
            server.execute(() -> player.sendMessage(
                    Text.literal("欢迎使用 WorldSoul！").formatted(Formatting.GREEN),
                    false));
            playerAgentManager.onPlayerJoin(player, server);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                playerAgentManager.onPlayerLeave(handler.getPlayer()));
        ServerLifecycleEvents.SERVER_STOPPING.register(playerAgentManager::onServerStopping);
        ServerPlayNetworking.registerGlobalReceiver(VOICE_CHANNEL, (server, player, handler, buffer, responder) -> {
            byte[] wave;
            try {
                wave = buffer.readByteArray(MAX_VOICE_PACKET_BYTES);
            } catch (RuntimeException error) {
                LOGGER.warn("Rejected oversized or malformed WorldSoul voice packet from {}",
                        player.getName().getString());
                return;
            }
            server.execute(() -> playerAgentManager.voice(player, wave));
        });

        new AiChatListener(playerAgentManager).register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("worldsoul")
                        .then(CommandManager.literal("mode")
                                .then(CommandManager.literal("guide").executes(context ->
                                        switchMode(context.getSource().getPlayerOrThrow(), "guide")))
                                .then(CommandManager.literal("deity").executes(context ->
                                        switchMode(context.getSource().getPlayerOrThrow(), "deity")))
                                .then(CommandManager.literal("troll").executes(context ->
                                        switchMode(context.getSource().getPlayerOrThrow(), "troll"))))));

        LOGGER.info("WorldSoul mod ready (harness={}, url={})",
                settings.isEnabled(), settings.getBaseUrl());
    }

    public static HarnessClient getHarnessClient() {
        return harnessClient;
    }

    public static PlayerAgentManager getPlayerAgentManager() {
        return playerAgentManager;
    }

    private static int switchMode(ServerPlayerEntity player, String mode) {
        playerAgentManager.setMode(player, mode);
        return 1;
    }
}
