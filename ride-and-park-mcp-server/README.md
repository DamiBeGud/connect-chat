# RideAndPark MCP Server

HTTP MCP adapter for the RideAndPark backend.

## Configuration

```text
RIDE_AND_PARK_API_BASE_URL=http://ride-and-park-backend:3000/api
RIDE_AND_PARK_REQUEST_TIMEOUT_SECONDS=10
DEFAULT_PARKING_RADIUS_KM=5
DEFAULT_PARKING_LIMIT=5
```

## Endpoints

- `GET /health`
- `POST /mcp` with JSON-RPC methods `tools/list` and `tools/call`
- `/mcp-protocol` streamable HTTP transport backed by the Python MCP SDK
- `POST /tools/{tool_name}` as a simple local adapter for tests/manual checks

Mutating backend operations such as cache refresh are intentionally not exposed.
