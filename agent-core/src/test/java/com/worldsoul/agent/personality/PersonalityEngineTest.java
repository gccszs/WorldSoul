package com.worldsoul.agent.personality;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PersonalityEngineTest {
    private final PersonalityEngine engine = new PersonalityEngine();

    @Test
    public void guideBehavesLikePlainTextStrategySearch() {
        String prompt = engine.buildSystemPrompt(GodMode.GUIDE);
        assertTrue(prompt.contains("攻略搜索"));
        assertTrue(prompt.contains("不要代替玩家执行命令"));
        assertTrue(prompt.contains("不使用 Markdown"));
        assertFalse(prompt.contains("```"));
    }

    @Test
    public void deityBehavesLikeValidatedConsole() {
        String prompt = engine.buildSystemPrompt(GodMode.DEITY);
        assertTrue(prompt.contains("受控 Minecraft 控制台"));
        assertTrue(prompt.contains("等待游戏端校验"));
        assertTrue(prompt.contains("不向玩家复述错误详情"));
        assertFalse(prompt.contains("```"));
    }
}
