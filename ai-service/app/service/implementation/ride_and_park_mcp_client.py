from typing import Any

import httpx


class RideAndParkMcpClient:
    def __init__(
        self,
        base_url: str,
        timeout_seconds: float = 10,
        client: httpx.Client | None = None,
    ):
        self.base_url = base_url.rstrip("/")
        self.client = client or httpx.Client(base_url=self.base_url, timeout=timeout_seconds)

    def call_tool(self, name: str, arguments: dict[str, Any]) -> dict[str, Any]:
        payload = {
            "jsonrpc": "2.0",
            "id": "ride-and-park-tool-call",
            "method": "tools/call",
            "params": {"name": name, "arguments": arguments},
        }
        try:
            response = self.client.post("/mcp", json=payload)
            response.raise_for_status()
            body = response.json()
        except httpx.TimeoutException:
            return self._error("backend_timeout", "Parking data is temporarily unavailable.")
        except (httpx.HTTPError, ValueError):
            return self._error("backend_unavailable", "Parking data is temporarily unavailable.")

        if isinstance(body, dict) and body.get("error"):
            return self._error("backend_unavailable", "Parking data is temporarily unavailable.")

        content = ((body.get("result") or {}).get("content") or []) if isinstance(body, dict) else []
        if content and isinstance(content[0], dict) and isinstance(content[0].get("json"), dict):
            return content[0]["json"]

        return self._error("unexpected_backend_response", "Parking data is temporarily unavailable.")

    def _error(self, error_type: str, message: str) -> dict[str, Any]:
        return {"error": {"type": error_type, "message": message}}
