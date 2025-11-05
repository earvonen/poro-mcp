package com.example.mcp.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import io.quarkus.runtime.StartupEvent;

import java.util.Optional;

@ApplicationScoped
public class ConfigValidator {
    private static final Logger LOG = Logger.getLogger(ConfigValidator.class);

    @ConfigProperty(name = "openai/mp-rest/url")
    Optional<String> restClientUrl;

    @ConfigProperty(name = "openai.model-name")
    Optional<String> modelName;

    void onStart(@Observes StartupEvent ev) {
        LOG.info("Validating configuration...");
        
        if (restClientUrl.isEmpty() || restClientUrl.get().isBlank()) {
            throw new IllegalStateException(
                "VLLM_BASE_URL environment variable is required but not set. " +
                "Please set VLLM_BASE_URL to the base URL of your vLLM server (e.g., http://vllm:8000)"
            );
        }
        
        if (modelName.isEmpty() || modelName.get().isBlank()) {
            throw new IllegalStateException(
                "PORO2_MODEL_NAME environment variable is required but not set. " +
                "Please set PORO2_MODEL_NAME to the model name (e.g., LumiOpen/Llama-Poro-2-70B-Instruct)"
            );
        }
        
        LOG.infof("Configuration validated: restClientUrl=%s, modelName=%s", restClientUrl.get(), modelName.get());
    }
}

