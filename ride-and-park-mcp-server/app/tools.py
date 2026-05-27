from math import asin, cos, radians, sin, sqrt
from typing import Any

from app.ride_and_park_client import RideAndParkClient, error_payload


def geocode_destination(client: RideAndParkClient, q: str) -> dict[str, Any]:
    return client.geocode(q)


def find_parkings_near_destination(
    client: RideAndParkClient,
    destination: str,
    radius_km: float = 5,
    only_open: bool = True,
    limit: int = 5,
) -> dict[str, Any]:
    validation_error = _validate_radius_limit(radius_km, limit)
    if validation_error:
        return validation_error

    geocoded = client.geocode(destination)
    if "error" in geocoded:
        return geocoded

    lat = _number_or_none(geocoded.get("lat"))
    lng = _number_or_none(geocoded.get("lng"))
    if lat is None or lng is None:
        return error_payload("unexpected_backend_response", "Destination did not include coordinates.")

    backend_result = client.list_parkings(
        {
            "target_lat": lat,
            "target_lng": lng,
            "radius_km": radius_km,
            "onlyOpen": only_open,
        }
    )
    if "error" in backend_result:
        return backend_result

    parkings = backend_result.get("data")
    if not isinstance(parkings, list):
        return error_payload("unexpected_backend_response", "Parking list response did not include a data array.")

    enriched = [_with_distance(parking, lat, lng) for parking in parkings if isinstance(parking, dict)]
    ranked = sorted(enriched, key=_parking_rank)
    limited = ranked[:limit]
    meta = backend_result.get("meta") if isinstance(backend_result.get("meta"), dict) else {}

    return {
        "destination": {
            "query": destination,
            "label": geocoded.get("label") or destination,
            "lat": lat,
            "lng": lng,
        },
        "parkings": limited,
        "meta": {
            "count": len(limited),
            "source": meta.get("source"),
            "loadedAt": meta.get("loadedAt"),
            "warning": meta.get("warning"),
            "radiusKm": radius_km,
            "onlyOpen": only_open,
        },
    }


def list_parkings(
    client: RideAndParkClient,
    name: str | None = None,
    only_open: bool | None = None,
    realtime_data: bool | None = None,
    target_lat: float | None = None,
    target_lng: float | None = None,
    radius_km: float | None = None,
    limit: int | None = None,
) -> dict[str, Any]:
    if limit is not None and limit <= 0:
        return error_payload("invalid_tool_input", "limit must be greater than 0.")
    if radius_km is not None and radius_km <= 0:
        return error_payload("invalid_tool_input", "radius_km must be greater than 0.")
    if (target_lat is None) != (target_lng is None):
        return error_payload("invalid_tool_input", "target_lat and target_lng must be provided together.")

    result = client.list_parkings(
        {
            "name": name,
            "onlyOpen": only_open,
            "realtimeData": realtime_data,
            "target_lat": target_lat,
            "target_lng": target_lng,
            "radius_km": radius_km,
        }
    )
    if "error" in result:
        return result

    data = result.get("data")
    if not isinstance(data, list):
        return error_payload("unexpected_backend_response", "Parking list response did not include a data array.")

    compact = data[:limit] if limit else data
    return {
        "parkings": compact,
        "meta": {
            **(result.get("meta") if isinstance(result.get("meta"), dict) else {}),
            "count": len(compact),
        },
    }


def get_parking_by_id(client: RideAndParkClient, id: str) -> dict[str, Any]:
    return client.get_parking_by_id(id)


