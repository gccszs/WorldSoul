package com.worldsoul.agent.provider;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.worldsoul.agent.config.AgentConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * OpenAI-compatible chat provider used by DeepSeek and OpenAI.
 */
public final class HttpChatProvider implements AIProvider {

    private final AgentConfig config;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    public HttpChatProvider(AgentConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getRequestTimeoutMs()))
                .build();
    }

    @Override
    public CompletableFuture<ChatResponse> chat(List<ChatMessage> messages) {
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("API key is missing. Configure apiKey or use provider=echo."));
        }

        JsonObject body = new JsonObject();
        body.addProperty("model", config.getModel());
        JsonArray payloadMessages = new JsonArray();
        for (ChatMessage message : messages) {
            JsonObject item = new JsonObject();
            item.addProperty("role", message.getRole());
            item.addProperty("content", message.getContent());
            payloadMessages.add(item);
        }
        body.add("messages", payloadMessages);

        String endpoint = trimTrailingSlash(config.getBaseUrl()) + "/v1/chat/completions";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofMillis(config.getRequestTimeoutMs()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::toChatResponse);
    }

    private ChatResponse toChatResponse(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("LLM HTTP " + response.statusCode() + ": " + response.body());
        }
        JsonObject root = gson.fromJson(response.body(), JsonObject.class);
        String content = root.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();

        int promptTokens = 0;
        int completionTokens = 0;
        if (root.has("usage")) {
            JsonObject usage = root.getAsJsonObject("usage");
            promptTokens = usage.has("prompt_tokens") ? usage.get("prompt_tokens").getAsInt() : 0;
            completionTokens = usage.has("completion_tokens") ? usage.get("completion_tokens").getAsInt() : 0;
        }
        return new ChatResponse(content, promptTokens, completionTokens);
    }

    @Override
    public String getId() {
        return config.getProvider();
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            return "https://api.deepseek.com";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
