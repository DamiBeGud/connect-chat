import logging

from app.common.messaging.events import AiPrivateReplyCommand, BotMessageCommand
from app.common.text.llm_message_formatter import clean_llm_message
from app.config.settings import Settings
from app.service.ai_client import AiClient

logger = logging.getLogger(__name__)


class BotReplyServiceImpl:
    def __init__(self, settings: Settings, ai_client: AiClient):
        self.settings = settings
        self.ai_client = ai_client

    def create_reply(
        self,
        command: BotMessageCommand,
    ) -> AiPrivateReplyCommand | None:
        if command.botUserId != self.settings.ai_bot_user_id:
            logger.warning(
                "Ignoring bot command for unexpected botUserId=%s",
                command.botUserId,
            )
            return None

        if not command.content.strip():
            answer = "Please send a message for me to respond to."
        else:
            answer = self.ai_client.generate(command.content)

        answer = clean_llm_message(answer)
        answer = self._cap_reply(answer)

        return AiPrivateReplyCommand(
            senderId=command.botUserId,
            recipientId=command.senderId,
            content=answer,
        )

    def _cap_reply(self, answer: str) -> str:
        max_chars = self.settings.ai_reply_max_chars
        if len(answer) <= max_chars:
            return answer

        logger.warning(
            "Truncating AI reply from %s to %s characters",
            len(answer),
            max_chars,
        )
        return answer[:max_chars]
