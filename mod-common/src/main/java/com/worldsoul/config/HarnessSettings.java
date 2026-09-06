package com.worldsoul.config;

/**
 * Harness connection settings for the Minecraft mod.
 */
public final class HarnessSettings {

    private static final String DEFAULT_URL = "http://127.0.0.1:8787/worldsoul";

    private final String baseUrl;
    private final boolean enabled;

    public HarnessSettings(String baseUrl, boolean enabled) {
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.enabled = enabled;
    }

    public static HarnessSettings load() {
        String url = firstNonBlank(
                System.getenv("WORLDSOUL_HARNESS_URL"),
                System.getProperty("worldsoul.harness.url"),
                DEFAULT_URL);
        boolean enabled = !"false".equalsIgnoreCase(System.getenv("WORLDSOUL_HARNESS_ENABLED"))
                && !"false".equalsIgnoreCase(System.getProperty("worldsoul.harness.enabled"));
        return new HarnessSettings(url, enabled);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return DEFAULT_URL;
    }

    private static String trimTrailingSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
}
