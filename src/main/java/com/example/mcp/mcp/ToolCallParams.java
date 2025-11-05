package com.example.mcp.mcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class ToolCallParams {
    private String name;
    private Map<String, Object> arguments;

    public ToolCallParams() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }
}

