package com.example.mcp.http;

import com.example.mcp.jsonrpc.JsonRpcResponse;
import com.example.mcp.mcp.ToolCallParams;
import com.example.mcp.service.McpService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

import java.util.Map;

@Path("/mcp/v1")
@ApplicationScoped
public class McpHttpEndpoint {
    private static final Logger LOG = Logger.getLogger(McpHttpEndpoint.class);

    @Inject
    McpService mcpService;

    @POST
    @Path("/initialize")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonRpcResponse initialize(@QueryParam("id") String id) {
        LOG.infof("HTTP: initialize request with id=%s", id);
        Object requestId = id != null ? id : "1";
        return mcpService.handleInitialize(requestId);
    }

    @GET
    @Path("/tools")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonRpcResponse toolsList(@QueryParam("toolgroup_id") String id) {
        LOG.infof("HTTP: tools/list request with id=%s", id);
        Object requestId = id != null ? id : "1";
        return mcpService.handleToolsList(requestId);
    }

    @GET
    @Path("/tools/call")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonRpcResponse toolsCall(ToolCallParams params, @QueryParam("id") String id) {
        LOG.infof("HTTP: tools/call request with id=%s, tool=%s", id, params != null ? params.getName() : "null");
        Object requestId = id != null ? id : "1";
        if (params == null) {
            return new JsonRpcResponse(requestId, 
                new com.example.mcp.jsonrpc.ErrorObject(
                    com.example.mcp.jsonrpc.ErrorObject.INVALID_PARAMS, 
                    "Missing params"
                ));
        }
        return mcpService.handleToolsCall(requestId, params);
    }

    @POST
    @Path("/ping")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonRpcResponse ping(@QueryParam("id") String id) {
        LOG.debugf("HTTP: ping request with id=%s", id);
        Object requestId = id != null ? id : "1";
        return mcpService.handlePing(requestId);
    }

    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}

