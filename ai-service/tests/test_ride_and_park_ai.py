from datetime import datetime, timezone
from uuid import UUID, uuid4

from app.common.messaging.events import BotMessageCommand
from app.config.settings import Settings
from app.service.implementation.bot_reply_service_impl import BotReplyServiceImpl
from app.service.implementation.google_ai_client import GoogleAiClient
from app.service.parking_response_formatter import extract_destination

BOT_USER_ID = UUID("00000000-0000-0000-0000-000000000001")


class TextResponse:
    text = "normal Gemini answer"


class FakeModels:
    def __init__(self):
        self.calls = []

    def generate_content(self, model, contents):
        self.calls.append((model, contents))
        return TextResponse()


class FakeGeminiClient:
    def __init__(self):
        self.models = FakeModels()


class FakeMcpClient:
    def __init__(self, result):
        self.result = result
        self.calls = []

    def call_tool(self, name, arguments):
        self.calls.append((name, arguments))
        return self.result


def parking_result(parkings):
    return {
        "destination": {"label": "Stuttgart Hauptbahnhof"},
        "parkings": parkings,
        "meta": {"warning": None},
    }


def bot_command(content):
    return BotMessageCommand(
        messageId=uuid4(),
        senderId=uuid4(),
        botUserId=BOT_USER_ID,
        content=content,
        occurredAt=datetime.now(timezone.utc),
    )


def test_non_parking_prompts_still_call_normal_gemini_generation():
    gemini = FakeGeminiClient()
    mcp = FakeMcpClient(parking_result([]))
    client = GoogleAiClient(
        "api-key",
        "gemini-test",
        client=gemini,
        ride_and_park_mcp_client=mcp,
    )

    assert client.generate("write a short greeting") == "normal Gemini answer"
    assert gemini.models.calls == [("gemini-test", "write a short greeting")]
    assert mcp.calls == []


def test_parking_prompt_triggers_ride_and_park_tool_path():
    gemini = FakeGeminiClient()
    mcp = FakeMcpClient(
        parking_result(
            [
                {
                    "name": "Parkhaus Hauptbahnhof",
                    "lat": 48.7842663,
                    "lng": 9.1821173,
                    "free": 184,
                    "total": 420,
                    "occupancyRate": 56,
                    "status": "open",
                    "updatedAt": "2026-05-27T10:00:00.000Z",
                }
            ]
        )
    )
    client = GoogleAiClient(
        "",
        "gemini-test",
        client=gemini,
        ride_and_park_mcp_client=mcp,
        parking_result_limit=3,
    )

    reply = client.generate("Find me free parking spots near Stuttgart hauptbahnhof")

    assert gemini.models.calls == []
    assert mcp.calls == [
        (
            "find_parkings_near_destination",
            {
                "destination": "Stuttgart hauptbahnhof",
                "radius_km": 5,
                "only_open": True,
                "limit": 3,
            },
        )
    ]
    assert "Parkhaus Hauptbahnhof" in reply
    assert "Free spaces: 184 of 420" in reply
    assert "Status: open" in reply
    assert "Google Maps: https://www.google.com/maps/dir/?api=1&destination=48.7842663,9.1821173" in reply
    assert "Apple Maps: https://maps.apple.com/?daddr=48.7842663,9.1821173" in reply


def test_parking_reply_omits_directions_when_coordinates_are_missing():
    client = GoogleAiClient(
        "",
        "gemini-test",
        ride_and_park_mcp_client=FakeMcpClient(
            parking_result(
                [
                    {
                        "name": "No Coordinates Garage",
                        "free": 10,
                        "total": 20,
                        "status": "open",
                    }
                ]
            )
        ),
    )

    reply = client.generate("parking near Stuttgart")

    assert "No Coordinates Garage" in reply
    assert "Google Maps:" not in reply
    assert "Apple Maps:" not in reply


def test_extract_destination_cleans_near_to_the_prefix_and_hbf_abbreviation():
    assert (
        extract_destination("Can you find parking spaces near to the Frankfurt hauptbahnhof?")
        == "Frankfurt hauptbahnhof"
    )
    assert (
        extract_destination("Can you find parking spaces near to the Frankfurt am Main HBF?")
        == "Frankfurt am Main Hauptbahnhof"
    )


def test_empty_parking_list_becomes_clear_no_results_reply():
    client = GoogleAiClient(
        "",
        "gemini-test",
        ride_and_park_mcp_client=FakeMcpClient(parking_result([])),
    )

    reply = client.generate("parking near Stuttgart")

    assert reply == "I could not find open parking options near Stuttgart Hauptbahnhof."


def test_mcp_failure_becomes_graceful_fallback_reply():
    client = GoogleAiClient(
        "",
        "gemini-test",
        ride_and_park_mcp_client=FakeMcpClient(
            {"error": {"type": "backend_unavailable", "message": "down"}}
        ),
    )

    reply = client.generate("parking near Stuttgart")

    assert reply == "Parking data is temporarily unavailable. Please try again later."


def test_parking_reply_is_capped_by_ai_reply_max_chars():
    mcp = FakeMcpClient(
        parking_result(
            [
                {
                    "name": "Very Long Parking Name",
                    "free": 100,
                    "total": 200,
                    "status": "open",
                }
            ]
        )
    )
    ai_client = GoogleAiClient("", "gemini-test", ride_and_park_mcp_client=mcp)
    service = BotReplyServiceImpl(
        Settings(ai_bot_user_id=BOT_USER_ID, ai_reply_max_chars=30),
        ai_client,
    )

    reply = service.create_reply(bot_command("parking near Stuttgart"))

    assert reply is not None
    assert len(reply.content) == 30
