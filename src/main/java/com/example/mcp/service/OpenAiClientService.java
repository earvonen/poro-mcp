package com.example.mcp.service;

import com.example.mcp.openai.ChatCompletionRequest;
import com.example.mcp.openai.ChatCompletionResponse;
import com.example.mcp.openai.OpenAiClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.Optional;

@ApplicationScoped
public class OpenAiClientService {
    private static final Logger LOG = Logger.getLogger(OpenAiClientService.class);

    @Inject
    @RestClient
    OpenAiClient openAiClient;

    @ConfigProperty(name = "openai.model-name")
    Optional<String> modelName;

    @ConfigProperty(name = "openai.default-temperature", defaultValue = "0.7")
    Double defaultTemperature;

    @ConfigProperty(name = "openai.default-max-tokens", defaultValue = "800")
    Integer defaultMaxTokens;

    public String getModelName() {
        return modelName.orElseThrow(() -> new IllegalStateException("PORO2_MODEL_NAME not set"));
    }

    public Double getDefaultTemperature() {
        return defaultTemperature;
    }

    public Integer getDefaultMaxTokens() {
        return defaultMaxTokens;
    }

    public ChatCompletionResponse completions(ChatCompletionRequest request) {
        try {
            LOG.debugf("Calling OpenAI API with model: %s", request.getModel());
            return openAiClient.completions(request);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to call OpenAI API");
            throw new RuntimeException("Failed to call vLLM API: " + e.getMessage(), e);
        }
    }
}

