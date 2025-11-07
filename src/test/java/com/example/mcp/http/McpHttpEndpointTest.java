package com.example.mcp.http;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class McpHttpEndpointTest {

    @Test
    void testHealthEndpoint() {
        given()
          .when().get("/mcp/health")
          .then()
             .statusCode(200)
             .body("status", is("ok"));
    }

    @Test
    void testInitializeEndpoint() {
        given()
          .contentType(MediaType.APPLICATION_JSON)
          .when().post("/mcp/initialize?id=1")
          .then()
             .statusCode(200)
             .body("jsonrpc", is("2.0"))
             .body("id", is("1"))
             .body("result", notNullValue());
    }

    @Test
    void testToolsListEndpoint() {
        given()
          .contentType(MediaType.APPLICATION_JSON)
          .when().post("/mcp/tools/list?id=2")
          .then()
             .statusCode(200)
             .body("jsonrpc", is("2.0"))
             .body("id", is("2"))
             .body("result", notNullValue());
    }

    @Test
    void testPingEndpoint() {
        given()
          .contentType(MediaType.APPLICATION_JSON)
          .when().post("/mcp/ping?id=3")
          .then()
             .statusCode(200)
             .body("jsonrpc", is("2.0"))
             .body("id", is("3"))
             .body("result.status", is("ok"));
    }
}


