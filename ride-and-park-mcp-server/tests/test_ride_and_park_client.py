import httpx

from app.ride_and_park_client import RideAndParkClient


def client_for(handler):
    return RideAndParkClient(
        "http://ride-and-park.test/api",
        timeout_seconds=1,
        transport=httpx.MockTransport(handler),
    )


def test_geocode_success():
    def handler(request):
        assert request.url.path == "/api/geocode"
        assert request.url.params["q"] == "Stuttgart Hbf"
        return httpx.Response(200, json={"lat": 48.7784, "lng": 9.18, "label": "Stuttgart Hauptbahnhof"})

    result = client_for(handler).geocode("Stuttgart Hbf")

    assert result["label"] == "Stuttgart Hauptbahnhof"


def test_geocode_404_maps_to_destination_not_found():
    def handler(request):
        return httpx.Response(404, json={"error": "No matching destination found."})

    result = client_for(handler).geocode("nowhere")

    assert result["error"]["type"] == "destination_not_found"


def test_backend_timeout_maps_to_backend_timeout():
    def handler(request):
        raise httpx.ReadTimeout("timeout")

    result = client_for(handler).list_parkings()

    assert result["error"]["type"] == "backend_timeout"


def test_backend_500_maps_to_backend_unavailable():
    def handler(request):
        return httpx.Response(500, json={"error": "boom"})

    result = client_for(handler).list_parkings()

    assert result["error"]["type"] == "backend_unavailable"
