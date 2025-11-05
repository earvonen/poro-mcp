package com.example.mcp.tool;

import com.example.mcp.mcp.ContentItem;
import com.example.mcp.mcp.Tool;
import com.example.mcp.mcp.ToolResult;
import com.example.mcp.openai.ChatCompletionRequest;
import com.example.mcp.openai.ChatCompletionResponse;
import com.example.mcp.openai.Message;
import com.example.mcp.service.OpenAiClientService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.*;

@ApplicationScoped
public class DraftFinnishTool implements ToolInterface {
    private static final Logger LOG = Logger.getLogger(DraftFinnishTool.class);

    @Inject
    OpenAiClientService openAiClientService;

    @Override
    public String getName() {
        return "draft_finnish";
    }

    @Override
    public String getDescription() {
        return "Compose high-quality Finnish text from given notes.";
    }

    @Override
    public Tool getToolDefinition() {
        Tool tool = new Tool();
        tool.setName(getName());
        tool.setDescription(getDescription());
        
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new LinkedHashMap<>();
        
        Map<String, Object> brief = new LinkedHashMap<>();
        brief.put("type", "string");
        brief.put("description", "Short task summary in English or Finnish");
        properties.put("brief", brief);
        
        Map<String, Object> keyPoints = new LinkedHashMap<>();
        keyPoints.put("type", "array");
        Map<String, Object> items = new LinkedHashMap<>();
        items.put("type", "string");
        keyPoints.put("items", items);
        keyPoints.put("description", "Bullet points to cover");
        properties.put("key_points", keyPoints);
        
        Map<String, Object> tone = new LinkedHashMap<>();
        tone.put("type", "string");
        tone.put("enum", Arrays.asList("neutral", "friendly", "professional", "persuasive", "concise"));
        tone.put("default", "professional");
        properties.put("tone", tone);
        
        Map<String, Object> length = new LinkedHashMap<>();
        length.put("type", "string");
        length.put("enum", Arrays.asList("short", "medium", "long"));
        length.put("default", "medium");
        properties.put("length", length);
        
        Map<String, Object> audience = new LinkedHashMap<>();
        audience.put("type", "string");
        audience.put("description", "Intended audience (e.g., executives, developers, general)");
        properties.put("audience", audience);
        
        schema.put("properties", properties);
        schema.put("required", Arrays.asList("brief", "key_points"));
        schema.put("additionalProperties", false);
        
        tool.setInputSchema(schema);
        return tool;
    }

    @Override
    public ToolResult invoke(Map<String, Object> args) throws Exception {
        // Validate required fields
        if (!args.containsKey("brief") || args.get("brief") == null) {
            throw new IllegalArgumentException("Missing required field: brief");
        }
        if (!args.containsKey("key_points") || args.get("key_points") == null) {
            throw new IllegalArgumentException("Missing required field: key_points");
        }

        String brief = (String) args.get("brief");
        @SuppressWarnings("unchecked")
        List<String> keyPoints = (List<String>) args.get("key_points");
        String tone = (String) args.getOrDefault("tone", "professional");
        String length = (String) args.getOrDefault("length", "medium");
        String audience = (String) args.get("audience");

        // Build the prompt
        String systemPrompt = "Kirjoita sujuvaa, luonnollista suomen kieltä. Käytä annettuja ohjeita ja säilytä informaatio tarkkana. Vastaa vain annettujen tietojen pohjalta.";
        
        StringBuilder userPromptBuilder = new StringBuilder();
        userPromptBuilder.append("Tehtävä: ").append(brief).append("\n\n");
        userPromptBuilder.append("Keskeiset kohdat:\n");
        for (String point : keyPoints) {
            userPromptBuilder.append("• ").append(point).append("\n");
        }
        userPromptBuilder.append("\n");
        userPromptBuilder.append("Sävy: ").append(tone).append("\n");
        userPromptBuilder.append("Pituus: ").append(length).append("\n");
        if (audience != null && !audience.isEmpty()) {
            userPromptBuilder.append("Kohdeyleisö: ").append(audience).append("\n");
        }
        userPromptBuilder.append("\nVastaa suomeksi.\n");
        userPromptBuilder.append("\nJos jokin tieto puuttuu, tee selkeä oletus ja kerro oletus lyhyesti.");

        String userPrompt = userPromptBuilder.toString();

        LOG.infof("Invoking vLLM with prompt length: %d", userPrompt.length());

        // Call OpenAI-compatible API
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(openAiClientService.getModelName());
        request.setTemperature(openAiClientService.getDefaultTemperature());
        request.setMaxTokens(openAiClientService.getDefaultMaxTokens());
        request.setStream(false);

        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", systemPrompt));
        messages.add(new Message("user", userPrompt));
        request.setMessages(messages);

        ChatCompletionResponse response = openAiClientService.completions(request);

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new RuntimeException("No response from vLLM API");
        }

        String generatedText = response.getChoices().get(0).getMessage().getContent();
        
        ToolResult result = new ToolResult();
        result.setContent(Arrays.asList(new ContentItem("text", generatedText)));
        result.setIsError(false);

        return result;
    }
}

