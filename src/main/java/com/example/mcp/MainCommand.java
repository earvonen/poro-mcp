package com.example.mcp;

import com.example.mcp.jsonrpc.ErrorObject;
import com.example.mcp.jsonrpc.JsonRpcRequest;
import com.example.mcp.jsonrpc.JsonRpcResponse;
import com.example.mcp.mcp.ToolCallParams;
import com.example.mcp.service.McpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

@Command(name = "quarkus-mcp", mixinStandardHelpOptions = true)
public class MainCommand implements Runnable {
    private static final Logger LOG = Logger.getLogger(MainCommand.class);

    @Inject
    McpService mcpService;

    @Inject
    ObjectMapper objectMapper;

    private volatile boolean running = true;

    @Override
    public void run() {
        LOG.info("Starting Quarkus MCP server");
        
        // Initialize tools
        mcpService.init();
        
        // Set up signal handlers for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down MCP server");
            running = false;
        }));

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        try {
            String line;
            while (running && (line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    processJsonRpcRequest(line.trim());
                } catch (Exception e) {
                    LOG.errorf(e, "Error processing request");
                    JsonRpcResponse errorResponse = new JsonRpcResponse(
                        null,
                        new ErrorObject(ErrorObject.PARSE_ERROR, "Failed to process request: " + e.getMessage())
                    );
                    writeResponse(errorResponse);
                }
            }
        } catch (IOException e) {
            if (running) {
                LOG.errorf(e, "Fatal error reading from stdin");
            }
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                LOG.warnf(e, "Error closing reader");
            }
        }
    }

    private void processJsonRpcRequest(String json) {
        try {
            JsonRpcRequest request = objectMapper.readValue(json, JsonRpcRequest.class);
            
            if (!"2.0".equals(request.getJsonrpc())) {
                writeResponse(new JsonRpcResponse(
                    request.getId(),
                    new ErrorObject(ErrorObject.INVALID_REQUEST, "Invalid jsonrpc version")
                ));
                return;
            }

            JsonRpcResponse response = dispatchRequest(request);
            writeResponse(response);
            
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            LOG.warnf(e, "Failed to parse JSON-RPC request");
            writeResponse(new JsonRpcResponse(
                null,
                new ErrorObject(ErrorObject.PARSE_ERROR, "Parse error: " + e.getMessage())
            ));
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error processing request");
            writeResponse(new JsonRpcResponse(
                null,
                new ErrorObject(ErrorObject.INTERNAL_ERROR, "Internal error: " + e.getMessage())
            ));
        }
    }

    private JsonRpcResponse dispatchRequest(JsonRpcRequest request) {
        String method = request.getMethod();
        Object id = request.getId();
        Map<String, Object> params = request.getParams();

        if (method == null) {
            return new JsonRpcResponse(id, new ErrorObject(
                ErrorObject.INVALID_REQUEST, "Missing method"
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
                        ErrorObject.INVALID_PARAMS, "Missing params"
                    ));
                }
                try {
                    ToolCallParams toolParams = objectMapper.convertValue(params, ToolCallParams.class);
                    return mcpService.handleToolsCall(id, toolParams);
                } catch (Exception e) {
                    LOG.warnf(e, "Failed to parse tool call params");
                    return new JsonRpcResponse(id, new ErrorObject(
                        ErrorObject.INVALID_PARAMS, "Invalid params: " + e.getMessage()
                    ));
                }
            
            case "ping":
                return mcpService.handlePing(id);
            
            default:
                return new JsonRpcResponse(id, new ErrorObject(
                    ErrorObject.METHOD_NOT_FOUND, "Method not found: " + method
                ));
        }
    }

    private void writeResponse(JsonRpcResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            System.out.println(json);
            System.out.flush();
        } catch (Exception e) {
            LOG.errorf(e, "Failed to write response");
            // Try to write error to stderr
            System.err.println("{\"jsonrpc\":\"2.0\",\"id\":null,\"error\":{\"code\":-32603,\"message\":\"Failed to serialize response\"}}");
            System.err.flush();
        }
    }
}

