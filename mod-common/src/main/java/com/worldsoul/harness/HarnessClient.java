package com.worldsoul.harness;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.worldsoul.config.HarnessSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/** HTTP client for the Minecraft-version-independent WorldSoul Harness protocol. */
public final class HarnessClient {
    private static final Logger LOGGER = LoggerFactory.getLogger("worldsoul-harness-client");
    private final HarnessSettings settings;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    public HarnessClient(HarnessSettings settings) {
        this.settings = settings;
        // The local Harness endpoint is HTTP/1.1. Java's default clear-text HTTP/2
        // upgrade is rejected by the Harness web server before it sends headers.
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public HarnessSettings getSettings() { return settings; }

    public CompletableFuture<Boolean> healthCheck() {
        return send("GET", "/v1/health", null).thenApply(response -> response.statusCode() == 200);
    }

    /** Retries health a few times and reports one descriptive connection failure. */
    public CompletableFuture<Void> ensureHealthy() { return attemptHealth(1, 3); }

    private CompletableFuture<Void> attemptHealth(int attempt, int maxAttempts) {
        return healthCheck().handle((ok, error) -> {
            if (error == null && Boolean.TRUE.equals(ok)) return CompletableFuture.<Void>completedFuture(null);
            if (attempt >= maxAttempts) throw new CompletionException(describeHealthFailure(error));
            try {
                Thread.sleep(400L * attempt);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new CompletionException(describeHealthFailure(interrupted));
            }
            return attemptHealth(attempt + 1, maxAttempts);
        }).thenCompose(next -> next);
    }

    private IllegalStateException describeHealthFailure(Throwable error) {
        String root = rootMessage(error);
        LOGGER.error("Harness unreachable at {}: {}", settings.getBaseUrl(), root);
        return new IllegalStateException("Harness 未启动或连不上 " + settings.getBaseUrl()
                + "（" + root + "）。请先运行 pnpm dsh --profile worldsoul");
    }

    public CompletableFuture<UsageStatus> playerOnline(
            String worldId, String playerId, String playerName, String mode, PlayerSnapshot playerState) {
        JsonObject body = new JsonObject();
        body.addProperty("playerName", playerName);
        body.addProperty("mode", mode);
        body.add("playerState", gson.toJsonTree(playerState));
        return send("PUT", playerPath(worldId, playerId) + "/online", body)
                .thenApply(this::ensureSuccess)
                .thenApply(json -> parseUsage(json.getAsJsonObject("usage")));
    }

    public CompletableFuture<Void> playerOffline(String worldId, String playerId) {
        return send("DELETE", playerPath(worldId, playerId) + "/online", null)
                .thenApply(this::ensureSuccess).thenApply(ignored -> null);
    }

    public CompletableFuture<Void> worldOffline(String worldId) {
        return send("DELETE", "/v1/worlds/" + pathSegment(worldId), null)
                .thenApply(this::ensureSuccess).thenApply(ignored -> null);
    }

    public CompletableFuture<ChatResult> chat(
            String worldId, String playerId, String message, PlayerSnapshot playerState) {
        JsonObject body = new JsonObject();
        body.addProperty("message", message);
        body.add("playerState", gson.toJsonTree(playerState));
        return send("POST", playerPath(worldId, playerId) + "/chat", body)
                .thenApply(this::ensureSuccess).thenApply(this::parseWorldSoulResult);
    }

    public CompletableFuture<UsageStatus> setMode(String worldId, String playerId, String mode) {
        JsonObject body = new JsonObject();
        body.addProperty("mode", mode);
        return send("PUT", playerPath(worldId, playerId) + "/mode", body)
                .thenApply(this::ensureSuccess)
                .thenApply(json -> parseUsage(json.getAsJsonObject("usage")));
    }

    public CompletableFuture<Void> updatePlayerState(
            String worldId, String playerId, PlayerSnapshot playerState) {
        JsonObject body = new JsonObject();
        body.add("playerState", gson.toJsonTree(playerState));
        return send("PUT", playerPath(worldId, playerId) + "/state", body)
                .thenApply(this::ensureSuccess).thenApply(ignored -> null);
    }

    public CompletableFuture<ChatResult> commandResult(
            String worldId, String playerId, String proposalId, boolean accepted, String error) {
        JsonObject body = new JsonObject();
        body.addProperty("accepted", accepted);
        if (error != null && !error.isBlank()) body.addProperty("error", error);
        return send("POST", playerPath(worldId, playerId) + "/commands/"
                        + pathSegment(proposalId) + "/result", body)
                .thenApply(this::ensureSuccess).thenApply(this::parseWorldSoulResult);
    }

    public CompletableFuture<VoiceResult> voice(String worldId, String playerId, byte[] waveAudio) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(settings.getBaseUrl() + playerPath(worldId, playerId) + "/voice"))
                .timeout(Duration.ofSeconds(90))
                .header("Content-Type", "audio/wav")
                .POST(HttpRequest.BodyPublishers.ofByteArray(waveAudio))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(this::ensureSuccess)
                .thenApply(json -> new VoiceResult(
                        json.get("transcript").getAsString(),
                        parseWorldSoulResult(json.getAsJsonObject("reply"))));
    }

    /** Transcribes audio without waiting for an Agent turn. */
    public CompletableFuture<String> transcribeVoice(String worldId, String playerId, byte[] waveAudio) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(settings.getBaseUrl() + playerPath(worldId, playerId) + "/voice/transcribe"))
                .timeout(Duration.ofSeconds(90))
                .header("Content-Type", "audio/wav")
                .POST(HttpRequest.BodyPublishers.ofByteArray(waveAudio))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(this::ensureSuccess)
                .thenApply(json -> json.get("transcript").getAsString());
    }

    private ChatResult parseWorldSoulResult(JsonObject detail) {
        String speech = detail.has("speech") ? detail.get("speech").getAsString() : "";
        List<CommandProposal> commands = new ArrayList<>();
        if (detail.has("commands") && detail.get("commands").isJsonArray()) {
            detail.getAsJsonArray("commands").forEach(node -> {
                JsonObject command = node.getAsJsonObject();
                commands.add(new CommandProposal(
                        command.get("id").getAsString(), command.get("command").getAsString(),
                        command.has("rationale") ? command.get("rationale").getAsString() : ""));
            });
        }
        UsageStatus usage = detail.has("usage") ? parseUsage(detail.getAsJsonObject("usage")) : null;
        return new ChatResult(speech, commands, usage);
    }

    private UsageStatus parseUsage(JsonObject usage) {
        return new UsageStatus(
                usage.get("used").getAsLong(), usage.get("limit").getAsLong(),
                usage.get("ratio").getAsDouble(), usage.get("state").getAsString(),
                usage.get("disabled").getAsBoolean());
    }

    private CompletableFuture<HttpResponse<String>> send(String method, String path, JsonObject body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(settings.getBaseUrl() + path)).timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json; charset=utf-8");
        if ("GET".equals(method)) builder.GET();
        else if ("DELETE".equals(method)) builder.DELETE();
        else if (body != null) builder.method(method, HttpRequest.BodyPublishers.ofString(gson.toJson(body)));
        else builder.method(method, HttpRequest.BodyPublishers.noBody());
        return httpClient.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private JsonObject ensureSuccess(HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return gson.fromJson(response.body(), JsonObject.class);
        }
        throw toException(response);
    }

    private IllegalStateException toException(HttpResponse<String> response) {
        String body = response.body();
        String message = "Harness HTTP " + response.statusCode();
        try {
            JsonObject json = gson.fromJson(body, JsonObject.class);
            if (json != null && json.has("error")) {
                if (json.get("error").isJsonObject()) {
                    JsonObject error = json.getAsJsonObject("error");
                    if (error.has("message")) message = error.get("message").getAsString();
                } else message = json.get("error").getAsString();
            }
        } catch (RuntimeException ignored) {
            if (body != null && !body.isBlank()) message = body;
        }
        return new IllegalStateException(message);
    }

    private static String playerPath(String worldId, String playerId) {
        return "/v1/worlds/" + pathSegment(worldId) + "/players/" + pathSegment(playerId);
    }

    private static String pathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String rootMessage(Throwable error) {
        if (error == null) return "health check returned non-OK";
        Throwable current = error;
        while (current.getCause() != null && current.getCause() != current) current = current.getCause();
        return current.getMessage() == null || current.getMessage().isBlank()
                ? current.getClass().getSimpleName() : current.getMessage();
    }

    public static final class ChatResult {
        private final String speech;
        private final List<CommandProposal> commands;
        private final UsageStatus usage;

        public ChatResult(String speech, List<CommandProposal> commands, UsageStatus usage) {
            this.speech = speech;
            this.commands = List.copyOf(commands);
            this.usage = usage;
        }

        public String getSpeech() { return speech; }
        public List<CommandProposal> getCommands() { return commands; }
        public UsageStatus getUsage() { return usage; }
    }

    public static final class CommandProposal {
        private final String id;
        private final String command;
        private final String rationale;

        public CommandProposal(String id, String command, String rationale) {
            this.id = id;
            this.command = command;
            this.rationale = rationale;
        }

        public String getId() { return id; }
        public String getCommand() { return command; }
        public String getRationale() { return rationale; }
    }

    public static final class UsageStatus {
        private final long used;
        private final long limit;
        private final double ratio;
        private final String state;
        private final boolean disabled;

        public UsageStatus(long used, long limit, double ratio, String state, boolean disabled) {
            this.used = used;
            this.limit = limit;
            this.ratio = ratio;
            this.state = state;
            this.disabled = disabled;
        }

        public long getUsed() { return used; }
        public long getLimit() { return limit; }
        public double getRatio() { return ratio; }
        public String getState() { return state; }
        public boolean isDisabled() { return disabled; }
    }

    public static final class VoiceResult {
        private final String transcript;
        private final ChatResult reply;

        public VoiceResult(String transcript, ChatResult reply) {
            this.transcript = transcript;
            this.reply = reply;
        }

        public String getTranscript() { return transcript; }
        public ChatResult getReply() { return reply; }
    }
}
