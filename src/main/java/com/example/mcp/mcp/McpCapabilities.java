package com.example.mcp.mcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpCapabilities {
    private Map<String, Object> tools;

    public McpCapabilities() {}

    public Map<String, Object> getTools() {
        return tools;
    }

    public void setTools(Map<String, Object> tools) {
        this.tools = tools;
    }
}

