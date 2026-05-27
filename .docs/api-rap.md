# RideAndPark Backend API

This document describes the current API exposed by `ride-and-park-backend` and how requests move through the code.

## Service Overview

- Runtime: Node.js with Express
- Entry point: `ride-and-park-backend/src/server.js`
- App setup: `ride-and-park-backend/src/app.js`
- Base path for all backend routes: `/api`
- Response format: plain JSON objects, not an envelope

The backend serves two kinds of data:

- Parking data from an external parking API
- Destination coordinates from a geocoding API

If the external parking API fails and `ALLOW_FALLBACK_DATA=true`, the service falls back to local seed data from `ride-and-park-backend/src/config/db.js`.

## Route Map

All routes are defined in `ride-and-park-backend/src/routes/parkingRoutes.js`.

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/health` | Liveness check |
| `GET` | `/api/geocode` | Resolve a destination string to coordinates |
| `GET` | `/api/statistics` | Aggregate parking counts by status |
| `GET` | `/api/parkings` | List parkings with optional filters |
| `GET` | `/api/parkings/:id` | Return one parking by id |
| `POST` | `/api/parkings/refresh` | Force refresh the parking cache |

## Common Behavior

### CORS

`app.js` enables CORS for `process.env.CORS_ORIGIN` or `*` if unset.

### JSON parsing

The app uses `express.json()`.

### Error responses

The error middleware returns:

```json
{
  "error": "Error message"
}
```

Validation errors raised by controllers set `statusCode=400`. Unknown failures default to `500`.

### 404 behavior

Unknown routes return:

```json
{
  "error": "Route not found"
}
```

## Data Model

Parking records are normalized into this shape by `ride-and-park-backend/src/utils/transformer.js`:

```json
{
  "id": "rp-101",
  "name": "Parkhaus Hauptbahnhof",
  "lat": 48.1353,
  "lng": 11.5584,
  "free": 184,
  "total": 420,
  "openingHours": null,
  "occupancyRate": 56.19,
  "status": "open",
  "realtimeData": true,
  "source": "external",
  "updatedAt": "2026-05-27T10:00:00.000Z"
}
```

`status` is normalized to one of:

- `open`
- `limited`
- `full`
- `unknown`

## Endpoints

### `GET /api/health`

Returns a simple service health payload.

Example response:

```json
{
  "status": "ok",
  "service": "rideandpark-backend",
  "timestamp": "2026-05-27T10:00:00.000Z"
}
```

### `GET /api/geocode?q=<query>`

Uses `ride-and-park-backend/src/services/geocodingService.js` to query Nominatim or `GEOCODING_API_URL`.

Required query parameters:

| Name | Type | Notes |
| --- | --- | --- |
| `q` | string | Required, trimmed, must not be empty |

Success response:

```json
{
  "lat": 48.7784,
  "lng": 9.18,
  "label": "Stuttgart Hauptbahnhof"
}
```

Behavior:

- Returns only the first geocoding hit
- Returns `404` with `{ "error": "No matching destination found." }` when no result exists
- Returns `400` when `q` is missing or empty
- Returns `504` when the geocoding request times out

### `GET /api/parkings`

Returns normalized parking records from cache, external API, or fallback seed data.

Supported query parameters:

| Name | Type | Notes |
| --- | --- | --- |
| `name` | string | Case-insensitive substring match on parking name |
| `source_uid` | string | Exact match against normalized `source`, case-insensitive |
| `realtimeData` | boolean | Accepts `true/false`, `1/0`, `yes/no` |
| `onlyOpen` | boolean | If `true`, only `status === "open"` |
| `target_lat` | number | Must be provided together with `target_lng` |
| `target_lng` | number | Must be provided together with `target_lat` |
| `radius_km` | number | Positive number, defaults to `5` when target coordinates are present |

Validation rules:

- `target_lat` and `target_lng` must appear together
- numeric params must parse as finite numbers
- boolean params must be valid booleans
- `radius_km` must be greater than `0`

Example:

```http
GET /api/parkings?name=central&realtimeData=true&onlyOpen=true&target_lat=48.1374&target_lng=11.5755&radius_km=5
```

Success response:

```json
{
  "data": [
    {
      "id": "ext-1",
      "name": "Central Garage",
      "lat": 48.13,
      "lng": 11.57,
      "free": 50,
      "total": 100,
      "openingHours": "24/7",
      "occupancyRate": 50,
      "status": "open",
      "realtimeData": true,
      "source": "external",
      "updatedAt": "2026-05-27T10:00:00.000Z"
    }
  ],
  "meta": {
    "source": "external",
    "count": 1,
    "loadedAt": "2026-05-27T10:00:00.000Z",
    "filters": {
      "name": "central",
      "realtimeData": true,
      "onlyOpen": true,
      "target_lat": 48.1374,
      "target_lng": 11.5755,
      "radius_km": 5
    },
    "warning": null
  }
}
```

When no filters are active, the shape is slightly different:

- `data` contains the full cached list
- `meta` contains `source`, `count`, `loadedAt`, and `warning`
- `meta.filters` is omitted

### `GET /api/parkings/:id`

Returns one normalized parking.

Success response:

```json
{
  "data": {
    "id": "ext-9",
    "name": "Direct Lookup",
    "lat": 48.12,
    "lng": 11.52,
    "free": 11,
    "total": 22,
    "openingHours": null,
    "occupancyRate": 50,
    "status": "open",
    "realtimeData": true,
    "source": "external",
    "updatedAt": "2026-05-27T10:00:00.000Z"
  },
  "meta": {
    "source": "external",
    "loadedAt": "2026-05-27T10:00:00.000Z"
  }
}
```

Lookup behavior:

1. Try direct lookup against the external API using `fetchParkingById(id)`.
2. Transform the returned record.
3. If that fails and fallback is allowed, search the cached dataset.
4. Return `404` with `{ "error": "Parking not found" }` if nothing matches.

### `POST /api/parkings/refresh`

Forces a refresh of the parking cache from the external API. If the external API fails and fallback is allowed, the cache is repopulated from seed data.

Response shape matches `GET /api/parkings` without filters:

```json
{
  "data": [],
  "meta": {
    "source": "external",
    "count": 0,
    "loadedAt": "2026-05-27T10:00:00.000Z",
    "warning": null
  }
}
```

### `GET /api/statistics`

Returns an aggregate count by normalized parking status.

Example response:

```json
{
  "data": {
    "total": 3,
    "open": 1,
    "limited": 1,
    "full": 1,
    "unknown": 0
  },
  "meta": {
    "source": "external",
    "loadedAt": "2026-05-27T10:00:00.000Z",
    "warning": null
  }
}
```

## Request Flow

### 1. Router

`src/routes/parkingRoutes.js` maps all `/api` paths to controller functions.

### 2. Controller

`src/controllers/parkingController.js` is responsible for:

- reading query and path params
- parsing booleans and numbers
- rejecting invalid input with `400`
- calling the relevant service
- mapping `null` results to `404`

### 3. Service layer

`src/services/parkingService.js` handles:

- cache lifecycle
- external API fetches
- fallback to seed data
- filtering
- statistics
- direct parking lookup

`src/services/geocodingService.js` handles destination lookup.

### 4. Transformer

`src/utils/transformer.js` converts inconsistent upstream payloads into one stable parking model.

It can extract fields from many alternate names, including:

- `id`, `uid`, `parking_id`, `parkingSiteId`
- `lat`, `latitude`, `geoLat`
- `lng`, `longitude`, `lon`, `geoLng`
- `free_slots`, `vacantSpaces`, `parkingNumberOfVacantSpaces`
- `total_slots`, `capacity`, `parkingNumberOfSpaces`

It also:

- computes `free` from `total` and occupied counts when needed
- computes `occupancyRate` if it can be derived
- normalizes free-form source statuses into `open`, `limited`, `full`, or `unknown`
- marks `realtimeData` based on explicit flags or live-looking fields

## Caching And Fallback

Parking data is cached in memory in module state.

Current behavior:

- default TTL: `600000` ms (`10` minutes)
- configurable with `PARKING_CACHE_TTL_MS`
- concurrent requests share a single in-flight load via `pendingLoad`
- `POST /api/parkings/refresh` bypasses TTL and reloads immediately

Fallback behavior:

- if external fetch fails and `ALLOW_FALLBACK_DATA=true`, seed data is transformed and cached
- the response `meta.warning` explains the fallback reason
- if fallback is disabled, the external error is propagated

## External Integrations

### Parking API

`src/services/apiService.js` defaults to:

```text
https://api.mobidata-bw.de/park-api/api/public/v3/parking-sites
```

Behavior:

- uses `GET`
- sets `Accept: application/json`
- times out after `PARKING_API_TIMEOUT_MS` or `20000` ms by default
- retries once with double timeout if the first request aborts

### Geocoding API

`src/services/geocodingService.js` defaults to:

```text
https://nominatim.openstreetmap.org/search
```

Behavior:

- sends `q`, `format=jsonv2`, and `limit=1`
- sets `Accept: application/json`
- sets `User-Agent` from `GEOCODING_USER_AGENT` or `RideAndPark/1.0`
- uses a `10000` ms timeout

## Environment Variables

| Name | Default | Purpose |
| --- | --- | --- |
| `PORT` | `3000` | HTTP listen port |
| `CORS_ORIGIN` | `*` | Allowed frontend origin |
| `PARKING_API_URL` | MobiData BW endpoint | Base URL for external parking API |
| `PARKING_API_TIMEOUT_MS` | `20000` | Timeout for external parking API |
| `PARKING_CACHE_TTL_MS` | `600000` | In-memory parking cache TTL |
| `ALLOW_FALLBACK_DATA` | `true` | Enables seed-data fallback |
| `GEOCODING_API_URL` | Nominatim search endpoint | Base URL for geocoding |
| `GEOCODING_USER_AGENT` | `RideAndPark/1.0` | User-Agent header for geocoding |

## Notable Implementation Details

- `GET /api/parkings` and `POST /api/parkings/refresh` return slightly different `meta` payloads depending on whether filters are active.
- `GET /api/parkings/:id` does not use the cached list first; it tries the external detail endpoint before falling back to cached data.
- Filtering is applied after normalization, so all filter logic works on the internal parking model, not raw upstream payloads.
- Radius filtering uses a haversine distance calculation and defaults to `5 km` only when target coordinates are supplied.
