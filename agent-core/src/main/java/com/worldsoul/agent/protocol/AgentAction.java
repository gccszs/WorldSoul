package com.worldsoul.agent.protocol;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A structured action extracted from model output.
 */
public final class AgentAction {

    private final ActionType type;
    private final Map<String, String> arguments;

    public AgentAction(ActionType type, Map<String, String> arguments) {
        this.type = Objects.requireNonNull(type, "type");
        this.arguments = Collections.unmodifiableMap(new LinkedHashMap<>(arguments));
    }

    public ActionType getType() {
        return type;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public String getArgument(String key) {
        return arguments.get(key);
    }

    @Override
    public String toString() {
        return type + arguments.toString();
    }
}
