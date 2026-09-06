package com.worldsoul.agent.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Parsed agent reply: chat text plus optional actions.
 */
public final class AgentReply {

    private final String speech;
    private final List<AgentAction> actions;

    public AgentReply(String speech, List<AgentAction> actions) {
        this.speech = Objects.requireNonNull(speech, "speech");
        this.actions = Collections.unmodifiableList(new ArrayList<>(actions));
    }

    public String getSpeech() {
        return speech;
    }

    public List<AgentAction> getActions() {
        return actions;
    }
}
