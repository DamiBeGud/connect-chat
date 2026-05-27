from typing import Any

import httpx


def error_payload(error_type: str, message: str) -> dict[str, Any]:
    return {"error": {"type": error_type, "message": message}}


class RideAndParkClient:
    def __init__(
        self,
        base_url: str,
        timeout_seconds: float = 10,
        transport: httpx.BaseTransport | None = None,
    ):
        self.base_url = base_url.rstrip("/")
        self.client = httpx.Client(
            base_url=self.base_url,
            timeout=timeout_seconds,
            transport=transport,
        )

    def close(self) -> None:
        self.client.close()

    def geocode(self, query: str) -> dict[str, Any]:
        query = query.strip()
        if not query:
            return error_payload("invalid_tool_input", "Destination query is required.")
        return self._get("/geocode", params={"q": query}, not_found_type="destination_not_found")

    def list_parkings(self, params: dict[str, Any] | None = None) -> dict[str, Any]:
        return self._get("/parkings", params=self._clean_params(params or {}))

    def get_parking_by_id(self, parking_id: str) -> dict[str, Any]:
        parking_id = parking_id.strip()
        if not parking_id:
            return error_payload("invalid_tool_input", "Parking id is required.")
        return self._get(f"/parkings/{parking_id}", not_found_type="parking_not_found")

    def _get(
        self,
        path: str,
        params: dict[str, Any] | None = None,
        not_found_type: str = "backend_unavailable",
    ) -> dict[str, Any]:
        try:
            response = self.client.get(path, params=params)
        except httpx.TimeoutException:
            return error_payload("backend_timeout", "RideAndPark backend timed out.")
        except httpx.HTTPError:
            return error_payload("backend_unavailable", "RideAndPark backend is unavailable.")

        if response.status_code == 404:
            message = self._error_message(response) or "Requested resource was not found."
            return error_payload(not_found_type, message)
        if response.status_code == 504:
            return error_payload("backend_timeout", "RideAndPark backend timed out.")
        if response.status_code >= 500:
            return error_payload("backend_unavailable", "RideAndPark backend is unavailable.")
        if response.status_code >= 400:
            return error_payload("invalid_tool_input", self._error_message(response) or "Invalid request.")

        try:
            payload = response.json()
        except ValueError:
            return error_payload("unexpected_backend_response", "RideAndPark backend returned invalid JSON.")

        if not isinstance(payload, dict):
            return error_payload("unexpected_backend_response", "RideAndPark backend returned an unexpected response.")
        return payload

    def _error_message(self, response: httpx.Response) -> str | None:
        try:
            payload = response.json()
        except ValueError:
            return None
        if isinstance(payload, dict) and isinstance(payload.get("error"), str):
            return payload["error"]
        return None

    def _clean_params(self, params: dict[str, Any]) -> dict[str, Any]:
        return {
            key: value
            for key, value in params.items()
            if value is not None and value != ""
        }
