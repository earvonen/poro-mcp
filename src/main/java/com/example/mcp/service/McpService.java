package com.example.mcp.service;

import com.example.mcp.jsonrpc.ErrorObject;
import com.example.mcp.jsonrpc.JsonRpcResponse;
import com.example.mcp.mcp.*;
import com.example.mcp.tool.DraftFinnishTool;
import com.example.mcp.tool.ToolInterface;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.*;

@ApplicationScoped
public class McpService {
    private static final Logger LOG = Logger.getLogger(McpService.class);

    @Inject
    DraftFinnishTool draftFinnishTool;

    private final Map<String, ToolInterface> tools = new HashMap<>();

    public void init() {
        tools.put("mcp::draft_finnish", draftFinnishTool);
    }

    public JsonRpcResponse handleInitialize(Object id) {
        LOG.info("Handling initialize request");
        
        McpInitializeResult result = new McpInitializeResult();
        result.setProtocolVersion("2024-11-05");
        
        McpCapabilities capabilities = new McpCapabilities();
        capabilities.setTools(Map.of());
        result.setCapabilities(capabilities);
        
        ServerInfo serverInfo = new ServerInfo("quarkus-mcp-poro2", "1.0.0");
        result.setServerInfo(serverInfo);
        
        return new JsonRpcResponse(id, result);
    }

    public JsonRpcResponse handleToolsList(Object id) {
        LOG.info("Handling tools/list request");
        
        if (tools.isEmpty()) {
            init();
        }
        
        List<Tool> toolList = new ArrayList<>();
        for (ToolInterface tool : tools.values()) {
            toolList.add(tool.getToolDefinition());
        }
        
        return new JsonRpcResponse(id, toolList);
    }

    public JsonRpcResponse handleToolsCall(Object id, ToolCallParams params) {
        LOG.infof("Handling tools/call request for tool: %s", params.getName());
        
        if (tools.isEmpty()) {
            init();
        }
        
        ToolInterface tool = tools.get(params.getName());
        if (tool == null) {
            return new JsonRpcResponse(id, new ErrorObject(
                ErrorObject.METHOD_NOT_FOUND,
                "Tool not found: " + params.getName()
            ));
        }
        
        try {
            Map<String, Object> args = params.getArguments() != null ? params.getArguments() : new HashMap<>();
            ToolResult result = tool.invoke(args);
            return new JsonRpcResponse(id, result);
        } catch (IllegalArgumentException e) {
            LOG.warnf(e, "Validation error in tool %s", params.getName());
            return new JsonRpcResponse(id, new ErrorObject(
                ErrorObject.INVALID_PARAMS,
                "Invalid parameters: " + e.getMessage(),
                Map.of("tool", params.getName(), "error", e.getMessage())
            ));
        } catch (Exception e) {
            LOG.errorf(e, "Error executing tool %s", params.getName());
            return new JsonRpcResponse(id, new ErrorObject(
                ErrorObject.INTERNAL_ERROR,
                "Tool execution failed: " + e.getMessage(),
                Map.of("tool", params.getName())
            ));
        }
    }

    public JsonRpcResponse handlePing(Object id) {
        LOG.debug("Handling ping request");
        return new JsonRpcResponse(id, Map.of("status", "ok"));
    }
}

