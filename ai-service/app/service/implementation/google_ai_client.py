from google import genai
from google.genai import errors

FALLBACK_RESPONSE = "I could not generate a response right now."


class GoogleAiClient:
    def __init__(self, api_key: str, model: str, client=None):
        self.client = client or (genai.Client(api_key=api_key) if api_key else None)
        self.model = model

    def generate(self, user_message: str) -> str:
        if self.client is None:
            raise RuntimeError("GOOGLE_API_KEY is required")

        try:
            response = self.client.models.generate_content(
                model=self.model,
                contents=user_message,
            )
        except errors.ClientError as exc:
            if exc.code in {400, 401, 403, 404}:
                return FALLBACK_RESPONSE
            raise

        return response.text or FALLBACK_RESPONSE
