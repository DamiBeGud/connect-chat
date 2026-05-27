from google.genai.errors import ClientError

from app.service.implementation.google_ai_client import (
    FALLBACK_RESPONSE,
    GoogleAiClient,
)


class EmptyTextResponse:
    text = None


class FakeModels:
    def generate_content(self, model, contents):
        return EmptyTextResponse()


class FakeClient:
    models = FakeModels()


class ClientErrorModels:
    def generate_content(self, model, contents):
        raise ClientError(
            404,
            {
                "error": {
                    "code": 404,
                    "message": "model not found",
                    "status": "NOT_FOUND",
                }
            },
        )


class ClientErrorFakeClient:
    models = ClientErrorModels()


def test_google_client_returns_fallback_when_response_text_is_empty():
    client = GoogleAiClient("api-key", "gemini-test", client=FakeClient())

    assert client.generate("hello") == FALLBACK_RESPONSE


def test_google_client_returns_fallback_for_non_retryable_client_error():
    client = GoogleAiClient(
        "api-key",
        "missing-model",
        client=ClientErrorFakeClient(),
    )

    assert client.generate("hello") == FALLBACK_RESPONSE
