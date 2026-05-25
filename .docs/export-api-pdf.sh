#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INPUT="${1:-"$SCRIPT_DIR/api.md"}"
OUTPUT="${2:-"${INPUT%.*}.pdf"}"

if [[ ! -f "$INPUT" ]]; then
  echo "Input file not found: $INPUT" >&2
  exit 1
fi

if command -v pandoc >/dev/null 2>&1; then
  if pandoc "$INPUT" -o "$OUTPUT" --toc --metadata title="Connect Chat API Documentation"; then
    echo "Wrote $OUTPUT"
    exit 0
  fi

  echo "pandoc failed; falling back to built-in PDF renderer." >&2
fi

python3 - "$INPUT" "$OUTPUT" <<'PY'
import re
import sys
import textwrap
from pathlib import Path

input_path = Path(sys.argv[1])
output_path = Path(sys.argv[2])

PAGE_WIDTH = 595
PAGE_HEIGHT = 842
MARGIN = 46
MAX_WIDTH = PAGE_WIDTH - (2 * MARGIN)


def clean_inline(text):
    text = re.sub(r"\[([^\]]+)\]\(([^)]+)\)", r"\1 (\2)", text)
    text = text.replace("**", "").replace("__", "")
    text = text.replace("`", "")
    return text


def wrap_text(text, font_size, code=False):
    chars = max(30, int(MAX_WIDTH / (font_size * (0.46 if code else 0.50))))
    return textwrap.wrap(text, width=chars, replace_whitespace=False) or [""]


def markdown_to_items(markdown):
    items = []
    in_code = False

    for raw_line in markdown.splitlines():
        line = raw_line.rstrip()

        if line.startswith("```"):
            in_code = not in_code
            items.append(("blank", ""))
            continue

        if in_code:
            items.append(("code", line))
            continue

        if not line.strip():
            items.append(("blank", ""))
            continue

        heading = re.match(r"^(#{1,4})\s+(.*)$", line)
        if heading:
            level = len(heading.group(1))
            kind = "h1" if level == 1 else "h2" if level == 2 else "h3"
            items.append((kind, clean_inline(heading.group(2))))
            continue

        if "|" in line:
            stripped = line.strip()
            if re.fullmatch(r"[\s|:\-]+", stripped):
                continue
            cells = [clean_inline(cell.strip()) for cell in stripped.strip("|").split("|")]
            items.append(("code", " | ".join(cells)))
            continue

        if line.lstrip().startswith(("- ", "* ")):
            text = "- " + clean_inline(line.lstrip()[2:])
            items.append(("normal", text))
            continue

        items.append(("normal", clean_inline(line)))

    return items


def pdf_escape(text):
    data = text.encode("cp1252", errors="replace")
    escaped = []
    for byte in data:
        char = chr(byte)
        if char in ("\\", "(", ")"):
            escaped.append("\\" + char)
        elif byte < 32 or byte > 126:
            escaped.append(f"\\{byte:03o}")
        else:
            escaped.append(char)
    return "".join(escaped)


def text_command(x, y, font, size, text):
    return f"BT /{font} {size} Tf {x:.2f} {y:.2f} Td ({pdf_escape(text)}) Tj ET\n"


def item_style(kind):
    if kind == "h1":
        return "F2", 20, 28
    if kind == "h2":
        return "F2", 16, 23
    if kind == "h3":
        return "F2", 13, 19
    if kind == "code":
        return "F3", 8.5, 12
    return "F1", 10.5, 15


def render_pages(items):
    pages = []
    current = []
    y = PAGE_HEIGHT - MARGIN

    for kind, text in items:
        if kind == "blank":
            y -= 9
            if y < MARGIN:
                pages.append("".join(current))
                current = []
                y = PAGE_HEIGHT - MARGIN
            continue

        font, size, line_height = item_style(kind)
        lines = wrap_text(text, size, code=(kind == "code"))

        if kind in ("h1", "h2", "h3"):
            y -= 5

        for line in lines:
            if y < MARGIN + line_height:
                pages.append("".join(current))
                current = []
                y = PAGE_HEIGHT - MARGIN
            current.append(text_command(MARGIN, y, font, size, line))
            y -= line_height

        if kind in ("h1", "h2"):
            y -= 5

    if current:
        pages.append("".join(current))
    return pages


def add_object(objects, body):
    objects.append(body)
    return len(objects)


def write_pdf(pages, output):
    objects = [None, None]
    font_regular = add_object(objects, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>")
    font_bold = add_object(objects, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>")
    font_mono = add_object(objects, "<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>")

    page_ids = []
    for page_stream in pages:
        stream_bytes = page_stream.encode("latin-1")
        content_id = add_object(
            objects,
            f"<< /Length {len(stream_bytes)} >>\nstream\n{page_stream}endstream",
        )
        page_id = add_object(
            objects,
            "<< /Type /Page "
            "/Parent 2 0 R "
            f"/MediaBox [0 0 {PAGE_WIDTH} {PAGE_HEIGHT}] "
            f"/Resources << /Font << /F1 {font_regular} 0 R /F2 {font_bold} 0 R /F3 {font_mono} 0 R >> >> "
            f"/Contents {content_id} 0 R >>",
        )
        page_ids.append(page_id)

    objects[0] = "<< /Type /Catalog /Pages 2 0 R >>"
    kids = " ".join(f"{page_id} 0 R" for page_id in page_ids)
    objects[1] = f"<< /Type /Pages /Kids [{kids}] /Count {len(page_ids)} >>"

    pdf = bytearray(b"%PDF-1.4\n")
    offsets = [0]
    for index, body in enumerate(objects, start=1):
        offsets.append(len(pdf))
        pdf.extend(f"{index} 0 obj\n{body}\nendobj\n".encode("latin-1"))

    xref_offset = len(pdf)
    pdf.extend(f"xref\n0 {len(objects) + 1}\n".encode("latin-1"))
    pdf.extend(b"0000000000 65535 f \n")
    for offset in offsets[1:]:
        pdf.extend(f"{offset:010d} 00000 n \n".encode("latin-1"))
    pdf.extend(
        f"trailer\n<< /Size {len(objects) + 1} /Root 1 0 R >>\nstartxref\n{xref_offset}\n%%EOF\n".encode("latin-1")
    )

    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_bytes(pdf)


markdown = input_path.read_text(encoding="utf-8")
items = markdown_to_items(markdown)
pages = render_pages(items)
write_pdf(pages, output_path)
print(f"Wrote {output_path}")
PY
