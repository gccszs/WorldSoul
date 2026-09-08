package com.worldsoul.command;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CommandPolicyTest {
    @Test
    public void allowsGameCommandsOnlyInDeityMode() {
        assertTrue(CommandPolicy.validate("give @s minecraft:stone 1", true).isAccepted());
        assertFalse(CommandPolicy.validate("give @s minecraft:stone 1", false).isAccepted());
    }

    @Test
    public void rejectsAdministrativeInjectionAndMultilineInput() {
        assertFalse(CommandPolicy.validate("op Player", true).isAccepted());
        assertFalse(CommandPolicy.validate("give @s stone\nstop", true).isAccepted());
        assertFalse(CommandPolicy.validate("/give @s stone", true).isAccepted());
    }
}
