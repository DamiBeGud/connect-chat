import httpx

from app.ride_and_park_client import RideAndParkClient
from app.tools import find_parkings_near_destination, list_parkings


def client_for(handler):
    return RideAndParkClient(
        "http://ride-and-park.test/api",
        timeout_seconds=1,
        transport=httpx.MockTransport(handler),
    )


def test_parking_search_success_applies_limit_sorts_and_calculates_distance():
    def handler(request):
        if request.url.path == "/api/geocode":
            return httpx.Response(200, json={"lat": 48.0, "lng": 9.0, "label": "Target"})
        assert request.url.path == "/api/parkings"
        return httpx.Response(
            200,
            json={
                "data": [
                    {
                        "id": "closed",
                        "name": "Closed",
                        "lat": 48.0,
                        "lng": 9.01,
                        "free": 500,
                        "total": 500,
                        "occupancyRate": 0,
                        "status": "full",
                        "realtimeData": True,
                    },
                    {
                        "id": "best",
                        "name": "Best",
                        "lat": 48.0,
                        "lng": 9.02,
                        "free": 40,
                        "total": 100,
                        "occupancyRate": 60,
                        "status": "open",
                        "realtimeData": True,
                    },
                    {
                        "id": "second",
                        "name": "Second",
                        "free": 20,
                        "total": 100,
                        "occupancyRate": 20,
                        "status": "open",
                        "realtimeData": False,
                    },
                ],
                "meta": {"source": "external", "loadedAt": "2026-05-27T10:00:00.000Z", "warning": None},
            },
        )

    result = find_parkings_near_destination(client_for(handler), "Target", limit=2)

    assert [parking["id"] for parking in result["parkings"]] == ["best", "second"]
    assert result["meta"]["count"] == 2
    assert result["parkings"][0]["distanceKm"] > 0
    assert "distanceKm" not in result["parkings"][1]


def test_parking_search_invalid_radius_or_limit_is_rejected():
    client = client_for(lambda request: httpx.Response(500))

    assert find_parkings_near_destination(client, "Target", radius_km=0)["error"]["type"] == "invalid_tool_input"
    assert find_parkings_near_destination(client, "Target", limit=0)["error"]["type"] == "invalid_tool_input"


def test_list_parkings_applies_limit():
    def handler(request):
        return httpx.Response(
            200,
            json={
                "data": [{"id": "one"}, {"id": "two"}, {"id": "three"}],
                "meta": {"source": "external", "count": 3},
            },
        )

    result = list_parkings(client_for(handler), limit=2)

    assert [parking["id"] for parking in result["parkings"]] == ["one", "two"]
    assert result["meta"]["count"] == 2
