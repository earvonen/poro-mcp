# Quarkus MCP Server

A Java + Quarkus implementation of a Model Context Protocol (MCP) server that provides high-quality Finnish text generation using a vLLM-served LumiOpen/Llama-Poro-2-70B-Instruct model via an OpenAI-compatible API.

## Features

- **MCP Protocol**: Full JSON-RPC 2.0 implementation with SSE streaming
- **Tool**: `draft_finnish` - Compose high-quality Finnish text from structured notes
- **Quarkus Command Mode**: No HTTP server required, pure stdio communication
- **OpenAI-Compatible API**: Works with vLLM and any OpenAI-compatible endpoint

## Prerequisites

- Java 21
- Maven 3.9+
- vLLM server running with OpenAI-compatible API (or any compatible endpoint)

## Environment Variables

Required:
- `VLLM_BASE_URL`: Base URL of the vLLM server (e.g., `http://vllm:8000`)
- `PORO2_MODEL_NAME`: Model name (e.g., `LumiOpen/Llama-Poro-2-70B-Instruct`)

Optional:
- `VLLM_API_KEY`: API key for authentication (if required)
- `DEFAULT_TEMPERATURE`: Temperature for text generation (default: `0.7`)
- `DEFAULT_MAX_TOKENS`: Maximum tokens to generate (default: `800`)

## Running Locally

### Development Mode

```bash
export VLLM_BASE_URL=http://localhost:8000
export PORO2_MODEL_NAME="LumiOpen/Llama-Poro-2-70B-Instruct"
./mvnw quarkus:dev
```

### Build and Run

```bash
# Build
./mvnw clean package

# Run
export VLLM_BASE_URL=http://localhost:8000
export PORO2_MODEL_NAME="LumiOpen/Llama-Poro-2-70B-Instruct"
java -jar target/quarkus-mcp-1.0.0-SNAPSHOT.jar
```

## Testing

### Unit Tests

```bash
./mvnw test
```

### HTTP Endpoints

The application exposes HTTP endpoints for easier access from outside the container:

- `GET /mcp/health` - Health check endpoint
- `POST /mcp/initialize?id=<id>` - Initialize MCP connection
- `POST /mcp/tools/list?id=<id>` - List available tools
- `POST /mcp/tools/call?id=<id>` - Call a tool (requires JSON body)
- `POST /mcp/ping?id=<id>` - Ping endpoint

**Example HTTP requests:**

```bash
# Health check
curl http://localhost:8080/mcp/health

# Initialize
curl -X POST http://localhost:8080/mcp/initialize?id=1 \
  -H "Content-Type: application/json"

# List tools
curl -X POST http://localhost:8080/mcp/tools/list?id=2 \
  -H "Content-Type: application/json"

# Call tool
curl -X POST http://localhost:8080/mcp/tools/call?id=3 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "draft_finnish",
    "arguments": {
      "brief": "Test task",
      "key_points": ["Point 1", "Point 2"]
    }
  }'
```

### SSE Streaming Interface

The application implements the MCP protocol over Server-Sent Events (SSE):

```bash
# Terminal 1: connect to the SSE stream
curl -N http://localhost:8080/mcp/stream?clientId=demo

# Terminal 2: send an MCP request
curl -X POST http://localhost:8080/mcp/request?clientId=demo \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "req-1",
    "method": "tools/list",
    "params": {}
  }'

# The response appears in Terminal 1 as a streamed SSE event
```

You can send additional JSON-RPC requests (initialize, tools/call, ping) through the `/mcp/request` endpoint and receive responses in real time via the SSE stream.

#### Ping

```bash
echo '{"jsonrpc":"2.0","id":4,"method":"ping","params":{}}' | java -jar target/quarkus-mcp-1.0.0-SNAPSHOT.jar
```

## Tool: draft_finnish

### Description
Compose high-quality Finnish text from given notes.

### Input Schema

```json
{
  "type": "object",
  "properties": {
    "brief": {
      "type": "string",
      "description": "Short task summary in English or Finnish"
    },
    "key_points": {
      "type": "array",
      "items": {
        "type": "string"
      },
      "description": "Bullet points to cover"
    },
    "tone": {
      "type": "string",
      "enum": ["neutral", "friendly", "professional", "persuasive", "concise"],
      "default": "professional"
    },
    "length": {
      "type": "string",
      "enum": ["short", "medium", "long"],
      "default": "medium"
    },
    "audience": {
      "type": "string",
      "description": "Intended audience (e.g., executives, developers, general)"
    }
  },
  "required": ["brief", "key_points"],
  "additionalProperties": false
}
```

### Response Format

```json
{
  "jsonrpc": "2.0",
  "id": 3,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "<Generated Finnish text>"
      }
    ],
    "isError": false
  }
}
```

## Docker

### JVM Image

```bash
docker build -t quarkus-mcp:latest .
docker run -e VLLM_BASE_URL=http://vllm:8000 \
           -e PORO2_MODEL_NAME="LumiOpen/Llama-Poro-2-70B-Instruct" \
           -i quarkus-mcp:latest
```

### Native Image

```bash
docker build -f Dockerfile.native -t quarkus-mcp:native .
docker run -e VLLM_BASE_URL=http://vllm:8000 \
           -e PORO2_MODEL_NAME="LumiOpen/Llama-Poro-2-70B-Instruct" \
           -i quarkus-mcp:native
```

## OpenShift/Kubernetes

### Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: quarkus-mcp
spec:
  replicas: 1
  selector:
    matchLabels:
      app: quarkus-mcp
  template:
    metadata:
      labels:
        app: quarkus-mcp
    spec:
      containers:
      - name: quarkus-mcp
        image: quarkus-mcp:latest
        env:
        - name: VLLM_BASE_URL
          valueFrom:
            configMapKeyRef:
              name: quarkus-mcp-config
              key: vllm-base-url
        - name: PORO2_MODEL_NAME
          valueFrom:
            configMapKeyRef:
              name: quarkus-mcp-config
              key: poro2-model-name
        - name: VLLM_API_KEY
          valueFrom:
            secretKeyRef:
              name: quarkus-mcp-secret
              key: api-key
        stdin: true
        tty: true
```

### ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: quarkus-mcp-config
data:
  vllm-base-url: "http://vllm:8000"
  poro2-model-name: "LumiOpen/Llama-Poro-2-70B-Instruct"
```

### Secret

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: quarkus-mcp-secret
type: Opaque
stringData:
  api-key: "your-api-key-here"
```

## Architecture

- **MainCommand**: Entry point that reads JSON-RPC from stdin and writes to stdout
- **McpService**: Handles MCP protocol methods (initialize, tools/list, tools/call, ping)
- **DraftFinnishTool**: Tool implementation that builds prompts and calls vLLM API
- **OpenAiClientService**: Service wrapper for the OpenAI-compatible REST client
- **OpenAiClient**: Quarkus REST client interface for vLLM API

## Logging

All logs are written to stderr to avoid corrupting the JSON-RPC stream on stdout. Configure logging in `application.properties` or via system properties.

## Error Handling

The server implements proper JSON-RPC 2.0 error codes:
- `-32700`: Parse error
- `-32600`: Invalid request
- `-32601`: Method not found
- `-32602`: Invalid params
- `-32603`: Internal error

## License

Apache License 2.0

