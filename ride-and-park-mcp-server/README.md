# RideAndPark MCP Server

Python HTTP MCP adapter that exposes read-only tools for the external RideAndPark API to AI clients.

## Responsibilities

- Connect to the external RideAndPark API.
- Expose MCP `tools/list` and `tools/call` behavior over HTTP.
- Provide streamable HTTP MCP transport through the Python MCP SDK.
- Provide simple `/tools/{tool_name}` endpoints for local checks and tests.
- Keep backend mutation operations, such as cache refresh, out of the AI tool surface.

## Technology

- Python
- FastAPI
- Uvicorn
- Python MCP SDK
- httpx
- pydantic-settings

## Local Port

`8080` when run from Docker Compose.

## Configuration

```text
RIDE_AND_PARK_API_BASE_URL=http://localhost:3000/api
RIDE_AND_PARK_REQUEST_TIMEOUT_SECONDS=10
DEFAULT_PARKING_RADIUS_KM=5
DEFAULT_PARKING_LIMIT=5
```

When this adapter runs in Docker Compose from the Connect Chat repository, the default is `http://host.docker.internal:3000/api` so the container can reach a RideAndPark backend running on the host.

## Endpoints

- `GET /health`
- `POST /mcp` with JSON-RPC methods `tools/list` and `tools/call`
- `/mcp-protocol` streamable HTTP transport backed by the Python MCP SDK
- `POST /tools/{tool_name}` as a simple local adapter for tests/manual checks

## Run Locally

Install dependencies:

```bash
cd ride-and-park-mcp-server
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

Run the app:

```bash
uvicorn app.main:app --reload --port 8080
```

Make sure the external RideAndPark API is running and `RIDE_AND_PARK_API_BASE_URL` points to it.

## Test

```bash
cd ride-and-park-mcp-server
pytest
```

## Notes

- This repository contains only the MCP adapter for RideAndPark, not the RideAndPark application itself.
- RideAndPark application code lives in [RideAndPark/RideAndPark](https://github.com/RideAndPark/RideAndPark), contributed by [@lukasp1209](https://github.com/lukasp1209), [@RafaelSwitala](https://github.com/RafaelSwitala), and [@Semineytor4](https://github.com/Semineytor4).
- Additional MCP-integrated service repositories are TODO placeholders until the integration boundaries are finalized.
