package com.example.mcp.jsonrpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class JsonRpcRequest {
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";
    
    private String id;
    private String method;
    private Map<String, Object> params;

    public JsonRpcRequest() {}

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}

