from app.common.text.llm_message_formatter import clean_llm_message


def test_cleans_headings_bold_and_bullets():
    raw = """
# Plan

- **First** item
- _Second_ item
"""

    assert clean_llm_message(raw) == "Plan\n\nFirst item\nSecond item"


def test_keeps_fenced_code_content_without_fences():
    raw = """
Here is code:

```python
def hello():
    return "world"
```
"""

    assert (
        clean_llm_message(raw)
        == 'Here is code:\n\ndef hello():\n    return "world"'
    )


def test_cleans_inline_code():
    assert clean_llm_message("Use `kubectl get pods` now.") == "Use kubectl get pods now."


def test_converts_markdown_links():
    raw = "Read [the docs](https://example.com/docs) and <https://example.com/raw>."

    assert (
        clean_llm_message(raw)
        == "Read the docs: https://example.com/docs and https://example.com/raw."
    )


def test_converts_simple_tables_to_readable_lines():
    raw = """
Name | John
Age | 30
"""

    assert clean_llm_message(raw) == "Name: John\nAge: 30"


def test_handles_malformed_markdown():
    raw = """
## Broken **Title
> - [x] Done _mostly
1. Step with `partial code
---
"""

    assert clean_llm_message(raw) == "Broken Title\nDone mostly\nStep with partial code"


def test_leaves_already_clean_plain_text_unchanged():
    raw = "Hello there.\n\nThis is already plain text.\nVisit https://example.com."

    assert clean_llm_message(raw) == raw
