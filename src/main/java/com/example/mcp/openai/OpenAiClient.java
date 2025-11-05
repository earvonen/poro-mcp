package com.example.mcp.openai;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "openai")
@RegisterClientHeaders(OpenAiClientHeadersFactory.class)
public interface OpenAiClient {
    @POST
    @Path("/v1/chat/completions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ChatCompletionResponse completions(ChatCompletionRequest request);
}

