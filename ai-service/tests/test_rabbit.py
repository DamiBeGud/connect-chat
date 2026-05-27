from datetime import datetime, timezone
from uuid import UUID, uuid4

from app.common.messaging.events import BotMessageCommand
from app.common.messaging.rabbit_bot_consumer import RabbitBotConsumer
from app.config.settings import Settings
from app.service.implementation.bot_reply_service_impl import BotReplyServiceImpl

BOT_USER_ID = UUID("00000000-0000-0000-0000-000000000001")


class FakeGoogleAi:
    def __init__(self, answer="AI answer"):
        self.answer = answer
        self.messages = []

    def generate(self, user_message: str) -> str:
        self.messages.append(user_message)
        return self.answer


class FakeChannel:
    def __init__(self):
        self.acked = []
        self.nacked = []

    def basic_ack(self, delivery_tag):
        self.acked.append(delivery_tag)

    def basic_nack(self, delivery_tag, requeue):
        self.nacked.append((delivery_tag, requeue))


class CapturingConsumer(RabbitBotConsumer):
    def __init__(self, settings, google_ai):
        super().__init__(settings, google_ai)
        self.published = []

    def publish_reply(self, reply):
        self.published.append(reply)


def settings(bot_user_id=BOT_USER_ID):
    return Settings(ai_bot_user_id=bot_user_id)


def bot_command(bot_user_id=BOT_USER_ID):
    return BotMessageCommand(
        messageId=uuid4(),
        senderId=uuid4(),
        botUserId=bot_user_id,
        content="hello bot",
        occurredAt=datetime.now(timezone.utc),
    )


def test_valid_bot_message_command_produces_ai_reply_command():
    google_ai = FakeGoogleAi("hello human")
    service = BotReplyServiceImpl(settings(), google_ai)
    command = bot_command()

    reply = service.create_reply(command)

    assert reply is not None
    assert reply.senderId == BOT_USER_ID
    assert reply.recipientId == command.senderId
    assert reply.content == "hello human"
    assert google_ai.messages == ["hello bot"]


def test_wrong_bot_user_id_is_ignored_without_calling_google():
    google_ai = FakeGoogleAi()
    service = BotReplyServiceImpl(settings(), google_ai)
    command = bot_command(bot_user_id=uuid4())

    reply = service.create_reply(command)

    assert reply is None
    assert google_ai.messages == []


def test_ai_reply_content_is_capped_to_configured_limit():
    google_ai = FakeGoogleAi("x" * 20)
    service = BotReplyServiceImpl(
        Settings(ai_bot_user_id=BOT_USER_ID, ai_reply_max_chars=16),
        google_ai,
    )
    command = bot_command()

    reply = service.create_reply(command)

    assert reply is not None
    assert reply.content == "x" * 16
    assert len(reply.content) == 16


def test_invalid_payload_is_acked_and_not_published():
    service = BotReplyServiceImpl(settings(), FakeGoogleAi())
    consumer = CapturingConsumer(settings(), service)
    channel = FakeChannel()

    consumer.handle_delivery(channel, 7, b"not-json")

    assert channel.acked == [7]
    assert channel.nacked == []
    assert consumer.published == []


def test_valid_payload_is_published_and_acked():
    service = BotReplyServiceImpl(settings(), FakeGoogleAi("answer"))
    consumer = CapturingConsumer(settings(), service)
    channel = FakeChannel()
    command = bot_command()

    consumer.handle_delivery(
        channel,
        8,
        command.model_dump_json().encode("utf-8"),
    )

    assert channel.acked == [8]
    assert channel.nacked == []
    assert len(consumer.published) == 1
    assert consumer.published[0].recipientId == command.senderId
