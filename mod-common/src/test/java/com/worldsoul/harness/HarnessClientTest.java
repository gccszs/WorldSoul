package com.worldsoul.harness;

import com.worldsoul.config.HarnessSettings;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HarnessClientTest {
    @Test
    public void healthCheckDoesNotAttemptClearTextHttp2Upgrade() throws Exception {
        AtomicReference<String> requestHeaders = new AtomicReference<>("");
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            server.setSoTimeout(5000);
            Thread responder = new Thread(() -> respondOnce(server, requestHeaders, "{\"ok\":true}"), "harness-http-test");
            responder.setDaemon(true);
            responder.start();

            HarnessClient client = new HarnessClient(new HarnessSettings(
                    "http://127.0.0.1:" + server.getLocalPort() + "/worldsoul", true));

            assertTrue(client.healthCheck().get(5, TimeUnit.SECONDS));
            responder.join(1000);
            assertFalse(requestHeaders.get().toLowerCase(Locale.ROOT).contains("upgrade: h2c"));
        }
    }

    @Test
    public void transcribeVoiceUsesSplitEndpointAndReturnsTranscript() throws Exception {
        AtomicReference<String> requestHeaders = new AtomicReference<>("");
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            server.setSoTimeout(5000);
            Thread responder = new Thread(() -> respondOnce(
                    server, requestHeaders, "{\"transcript\":\"立即回显\"}"), "harness-transcribe-test");
            responder.setDaemon(true);
            responder.start();

            HarnessClient client = new HarnessClient(new HarnessSettings(
                    "http://127.0.0.1:" + server.getLocalPort() + "/worldsoul", true));
            String transcript = client.transcribeVoice("world", "player", new byte[44])
                    .get(5, TimeUnit.SECONDS);

            assertEquals("立即回显", transcript);
            responder.join(1000);
            assertTrue(requestHeaders.get().startsWith(
                    "POST /worldsoul/v1/worlds/world/players/player/voice/transcribe HTTP/1.1"));
        }
    }

    private static void respondOnce(
            ServerSocket server, AtomicReference<String> requestHeaders, String responseBody) {
        try (Socket socket = server.accept();
             BufferedReader reader = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.US_ASCII))) {
            StringBuilder headers = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                headers.append(line).append('\n');
            }
            requestHeaders.set(headers.toString());

            byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
            String response = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: application/json\r\n"
                    + "Content-Length: " + body.length + "\r\n"
                    + "Connection: close\r\n\r\n";
            socket.getOutputStream().write(response.getBytes(StandardCharsets.US_ASCII));
            socket.getOutputStream().write(body);
            socket.getOutputStream().flush();
        } catch (Exception error) {
            throw new RuntimeException(error);
        }
    }
}
