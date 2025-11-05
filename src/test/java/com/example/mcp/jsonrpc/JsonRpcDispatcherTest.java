package com.example.mcp.jsonrpc;

import com.example.mcp.MainCommand;
import com.example.mcp.service.McpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class JsonRpcDispatcherTest {

    @Inject
    McpService mcpService;

    @Inject
    ObjectMapper objectMapper;

    @Test
    void testInitializeRequest() throws Exception {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("jsonrpc", "2.0");
        requestMap.put("id", 1);
        requestMap.put("method", "initialize");
        requestMap.put("params", new HashMap<>());

        String json = objectMapper.writeValueAsString(requestMap);
        JsonRpcRequest request = objectMapper.readValue(json, JsonRpcRequest.class);
        
        assertEquals("2.0", request.getJsonrpc());
        assertEquals("initialize", request.getMethod());
        
        JsonRpcResponse response = mcpService.handleInitialize(request.getId());
        assertNotNull(response);
        assertNull(response.getError());
    }

    @Test
    void testToolsListRequest() throws Exception {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("jsonrpc", "2.0");
        requestMap.put("id", 2);
        requestMap.put("method", "tools/list");
        requestMap.put("params", new HashMap<>());

        String json = objectMapper.writeValueAsString(requestMap);
        JsonRpcRequest request = objectMapper.readValue(json, JsonRpcRequest.class);
        
        JsonRpcResponse response = mcpService.handleToolsList(request.getId());
        assertNotNull(response);
        assertNull(response.getError());
        
        @SuppressWarnings("unchecked")
        var tools = (java.util.List<?>) response.getResult();
        assertFalse(tools.isEmpty());
    }

    @Test
    void testToolsCallRequest() throws Exception {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("brief", "Test task");
        arguments.put("key_points", Arrays.asList("Point 1", "Point 2"));
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", "draft_finnish");
        params.put("arguments", arguments);
        
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("jsonrpc", "2.0");
        requestMap.put("id", 3);
        requestMap.put("method", "tools/call");
        requestMap.put("params", params);

        String json = objectMapper.writeValueAsString(requestMap);
        JsonRpcRequest request = objectMapper.readValue(json, JsonRpcRequest.class);
        
        // The actual call will fail without a real vLLM endpoint, but we can test the structure
        // In a real test with mocked client, we'd verify the response
        assertNotNull(request);
        assertEquals("tools/call", request.getMethod());
    }
}

