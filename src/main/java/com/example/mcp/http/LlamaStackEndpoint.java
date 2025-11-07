package com.example.mcp.http;

import com.example.mcp.jsonrpc.JsonRpcResponse;
import com.example.mcp.mcp.Tool;
import com.example.mcp.service.McpRequestProcessor;
import com.example.mcp.service.McpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class LlamaStackEndpoint {
    private static final Logger LOG = Logger.getLogger(LlamaStackEndpoint.class);

    @Inject
    McpService mcpService;

    @Inject
    ObjectMapper objectMapper;

    @GET
    @Path("/tools/_stcore/health")
    public Map<String, String> toolsHealth() {
        return Map.of("status", "ok");
    }

    @GET
    @Path("/tools/_stcore/host-config")
    public Map<String, Object> hostConfig() {
        // Streamlit expects a JSON object even if empty
        return Collections.emptyMap();
    }

    @GET
    @Path("/v1/tools")
    @SuppressWarnings("unchecked")
    public Map<String, Object> listTools(@QueryParam("toolgroup_id") String toolGroupId) {
        LOG.infof("LlamaStack list tools invoked for group %s", toolGroupId);
        JsonRpcResponse response = mcpService.handleToolsList(toolGroupId != null ? toolGroupId : "llama-stack");
        Object result = response.getResult();
        List<Tool> tools = result instanceof List ? (List<Tool>) result : List.of();
        return Map.of(
            "object", "list",
            "data", tools
        );
    }
}

