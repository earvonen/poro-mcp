package com.example.mcp.mcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpInitializeResult {
    private String protocolVersion = "2024-11-05";
    private McpCapabilities capabilities;
    private ServerInfo serverInfo;

    public McpInitializeResult() {}

    @JsonProperty("protocolVersion")
    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public McpCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(McpCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    @JsonProperty("serverInfo")
    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }
}

