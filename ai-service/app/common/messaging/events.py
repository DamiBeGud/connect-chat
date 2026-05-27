from datetime import datetime
from uuid import UUID

from pydantic import BaseModel


class BotMessageCommand(BaseModel):
    messageId: UUID
    senderId: UUID
    botUserId: UUID
    content: str
    occurredAt: datetime


class AiPrivateReplyCommand(BaseModel):
    senderId: UUID
    recipientId: UUID
    content: str
