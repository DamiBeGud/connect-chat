# AI Service

Python service that powers the Connect Chat AI bot.

<img src="../.images/Screenshot%202026-05-27%20at%2023.06.00.png" alt="AI chat bot conversation" width="320">

## Responsibilities

- Consume bot inbox commands from RabbitMQ.
- Format chat context into model prompts.
- Call Google Gemini through `google-genai`.
- Optionally call RideAndPark tools through the local MCP adapter.
- Clean model output into chat-friendly plain text.
- Publish AI private reply commands back to RabbitMQ for `chat-service`.
- Expose a lightweight health endpoint.

## Technology

- Python
- FastAPI
- Uvicorn
- RabbitMQ via `pika`
- Google GenAI SDK
- HTTP MCP tool calls via `httpx`

## Dependencies

- RabbitMQ
- `chat-service` event contracts
- Google API key for model calls
- Optional `ride-and-park-mcp-server`
- Optional external RideAndPark API from [RideAndPark/RideAndPark](https://github.com/RideAndPark/RideAndPark)

## Configuration

The main environment variables are:

```text
AI_BOT_USER_ID=00000000-0000-0000-0000-000000000001
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
BOT_INBOX_EXCHANGE=chat.bot-inbox.exchange
BOT_INBOX_ROUTING_KEY=chat.bot-inbox
BOT_INBOX_QUEUE=ai-service.bot-inbox.commands
AI_REPLY_EXCHANGE=chat.ai-reply.exchange
AI_REPLY_ROUTING_KEY=chat.ai-reply
AI_REPLY_MAX_CHARS=16000
GOOGLE_API_KEY=
GOOGLE_MODEL=gemini-2.5-flash
RIDE_AND_PARK_MCP_URL=http://ride-and-park-mcp-server:8080
AI_TOOL_TIMEOUT_SECONDS=10
AI_PARKING_RESULT_LIMIT=5
AI_ENABLE_RIDE_AND_PARK_TOOLS=true
```

## Endpoints

- `GET /health`

The service's primary runtime behavior is RabbitMQ consumption, not HTTP request handling.

## Run Locally

Install dependencies:

```bash
cd ai-service
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

Run the app:

```bash
uvicorn app.main:app --reload
```

When running through Docker Compose, the service starts with the shared RabbitMQ and RideAndPark MCP configuration from the root `docker-compose.yml`. The RideAndPark application itself is a separate repository and must be run separately if those tools are enabled.

## Test

```bash
cd ai-service
pytest
```

## LLM Response Formatting

AI replies are passed through `clean_llm_message(raw)` before they are published back to chat-service. The formatter is pure Python and does not call another model.

Cleanup rules:

- Removes common Markdown syntax such as headings, emphasis markers, blockquotes, list markers, checkboxes, inline backticks, fenced-code markers, and horizontal rules.
- Keeps fenced-code contents while removing only the fences.
- Converts Markdown links to `text: url` and keeps plain URLs.
- Converts simple pipe tables into readable `Label: Value` lines.
- Trims trailing spaces and collapses excessive blank lines.

Tradeoffs:

- This is intentionally heuristic, not a full Markdown parser.
- It preserves meaning for common chat-style LLM output, but unusual Markdown can still produce approximate plain text.
- Single `_` or `*` characters inside words are preserved to avoid damaging identifiers.

CLI usage:

```bash
python -m app.common.text.llm_message_formatter < raw-response.txt
```

## Notes

- The AI bot identity is seeded by `identity-service`.
- The service publishes replies back to `chat-service`; it does not write chat messages directly.
- RideAndPark application code lives in [RideAndPark/RideAndPark](https://github.com/RideAndPark/RideAndPark), contributed by [@lukasp1209](https://github.com/lukasp1209), [@RafaelSwitala](https://github.com/RafaelSwitala), and [@Semineytor4](https://github.com/Semineytor4).
- Additional MCP-integrated service repositories are TODO placeholders until finalized.
