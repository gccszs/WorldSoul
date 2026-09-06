package com.worldsoul.agent.world;

import com.worldsoul.agent.protocol.AgentAction;

/**
 * Executes agent actions against a world implementation.
 * Mod provides Minecraft; harness provides mocks.
 */
public interface WorldBridge {

    /**
     * Executes one whitelisted action and returns a human-readable result.
     */
    String execute(AgentAction action);
}
