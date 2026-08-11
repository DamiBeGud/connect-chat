from typing import Any, Protocol


class McpClient(Protocol):
    def call_tool(self, name: str, arguments: dict[str, Any]) -> dict[str, Any]:
        ...
