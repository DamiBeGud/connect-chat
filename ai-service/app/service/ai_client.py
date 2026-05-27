from typing import Protocol


class AiClient(Protocol):
    def generate(self, user_message: str) -> str:
        ...
