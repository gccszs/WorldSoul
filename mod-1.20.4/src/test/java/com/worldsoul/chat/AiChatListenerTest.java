package com.worldsoul.chat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AiChatListenerTest {

    @Test
    public void detectsAiPrefixCaseInsensitive() {
        assertTrue(AiChatListener.isAiMessage("@AI 你好"));
        assertTrue(AiChatListener.isAiMessage("  @ai 钻石怎么找"));
        assertFalse(AiChatListener.isAiMessage("WorldSoul, 你好"));
        assertFalse(AiChatListener.isAiMessage("@AI"));
    }

    @Test
    public void extractsMessageAfterPrefix() {
        assertEquals("你好", AiChatListener.extractMessage("@AI 你好"));
        assertEquals("give me diamond", AiChatListener.extractMessage("  @ai   give me diamond"));
    }
}