def call_tool(client: RideAndParkClient, name: str, arguments: dict[str, Any]) -> dict[str, Any]:
    try:
        if name == "geocode_destination":
            return geocode_destination(client, q=str(arguments.get("q", "")))
        if name == "find_parkings_near_destination":
            return find_parkings_near_destination(
                client,
                destination=str(arguments.get("destination", "")),
                radius_km=_float_arg(arguments.get("radius_km", 5), "radius_km"),
                only_open=_bool_arg(arguments.get("only_open", True), "only_open"),
                limit=_int_arg(arguments.get("limit", 5), "limit"),
            )
        if name == "list_parkings":
            return list_parkings(
                client,
                name=arguments.get("name"),
                only_open=_optional_bool_arg(arguments.get("only_open"), "only_open"),
                realtime_data=_optional_bool_arg(arguments.get("realtime_data"), "realtime_data"),
                target_lat=_optional_float_arg(arguments.get("target_lat"), "target_lat"),
                target_lng=_optional_float_arg(arguments.get("target_lng"), "target_lng"),
                radius_km=_optional_float_arg(arguments.get("radius_km"), "radius_km"),
                limit=_optional_int_arg(arguments.get("limit"), "limit"),
            )
        if name == "get_parking_by_id":
            return get_parking_by_id(client, id=str(arguments.get("id", "")))
        return error_payload("invalid_tool_input", f"Unknown tool: {name}")
    except ValueError as exc:
        return error_payload("invalid_tool_input", str(exc))


def tool_definitions() -> list[dict[str, Any]]:
    return [
        {"name": "geocode_destination", "description": "Geocode a destination string with RideAndPark."},
        {"name": "find_parkings_near_destination", "description": "Find useful parking options near a destination."},
        {"name": "list_parkings", "description": "List RideAndPark parkings with optional filters."},
        {"name": "get_parking_by_id", "description": "Get one RideAndPark parking by id."},
    ]


def _validate_radius_limit(radius_km: float, limit: int) -> dict[str, Any] | None:
    if radius_km <= 0:
        return error_payload("invalid_tool_input", "radius_km must be greater than 0.")
    if limit <= 0:
        return error_payload("invalid_tool_input", "limit must be greater than 0.")
    return None


def _with_distance(parking: dict[str, Any], target_lat: float, target_lng: float) -> dict[str, Any]:
    lat = _number_or_none(parking.get("lat"))
    lng = _number_or_none(parking.get("lng"))
    if lat is None or lng is None:
        return dict(parking)
    return {**parking, "distanceKm": round(_distance_km(target_lat, target_lng, lat, lng), 2)}


def _distance_km(lat1: float, lng1: float, lat2: float, lng2: float) -> float:
    earth_radius_km = 6371.0
    dlat = radians(lat2 - lat1)
    dlng = radians(lng2 - lng1)
    a = sin(dlat / 2) ** 2 + cos(radians(lat1)) * cos(radians(lat2)) * sin(dlng / 2) ** 2
    return 2 * earth_radius_km * asin(sqrt(a))


def _parking_rank(parking: dict[str, Any]) -> tuple[int, float, float, int]:
    status_rank = 0 if parking.get("status") == "open" else 1
    free_rank = -(_number_or_none(parking.get("free")) or 0)
    occupancy_rank = _number_or_none(parking.get("occupancyRate"))
    if occupancy_rank is None:
        occupancy_rank = 101
    realtime_rank = 0 if parking.get("realtimeData") is True else 1
    return (status_rank, free_rank, occupancy_rank, realtime_rank)


def _number_or_none(value: Any) -> float | None:
    try:
        number = float(value)
    except (TypeError, ValueError):
        return None
    return number


def _float_arg(value: Any, name: str) -> float:
    try:
        return float(value)
    except (TypeError, ValueError):
        raise ValueError(f"{name} must be a valid number.")


def _optional_float_arg(value: Any, name: str) -> float | None:
    if value is None:
        return None
    return _float_arg(value, name)


def _int_arg(value: Any, name: str) -> int:
    try:
        return int(value)
    except (TypeError, ValueError):
        raise ValueError(f"{name} must be a valid integer.")


def _optional_int_arg(value: Any, name: str) -> int | None:
    if value is None:
        return None
    return _int_arg(value, name)


def _bool_arg(value: Any, name: str) -> bool:
    if isinstance(value, bool):
        return value
    normalized = str(value).strip().lower()
    if normalized in {"true", "1", "yes"}:
        return True
    if normalized in {"false", "0", "no"}:
        return False
    raise ValueError(f"{name} must be a valid boolean.")


def _optional_bool_arg(value: Any, name: str) -> bool | None:
    if value is None:
        return None
    return _bool_arg(value, name)
