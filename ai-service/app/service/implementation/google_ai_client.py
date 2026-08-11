from google import genai
from google.genai import errors

from app.service.mcp_client import McpClient
from app.service.parking_response_formatter import (
    ParkingResponseFormatter,
    extract_destination,
    is_parking_prompt,
)

FALLBACK_RESPONSE = "I could not generate a response right now."


class GoogleAiClient:
    def __init__(
        self,
        api_key: str,
        model: str,
        client=None,
        ride_and_park_mcp_client: McpClient | None = None,
        enable_ride_and_park_tools: bool = True,
        parking_result_limit: int = 5,
        parking_formatter: ParkingResponseFormatter | None = None,
    ):
        self.client = client or (genai.Client(api_key=api_key) if api_key else None)
        self.model = model
        self.ride_and_park_mcp_client = ride_and_park_mcp_client
        self.enable_ride_and_park_tools = enable_ride_and_park_tools
        self.parking_result_limit = parking_result_limit
        self.parking_formatter = parking_formatter or ParkingResponseFormatter()

    def generate(self, user_message: str) -> str:
        if self._should_use_parking_tools(user_message):
            return self._generate_parking_reply(user_message)

        if self.client is None:
            raise RuntimeError("GOOGLE_API_KEY is required")

        try:
            response = self.client.models.generate_content(
                model=self.model,
                contents=user_message,
            )
        except errors.ClientError as exc:
            if exc.code in {400, 401, 403, 404}:
                return FALLBACK_RESPONSE
            raise

        return response.text or FALLBACK_RESPONSE

    def _should_use_parking_tools(self, user_message: str) -> bool:
        return (
            self.enable_ride_and_park_tools
            and self.ride_and_park_mcp_client is not None
            and is_parking_prompt(user_message)
        )

    def _generate_parking_reply(self, user_message: str) -> str:
        destination = extract_destination(user_message)
        if not destination:
            return "Please include a destination so I can search nearby parking options."

        result = self.ride_and_park_mcp_client.call_tool(
            "find_parkings_near_destination",
            {
                "destination": destination,
                "radius_km": 5,
                "only_open": True,
                "limit": self.parking_result_limit,
            },
        )
        return self.parking_formatter.format(result)
