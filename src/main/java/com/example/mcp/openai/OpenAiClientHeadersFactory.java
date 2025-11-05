package com.example.mcp.openai;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.Optional;

@ApplicationScoped
public class OpenAiClientHeadersFactory implements ClientHeadersFactory {
    @ConfigProperty(name = "openai.api-key")
    Optional<String> apiKey;

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders,
                                                   MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        if (apiKey.isPresent() && !apiKey.get().isEmpty()) {
            headers.add("Authorization", "Bearer " + apiKey.get());
        }
        return headers;
    }
}

