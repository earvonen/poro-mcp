# OpenShift Deployment

This directory contains OpenShift manifests for deploying the Quarkus MCP server.

## Files

- `Containerfile` - Container build definition (root directory)
- `buildconfig.yaml` - BuildConfig to build the application image
- `deployment.yaml` - Deployment manifest for the poro-mcp application
- `service.yaml` - Service manifest (optional, for potential health checks)
- `configmap.yaml` - Configuration values
- `secret.yaml` - Secret for API keys (update before deployment)

## Deployment Steps

### 1. Create the ConfigMap and Secret

```bash
oc create -f openshift/configmap.yaml
oc create -f openshift/secret.yaml
```

Edit the secret to set your API key:
```bash
oc edit secret poro-mcp-secret
```

### 2. Build the Application

#### Option A: Using BuildConfig (Recommended)

```bash
# Start a binary build
oc new-build --name=poro-mcp --binary --strategy=docker
oc start-build poro-mcp --from-dir=. --follow
```

Or use the BuildConfig manifest:
```bash
oc create -f openshift/buildconfig.yaml
oc start-build poro-mcp --from-dir=. --follow
```

#### Option B: Build locally and push

```bash
# Build locally
podman build -t poro-mcp:latest -f Containerfile .

# Tag and push to OpenShift registry
oc login
oc project <your-project>
podman tag poro-mcp:latest $(oc get route default-route -n openshift-image-registry --template='{{ .spec.host }}')/<your-project>/poro-mcp:latest
podman push $(oc get route default-route -n openshift-image-registry --template='{{ .spec.host }}')/<your-project>/poro-mcp:latest --tls-verify=false
```

### 3. Deploy the Application

```bash
oc create -f openshift/deployment.yaml
oc create -f openshift/service.yaml
```

### 4. Verify Deployment

```bash
# Check pod status
oc get pods -l app=poro-mcp

# Check logs
oc logs -f deployment/poro-mcp

# Check service
oc get svc poro-mcp
```

## Configuration

Update the ConfigMap values as needed:
- `vllm-base-url`: Base URL of your vLLM server
- `poro2-model-name`: Model name
- `default-temperature`: Temperature for text generation
- `default-max-tokens`: Maximum tokens to generate

## Using the MCP Server

The application runs as a stdio-based MCP server. To interact with it:

```bash
# Get pod name
POD_NAME=$(oc get pod -l app=poro-mcp -o jsonpath='{.items[0].metadata.name}')

# Execute command in pod with stdin
oc exec -it $POD_NAME -- sh -c 'echo "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}" | java -jar /app/quarkus-run.jar'
```

## Notes

- The service is optional since the MCP server communicates via stdio
- Adjust resource limits in `deployment.yaml` based on your needs
- The application requires `stdin: true` and `tty: true` for stdio communication
- Ensure the vLLM server is accessible from the pod (same namespace or proper service)

