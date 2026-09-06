package com.worldsoul.harness;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AgentModeTest {
    @Test
    public void acceptsExactlyTheThreeWireModes() {
        assertTrue(AgentMode.isSupported("guide"));
        assertTrue(AgentMode.isSupported("deity"));
        assertTrue(AgentMode.isSupported("troll"));
        assertFalse(AgentMode.isSupported("creative"));
        assertFalse(AgentMode.isSupported("GUIDE"));
    }
}
