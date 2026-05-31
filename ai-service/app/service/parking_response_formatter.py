from datetime import datetime
from typing import Any


def is_parking_prompt(message: str) -> bool:
    normalized = message.lower()
    parking_terms = ("parking", "parkings", "parkhaus", "garage", "free spaces", "parkplätze", "parken")
    return any(term in normalized for term in parking_terms)


def extract_destination(message: str) -> str:
    normalized = message.strip()
    lower = normalized.lower()
    markers = (" near ", " around ", " at ", " close to ", " nearby ", " by ", " in ")
    for marker in markers:
        index = lower.rfind(marker)
        if index >= 0:
            destination = normalized[index + len(marker) :].strip(" .?!")
            if destination:
                return _clean_destination(destination)

    cleaned = normalized
    for phrase in (
        "find me",
        "show me",
        "free",
        "open",
        "parking spots",
        "parking garages",
        "parking",
        "parkings",
        "garages",
        "nearby",
    ):
        cleaned = cleaned.replace(phrase, " ")
        cleaned = cleaned.replace(phrase.title(), " ")
    return _clean_destination(cleaned)


def _clean_destination(destination: str) -> str:
    cleaned = " ".join(destination.strip(" .?!").split())
    lower = cleaned.lower()
    for prefix in ("to the ", "to ", "the "):
        if lower.startswith(prefix):
            cleaned = cleaned[len(prefix) :].strip()
            lower = cleaned.lower()

    words = ["Hauptbahnhof" if word.lower() == "hbf" else word for word in cleaned.split()]
    return " ".join(words)


class ParkingResponseFormatter:
    def format(self, result: dict[str, Any]) -> str:
        error = result.get("error")
        if isinstance(error, dict):
            return self._format_error(error)

        parkings = result.get("parkings")
        if not isinstance(parkings, list) or not parkings:
            destination = self._destination_label(result)
            return f"I could not find open parking options near {destination}."

        destination = self._destination_label(result)
        lines = [f"Here are the best open parking options near {destination}:"]
        for index, parking in enumerate(parkings[:5], start=1):
            if not isinstance(parking, dict):
                continue
            lines.extend(self._parking_lines(index, parking))

        warning = (result.get("meta") or {}).get("warning") if isinstance(result.get("meta"), dict) else None
        if warning:
            lines.append(f"Warning: {warning}")
        lines.append("Availability can change quickly, so check again before driving.")
        return "\n".join(lines)

    def _parking_lines(self, index: int, parking: dict[str, Any]) -> list[str]:
        lines = ["", f"{index}. {parking.get('name') or 'Unnamed parking'}"]
        free = parking.get("free")
        total = parking.get("total")
        if free is not None and total is not None:
            lines.append(f"   Free spaces: {free} of {total}")
        elif free is not None:
            lines.append(f"   Free spaces: {free}")

        occupancy = parking.get("occupancyRate")
        if occupancy is not None:
            lines.append(f"   Occupancy: {self._format_percent(occupancy)}")

        status = parking.get("status")
        if status:
            lines.append(f"   Status: {status}")

        distance = parking.get("distanceKm")
        if distance is not None:
            lines.append(f"   Distance: {distance} km")

        directions_urls = self._directions_urls(parking)
        if directions_urls:
            lines.append(f"   Google Maps: {directions_urls['google']}")
            lines.append(f"   Apple Maps: {directions_urls['apple']}")

        opening_hours = parking.get("openingHours")
        if opening_hours:
            lines.append(f"   Opening hours: {opening_hours}")

        updated_at = self._format_datetime(parking.get("updatedAt"))
        if updated_at:
            lines.append(f"   Updated: {updated_at}")
        return lines

    def _format_error(self, error: dict[str, Any]) -> str:
        error_type = error.get("type")
        if error_type == "destination_not_found":
            return "I could not find that destination in RideAndPark."
        if error_type == "parking_not_found":
            return "I could not find that parking option in RideAndPark."
        if error_type in {"backend_timeout", "backend_unavailable", "unexpected_backend_response"}:
            return "Parking data is temporarily unavailable. Please try again later."
        return "I could not search parking data for that request."

    def _destination_label(self, result: dict[str, Any]) -> str:
        destination = result.get("destination")
        if isinstance(destination, dict):
            return str(destination.get("label") or destination.get("query") or "that destination")
        return "that destination"

    def _format_percent(self, value: Any) -> str:
        try:
            number = float(value)
        except (TypeError, ValueError):
            return str(value)
        if number.is_integer():
            return f"{int(number)}%"
        return f"{number:.1f}%"

    def _format_datetime(self, value: Any) -> str | None:
        if not value:
            return None
        if not isinstance(value, str):
            return str(value)
        try:
            parsed = datetime.fromisoformat(value.replace("Z", "+00:00"))
        except ValueError:
            return value
        return parsed.strftime("%Y-%m-%d %H:%M")

    def _directions_urls(self, parking: dict[str, Any]) -> dict[str, str] | None:
        lat = self._number_or_none(parking.get("lat"))
        lng = self._number_or_none(parking.get("lng"))
        if lat is None or lng is None:
            return None
        return {
            "google": f"https://www.google.com/maps/dir/?api=1&destination={lat},{lng}",
            "apple": f"https://maps.apple.com/?daddr={lat},{lng}",
        }

    def _number_or_none(self, value: Any) -> float | None:
        try:
            return float(value)
        except (TypeError, ValueError):
            return None
