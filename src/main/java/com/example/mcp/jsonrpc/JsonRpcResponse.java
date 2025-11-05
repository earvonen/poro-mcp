package com.example.mcp.jsonrpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcResponse {
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";
    
    private Object id;
    private Object result;
    private ErrorObject error;

    public JsonRpcResponse() {}

    public JsonRpcResponse(Object id, Object result) {
        this.id = id;
        this.result = result;
    }

    public JsonRpcResponse(Object id, ErrorObject error) {
        this.id = id;
        this.error = error;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public ErrorObject getError() {
        return error;
    }

    public void setError(ErrorObject error) {
        this.error = error;
    }
}

