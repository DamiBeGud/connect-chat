import re
import sys

LINK_PATTERN = re.compile(r"\[([^\]]+)\]\(([^)\s]+)(?:\s+\"[^\"]*\")?\)")
AUTOLINK_PATTERN = re.compile(r"<(https?://[^>]+)>")
INLINE_CODE_PATTERN = re.compile(r"`([^`\n]+)`")
HEADING_PATTERN = re.compile(r"^\s{0,3}#{1,6}\s+")
BLOCKQUOTE_PATTERN = re.compile(r"^\s{0,3}>\s?")
CHECKBOX_PATTERN = re.compile(r"^\s*[-*+]\s+\[[ xX]\]\s+")
UNORDERED_LIST_PATTERN = re.compile(r"^\s*[-*+]\s+")
ORDERED_LIST_PATTERN = re.compile(r"^\s*\d+[.)]\s+")
HORIZONTAL_RULE_PATTERN = re.compile(r"^\s*([-*_])(?:\s*\1){2,}\s*$")
TABLE_SEPARATOR_PATTERN = re.compile(r"^\s*\|?\s*:?-{3,}:?\s*(\|\s*:?-{3,}:?\s*)+\|?\s*$")


def clean_llm_message(raw: str) -> str:
    if raw is None:
        return ""

    text = raw.replace("\r\n", "\n").replace("\r", "\n")
    text = _replace_links(text)

    cleaned_lines: list[str] = []
    in_fenced_code = False
    table_rows: list[list[str]] = []

    for line in text.split("\n"):
        stripped = line.strip()

        if stripped.startswith("```") or stripped.startswith("~~~"):
            in_fenced_code = not in_fenced_code
            _flush_table(table_rows, cleaned_lines)
            continue

        if in_fenced_code:
            _flush_table(table_rows, cleaned_lines)
            cleaned_lines.append(line.rstrip())
            continue

        if HORIZONTAL_RULE_PATTERN.match(line) or TABLE_SEPARATOR_PATTERN.match(line):
            continue

        table_cells = _parse_table_row(line)
        if table_cells is not None:
            table_rows.append(table_cells)
            continue

        _flush_table(table_rows, cleaned_lines)
        cleaned_lines.append(_clean_line(line))

    _flush_table(table_rows, cleaned_lines)
    return _normalize_blank_lines(cleaned_lines)


def _replace_links(text: str) -> str:
    text = LINK_PATTERN.sub(lambda match: _format_link(match.group(1), match.group(2)), text)
    return AUTOLINK_PATTERN.sub(r"\1", text)


def _format_link(label: str, url: str) -> str:
    label = label.strip()
    url = url.strip()
    if not label or label == url:
        return url
    return f"{label}: {url}"


def _clean_line(line: str) -> str:
    line = line.rstrip()
    line = BLOCKQUOTE_PATTERN.sub("", line)
    line = HEADING_PATTERN.sub("", line)
    line = CHECKBOX_PATTERN.sub("", line)
    line = UNORDERED_LIST_PATTERN.sub("", line)
    line = ORDERED_LIST_PATTERN.sub("", line)
    line = INLINE_CODE_PATTERN.sub(r"\1", line)
    line = line.replace("`", "")
    line = line.replace("**", "")
    line = line.replace("__", "")
    line = _strip_single_emphasis_markers(line)
    return re.sub(r"[ \t]+", " ", line).strip()


def _strip_single_emphasis_markers(line: str) -> str:
    chars: list[str] = []
    for index, char in enumerate(line):
        if char not in "*_":
            chars.append(char)
            continue

        previous_char = line[index - 1] if index > 0 else ""
        next_char = line[index + 1] if index + 1 < len(line) else ""
        if previous_char.isalnum() and next_char.isalnum():
            chars.append(char)

    return "".join(chars)


def _parse_table_row(line: str) -> list[str] | None:
    stripped = line.strip()
    if "|" not in stripped:
        return None

    cells = [cell.strip() for cell in stripped.strip("|").split("|")]
    if len(cells) < 2 or any(cell == "" for cell in cells):
        return None

    return [_clean_line(cell) for cell in cells]


def _flush_table(rows: list[list[str]], output: list[str]) -> None:
    if not rows:
        return

    if output and output[-1] != "":
        output.append("")

    for row in rows:
        if len(row) == 2:
            output.append(f"{row[0]}: {row[1]}")
        else:
            output.append(": ".join([row[0], " | ".join(row[1:])]))

    rows.clear()


def _normalize_blank_lines(lines: list[str]) -> str:
    normalized: list[str] = []
    previous_blank = True

    for line in lines:
        clean_line = line.rstrip()
        is_blank = clean_line == ""

        if is_blank:
            if not previous_blank:
                normalized.append("")
            previous_blank = True
            continue

        normalized.append(clean_line)
        previous_blank = False

    while normalized and normalized[-1] == "":
        normalized.pop()

    return "\n".join(normalized)


def main() -> None:
    sys.stdout.write(clean_llm_message(sys.stdin.read()))


if __name__ == "__main__":
    main()
