from typing import Any

from fastapi import FastAPI
from mcp.server.fastmcp import FastMCP
from pydantic import BaseModel, Field

from app.config import Settings
from app.ride_and_park_client import RideAndParkClient
from app.tools import (
    call_tool,
    find_parkings_near_destination,
    geocode_destination,
    get_parking_by_id,
    list_parkings,
    tool_definitions,
)

settings = Settings()
client = RideAndParkClient(
    settings.ride_and_park_api_base_url,
    settings.ride_and_park_request_timeout_seconds,
)
app = FastAPI(title="RideAndPark MCP Server")
mcp_server = FastMCP("ride-and-park")


@mcp_server.tool(name="geocode_destination")
def mcp_geocode_destination(q: str) -> dict[str, Any]:
    return geocode_destination(client, q)


@mcp_server.tool(name="find_parkings_near_destination")
def mcp_find_parkings_near_destination(
    destination: str,
    radius_km: float = 5,
    only_open: bool = True,
    limit: int = 5,
) -> dict[str, Any]:
    return find_parkings_near_destination(client, destination, radius_km, only_open, limit)


@mcp_server.tool(name="list_parkings")
def mcp_list_parkings(
    name: str | None = None,
    only_open: bool | None = None,
    realtime_data: bool | None = None,
    target_lat: float | None = None,
    target_lng: float | None = None,
    radius_km: float | None = None,
    limit: int | None = None,
) -> dict[str, Any]:
    return list_parkings(client, name, only_open, realtime_data, target_lat, target_lng, radius_km, limit)


@mcp_server.tool(name="get_parking_by_id")
def mcp_get_parking_by_id(id: str) -> dict[str, Any]:
    return get_parking_by_id(client, id)


class ToolRequest(BaseModel):
    arguments: dict[str, Any] = Field(default_factory=dict)


class JsonRpcRequest(BaseModel):
    jsonrpc: str = "2.0"
    id: str | int | None = None
    method: str
    params: dict[str, Any] = Field(default_factory=dict)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok", "service": "ride-and-park-mcp-server"}


@app.post("/tools/{tool_name}")
def invoke_tool(tool_name: str, request: ToolRequest) -> dict[str, Any]:
    return call_tool(client, tool_name, request.arguments)


@app.post("/mcp")
def mcp(request: JsonRpcRequest) -> dict[str, Any]:
    if request.method == "tools/list":
        return {"jsonrpc": "2.0", "id": request.id, "result": {"tools": tool_definitions()}}

    if request.method == "tools/call":
        name = str(request.params.get("name", ""))
        arguments = request.params.get("arguments")
        if not isinstance(arguments, dict):
            arguments = {}
        result = call_tool(client, name, arguments)
        return {"jsonrpc": "2.0", "id": request.id, "result": {"content": [{"type": "json", "json": result}]}}

    return {
        "jsonrpc": "2.0",
        "id": request.id,
        "error": {"code": -32601, "message": f"Unsupported MCP method: {request.method}"},
    }


app.mount("/mcp-protocol", mcp_server.streamable_http_app())
