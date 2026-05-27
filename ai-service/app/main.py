from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.api.health_controller import router as health_router
from app.common.messaging.rabbit_bot_consumer import RabbitBotConsumer
from app.config.settings import Settings
from app.service.implementation.bot_reply_service_impl import BotReplyServiceImpl
from app.service.implementation.google_ai_client import GoogleAiClient

settings = Settings()
google_ai_client = GoogleAiClient(settings.google_api_key, settings.google_model)
bot_reply_service = BotReplyServiceImpl(settings, google_ai_client)
rabbit_consumer = RabbitBotConsumer(settings, bot_reply_service)


@asynccontextmanager
async def lifespan(app: FastAPI):
    rabbit_consumer.start()
    yield
    rabbit_consumer.stop()


app = FastAPI(lifespan=lifespan)
app.include_router(health_router)
