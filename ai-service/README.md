# AI Service

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
