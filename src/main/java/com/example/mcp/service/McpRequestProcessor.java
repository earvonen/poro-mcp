package com.example.mcp.service;

import com.example.mcp.jsonrpc.ErrorObject;
import com.example.mcp.jsonrpc.JsonRpcRequest;
import com.example.mcp.jsonrpc.JsonRpcResponse;
import com.example.mcp.mcp.ToolCallParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Map;

@ApplicationScoped
public class McpRequestProcessor {
    private static final Logger LOG = Logger.getLogger(McpRequestProcessor.class);

    @Inject
    McpService mcpService;

    @Inject
    ObjectMapper objectMapper;

    public JsonRpcResponse handle(JsonRpcRequest request) {
        if (request == null) {
            return new JsonRpcResponse(null, new ErrorObject(
                ErrorObject.INVALID_REQUEST,
                "Request body is missing"
            ));
        }

        if (!"2.0".equals(request.getJsonrpc())) {
            return new JsonRpcResponse(request.getId(), new ErrorObject(
                ErrorObject.INVALID_REQUEST,
                "Invalid jsonrpc version"
            ));
        }

        String method = request.getMethod();
        Object id = request.getId();
        Map<String, Object> params = request.getParams();

        if (method == null || method.isBlank()) {
            return new JsonRpcResponse(id, new ErrorObject(
                ErrorObject.INVALID_REQUEST,
                "Missing method"
            ));
        }

        switch (method) {
            case "initialize":
                return mcpService.handleInitialize(id);

            case "tools/list":
                return mcpService.handleToolsList(id);

            case "tools/call":
                if (params == null) {
                    return new JsonRpcResponse(id, new ErrorObject(
                        ErrorObject.INVALID_PARAMS,
                        "Missing params"
                    ));
                }

                try {
                    ToolCallParams toolParams = objectMapper.convertValue(params, ToolCallParams.class);
                    return mcpService.handleToolsCall(id, toolParams);
                } catch (IllegalArgumentException e) {
                    LOG.warnf(e, "Failed to convert params for tools/call");
                    return new JsonRpcResponse(id, new ErrorObject(
                        ErrorObject.INVALID_PARAMS,
                        "Invalid params: " + e.getMessage()
                    ));
                }

            case "ping":
                return mcpService.handlePing(id);

            default:
                return new JsonRpcResponse(id, new ErrorObject(
                    ErrorObject.METHOD_NOT_FOUND,
                    "Method not found: " + method
                ));
        }
    }
}

