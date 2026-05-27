from typing import Protocol

from app.common.messaging.events import AiPrivateReplyCommand, BotMessageCommand


class BotReplyService(Protocol):
    def create_reply(
        self,
        command: BotMessageCommand,
    ) -> AiPrivateReplyCommand | None:
        ...
