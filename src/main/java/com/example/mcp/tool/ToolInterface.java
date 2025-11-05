package com.example.mcp.tool;

import com.example.mcp.mcp.Tool;
import com.example.mcp.mcp.ToolResult;
import java.util.Map;

public interface ToolInterface {
    String getName();
    String getDescription();
    Tool getToolDefinition();
    ToolResult invoke(Map<String, Object> args) throws Exception;
}

