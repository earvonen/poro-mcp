package com.example.mcp.http;

import com.example.mcp.jsonrpc.JsonRpcRequest;
import com.example.mcp.jsonrpc.JsonRpcResponse;
import com.example.mcp.service.McpRequestProcessor;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestStreamElementType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Path("/mcp")
@ApplicationScoped
public class McpSseEndpoint {
    private static final Logger LOG = Logger.getLogger(McpSseEndpoint.class);

    private final Map<String, MultiEmitter<? super Object>> clientEmitters = new ConcurrentHashMap<>();

    @Inject
    McpRequestProcessor requestProcessor;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<Object> rootStream(@QueryParam("clientId") String clientId) {
        return openStream(clientId);
    }

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<Object> streamAlias(@QueryParam("clientId") String clientId) {
        return openStream(clientId);
    }

    @POST
    @Path("/request")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonRpcResponse handleRequest(JsonRpcRequest request,
                                         @QueryParam("clientId") String clientId) {
        JsonRpcResponse response = requestProcessor.handle(request);
        broadcastResponse(clientId, response);
        return response;
    }

    private void broadcastResponse(String clientId, JsonRpcResponse response) {
        if (clientId != null && !clientId.isBlank()) {
            emit(clientId, response);
        } else {
            clientEmitters.keySet().forEach(id -> emit(id, response));
        }
    }

    private void emit(String clientId, Object payload) {
        MultiEmitter<? super Object> emitter = clientEmitters.get(clientId);
        if (emitter == null) {
            LOG.warnf("No SSE client registered with id %s; response will not be streamed", clientId);
            return;
        }

        try {
            emitter.emit(payload);
        } catch (IllegalStateException e) {
            LOG.warnf(e, "Failed to emit SSE event to client %s", clientId);
            clientEmitters.remove(clientId);
        }
    }

    private Multi<Object> openStream(String clientId) {
        String id = (clientId == null || clientId.isBlank()) ? UUID.randomUUID().toString() : clientId;
        LOG.infof("SSE client connected: %s", id);

        return Multi.createFrom().emitter(emitter -> {
            clientEmitters.put(id, emitter);
            emitter.onTermination(() -> {
                LOG.infof("SSE client disconnected: %s", id);
                clientEmitters.remove(id);
            });

            emitter.emit(Map.of(
                "event", "connected",
                "clientId", id,
                "status", "connected"
            ));
        });
    }
}

