package com.worldsoul.agent.protocol;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReplyParserTest {

    @Test
    public void parsesSpeechAndActions() {
        ReplyParser parser = new ReplyParser();
        AgentReply reply = parser.parse(
                "给你一把剑。\n[[action:give item=diamond_sword count=1]]");

        assertEquals("给你一把剑。", reply.getSpeech());
        assertEquals(1, reply.getActions().size());
        assertEquals(ActionType.GIVE, reply.getActions().get(0).getType());
        assertEquals("diamond_sword", reply.getActions().get(0).getArgument("item"));
        assertEquals("1", reply.getActions().get(0).getArgument("count"));
    }

    @Test
    public void handlesEmptyInput() {
        ReplyParser parser = new ReplyParser();
        AgentReply reply = parser.parse("   ");
        assertEquals("", reply.getSpeech());
        assertTrue(reply.getActions().isEmpty());
    }
}
