from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    ride_and_park_api_base_url: str = "http://ride-and-park-backend:3000/api"
    ride_and_park_request_timeout_seconds: float = 10
    default_parking_radius_km: float = 5
    default_parking_limit: int = 5
