package com.worldsoul;

import com.worldsoul.voice.VoiceRecorder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-only Fabric entrypoint.
 */
public final class WorldSoulClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("worldsoul-client");
    private static final Identifier VOICE_CHANNEL = new Identifier(WorldSoulMod.MOD_ID, "voice");
    private final VoiceRecorder recorder = new VoiceRecorder();
    private boolean keyWasDown;

    @Override
    public void onInitializeClient() {
        KeyBinding voiceKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.worldsoul.voice", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.worldsoul"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean down = voiceKey.isPressed();
            if (down && !keyWasDown) {
                recorder.start(wave -> client.execute(() -> {
                    if (client.player == null || ClientPlayNetworking.canSend(VOICE_CHANNEL) == false) return;
                    var buffer = PacketByteBufs.create();
                    buffer.writeByteArray(wave);
                    ClientPlayNetworking.send(VOICE_CHANNEL, buffer);
                }), error -> client.execute(() -> {
                    LOGGER.error("Microphone capture failed", error);
                    String detail = error.getMessage() == null || error.getMessage().isBlank()
                            ? "无法访问麦克风。" : error.getMessage();
                    if (client.player != null) client.player.sendMessage(
                            Text.literal("[WorldSoul] " + detail).formatted(Formatting.RED), false);
                }));
            } else if (!down && keyWasDown) {
                recorder.stop();
            }
            keyWasDown = down;
        });
        LOGGER.info("WorldSoul client initialized");
    }
}
