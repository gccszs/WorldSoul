package com.worldsoul.agent.config;

import com.worldsoul.agent.personality.GodMode;

/**
 * Runtime configuration shared by harness and mod.
 */
public final class AgentConfig {

    private String provider = "echo";
    private String apiKey = "";
    private String baseUrl = "https://api.deepseek.com";
    private String model = "deepseek-chat";
    private GodMode mode = GodMode.GUIDE;
    private int maxHistory = 20;
    private int dailyTokenBudget = 100_000;
    private long requestTimeoutMs = 30_000L;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public GodMode getMode() {
        return mode;
    }

    public void setMode(GodMode mode) {
        this.mode = mode;
    }

    public int getMaxHistory() {
        return maxHistory;
    }

    public void setMaxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    public int getDailyTokenBudget() {
        return dailyTokenBudget;
    }

    public void setDailyTokenBudget(int dailyTokenBudget) {
        this.dailyTokenBudget = dailyTokenBudget;
    }

    public long getRequestTimeoutMs() {
        return requestTimeoutMs;
    }

    public void setRequestTimeoutMs(long requestTimeoutMs) {
        this.requestTimeoutMs = requestTimeoutMs;
    }

    /**
     * Creates a shallow copy of this config.
     */
    public AgentConfig copy() {
        AgentConfig copy = new AgentConfig();
        copy.provider = this.provider;
        copy.apiKey = this.apiKey;
        copy.baseUrl = this.baseUrl;
        copy.model = this.model;
        copy.mode = this.mode;
        copy.maxHistory = this.maxHistory;
        copy.dailyTokenBudget = this.dailyTokenBudget;
        copy.requestTimeoutMs = this.requestTimeoutMs;
        return copy;
    }
}
