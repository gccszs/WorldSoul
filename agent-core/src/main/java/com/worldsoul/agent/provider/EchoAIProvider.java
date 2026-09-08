package com.worldsoul.agent.provider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Offline stub provider for local harness development without an API key.
 */
public final class EchoAIProvider implements AIProvider {

    @Override
    public CompletableFuture<ChatResponse> chat(List<ChatMessage> messages) {
        String lastUser = "";
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage message = messages.get(i);
            if ("user".equals(message.getRole())) {
                lastUser = message.getContent();
                break;
            }
        }

        String reply = "[echo] 收到：" + lastUser
                + "\n（当前为本地 Echo Provider，配置真实 API Key 后可切换 DeepSeek/OpenAI）";
        return CompletableFuture.completedFuture(new ChatResponse(reply, 0, 0));
    }

    @Override
    public String getId() {
        return "echo";
    }
}
