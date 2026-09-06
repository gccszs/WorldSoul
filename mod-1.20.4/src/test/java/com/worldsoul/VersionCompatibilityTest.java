package com.worldsoul;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Version compatibility helpers used by CI and docs.
 */
public class VersionCompatibilityTest {

    @Test
    public void testModBasicInfo() {
        assertTrue("worldsoul".equals(WorldSoulMod.MOD_ID));
    }

    @Test
    public void testVersionRange() {
        String versionRange = ">=1.20.1 <=1.20.6";
        assertTrue(isVersionSupported("1.20.1", versionRange));
        assertTrue(isVersionSupported("1.20.4", versionRange));
        assertTrue(isVersionSupported("1.20.6", versionRange));
        assertFalse(isVersionSupported("1.19.4", versionRange));
        assertFalse(isVersionSupported("1.21", versionRange));
    }

    private boolean isVersionSupported(String version, String versionRange) {
        String[] tokens = versionRange.replace(">=", " ").replace("<=", " ").trim().split("\\s+");
        String minVersion = tokens[0];
        String maxVersion = tokens[1];
        return compareVersions(version, minVersion) >= 0 && compareVersions(version, maxVersion) <= 0;
    }

    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (num1 != num2) {
                return num1 - num2;
            }
        }
        return 0;
    }
}
