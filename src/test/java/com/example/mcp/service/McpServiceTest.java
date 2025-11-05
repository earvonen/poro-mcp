package com.example.mcp.service;

import com.example.mcp.jsonrpc.JsonRpcResponse;
import com.example.mcp.mcp.ToolCallParams;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class McpServiceTest {

    @Inject
    McpService mcpService;

    @Test
    void testHandleInitialize() {
        JsonRpcResponse response = mcpService.handleInitialize(1);
        assertNotNull(response);
        assertNull(response.getError());
        assertNotNull(response.getResult());
    }

    @Test
    void testHandleToolsList() {
        JsonRpcResponse response = mcpService.handleToolsList(2);
        assertNotNull(response);
        assertNull(response.getError());
        assertNotNull(response.getResult());
        
        @SuppressWarnings("unchecked")
        var tools = (java.util.List<?>) response.getResult();
        assertFalse(tools.isEmpty());
    }

    @Test
    void testHandleToolsCallWithInvalidTool() {
        ToolCallParams params = new ToolCallParams();
        params.setName("nonexistent_tool");
        params.setArguments(new HashMap<>());
        
        JsonRpcResponse response = mcpService.handleToolsCall(3, params);
        assertNotNull(response);
        assertNotNull(response.getError());
    }

    @Test
    void testHandleToolsCallWithMissingArgs() {
        ToolCallParams params = new ToolCallParams();
        params.setName("draft_finnish");
        params.setArguments(new HashMap<>());
        
        JsonRpcResponse response = mcpService.handleToolsCall(4, params);
        assertNotNull(response);
        // Should have validation error
        assertNotNull(response.getError());
    }

    @Test
    void testHandlePing() {
        JsonRpcResponse response = mcpService.handlePing(5);
        assertNotNull(response);
        assertNull(response.getError());
        assertNotNull(response.getResult());
    }
}

