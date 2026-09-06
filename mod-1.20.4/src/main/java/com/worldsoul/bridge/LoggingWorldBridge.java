package com.worldsoul.bridge;

import com.worldsoul.WorldSoulMod;
import com.worldsoul.agent.protocol.AgentAction;
import com.worldsoul.agent.world.WorldBridge;

/**
 * Temporary bridge that logs actions until Minecraft world ops are implemented.
 */
public final class LoggingWorldBridge implements WorldBridge {

    @Override
    public String execute(AgentAction action) {
        String message = "deferred world action: " + action;
        WorldSoulMod.LOGGER.info(message);
        return message;
    }
}
