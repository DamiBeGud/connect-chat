from uuid import UUID

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    ai_bot_user_id: UUID = UUID("00000000-0000-0000-0000-000000000001")

    rabbitmq_host: str = "rabbitmq"
    rabbitmq_port: int = 5672
    rabbitmq_username: str = "guest"
    rabbitmq_password: str = "guest"

    bot_inbox_exchange: str = "chat.bot-inbox.exchange"
    bot_inbox_routing_key: str = "chat.bot-inbox"
    bot_inbox_queue: str = "ai-service.bot-inbox.commands"

    ai_reply_exchange: str = "chat.ai-reply.exchange"
    ai_reply_routing_key: str = "chat.ai-reply"
    ai_reply_max_chars: int = 16_000

    google_api_key: str = ""
    google_model: str = "gemini-2.5-flash"

    ride_and_park_mcp_url: str = "http://ride-and-park-mcp-server:8080"
    ai_tool_timeout_seconds: float = 10
    ai_parking_result_limit: int = 5
    ai_enable_ride_and_park_tools: bool = True
