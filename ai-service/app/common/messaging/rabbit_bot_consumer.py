import logging
import threading
import time

import pika
from pydantic import ValidationError

from app.common.messaging.events import AiPrivateReplyCommand, BotMessageCommand
from app.config.settings import Settings
from app.service.bot_reply_service import BotReplyService

logger = logging.getLogger(__name__)


class RabbitBotConsumer:
    def __init__(self, settings: Settings, bot_reply_service: BotReplyService):
        self.settings = settings
        self.bot_reply_service = bot_reply_service
        self.connection = None
        self.channel = None
        self.thread: threading.Thread | None = None
        self._stop_event = threading.Event()

    def start(self) -> None:
        if self.thread and self.thread.is_alive():
            return

        self.thread = threading.Thread(target=self.run_forever, daemon=True)
        self.thread.start()

    def stop(self) -> None:
        self._stop_event.set()
        try:
            if self.channel and self.channel.is_open:
                self.channel.stop_consuming()
        except Exception:
            logger.exception("Failed to stop RabbitMQ consuming")

        try:
            if self.connection and self.connection.is_open:
                self.connection.close()
        except Exception:
            logger.exception("Failed to close RabbitMQ connection")

    def run_forever(self) -> None:
        while not self._stop_event.is_set():
            try:
                self._connect()
                logger.info(
                    "Consuming AI bot commands from queue=%s",
                    self.settings.bot_inbox_queue,
                )
                self.channel.start_consuming()
            except Exception:
                if self._stop_event.is_set():
                    break
                logger.exception("RabbitMQ consumer failed; retrying")
                time.sleep(5)
            finally:
                self._close_connection()

    def _connect(self) -> None:
        credentials = pika.PlainCredentials(
            self.settings.rabbitmq_username,
            self.settings.rabbitmq_password,
        )
        parameters = pika.ConnectionParameters(
            host=self.settings.rabbitmq_host,
            port=self.settings.rabbitmq_port,
            credentials=credentials,
            heartbeat=30,
            blocked_connection_timeout=30,
        )
        self.connection = pika.BlockingConnection(parameters)
        self.channel = self.connection.channel()

        self.channel.exchange_declare(
            exchange=self.settings.bot_inbox_exchange,
            exchange_type="direct",
            durable=True,
        )
        self.channel.queue_declare(
            queue=self.settings.bot_inbox_queue,
            durable=True,
        )
        self.channel.queue_bind(
            queue=self.settings.bot_inbox_queue,
            exchange=self.settings.bot_inbox_exchange,
            routing_key=self.settings.bot_inbox_routing_key,
        )
        self.channel.exchange_declare(
            exchange=self.settings.ai_reply_exchange,
            exchange_type="direct",
            durable=True,
        )
        self.channel.basic_qos(prefetch_count=1)
        self.channel.basic_consume(
            queue=self.settings.bot_inbox_queue,
            on_message_callback=self._on_message,
            auto_ack=False,
        )

    def _close_connection(self) -> None:
        try:
            if self.connection and self.connection.is_open:
                self.connection.close()
        except Exception:
            logger.exception("Failed to close RabbitMQ connection")

    def _on_message(self, channel, method, properties, body: bytes) -> None:
        self.handle_delivery(channel, method.delivery_tag, body)

    def handle_delivery(self, channel, delivery_tag: int, body: bytes) -> None:
        try:
            command = BotMessageCommand.model_validate_json(body)
        except (ValidationError, ValueError):
            logger.exception("Invalid BotMessageCommand payload")
            channel.basic_ack(delivery_tag=delivery_tag)
            return

        try:
            reply = self.bot_reply_service.create_reply(command)
            if reply is not None:
                self.publish_reply(reply)
            channel.basic_ack(delivery_tag=delivery_tag)
        except Exception:
            logger.exception("Failed to process BotMessageCommand")
            channel.basic_nack(delivery_tag=delivery_tag, requeue=True)

    def publish_reply(self, reply: AiPrivateReplyCommand) -> None:
        if self.channel is None:
            raise RuntimeError("RabbitMQ channel is not connected")

        self.channel.basic_publish(
            exchange=self.settings.ai_reply_exchange,
            routing_key=self.settings.ai_reply_routing_key,
            body=reply.model_dump_json().encode("utf-8"),
            properties=pika.BasicProperties(
                content_type="application/json",
                delivery_mode=2,
            ),
        )
