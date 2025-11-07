package com.example.mcp.http.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LlamaStackToolListResponse {
    private String object = "list";
    private List<LlamaStackTool> data;
    private Boolean hasMore = false;

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public List<LlamaStackTool> getData() {
        return data;
    }

    public void setData(List<LlamaStackTool> data) {
        this.data = data;
    }

    public Boolean getHasMore() {
        return hasMore;
    }

    public void setHasMore(Boolean hasMore) {
        this.hasMore = hasMore;
    }
}

