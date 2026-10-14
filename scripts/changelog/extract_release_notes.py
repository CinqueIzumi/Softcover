#!/usr/bin/env python3
"""
Extract one release's notes from the repo-root CHANGELOG.md.

    python3 scripts/changelog/extract_release_notes.py v3.2.0
    python3 scripts/changelog/extract_release_notes.py 3.2.0-rc1 --output release-notes.md

CHANGELOG.md is the single source for release notes: the release workflow publishes this
script's output as the GitHub Release body, and the in-app changelog reads the same file.

An entry is `## X.Y.Z: Headline`, then a `*YYYY-MM-DD*` line, then an optional lede and
bullets up to the next `## ` heading. A tag or version is accepted; a leading `v` and any
`-...` / `+...` suffix are stripped. The output is the headline in bold, then the body
without the date line. Exits non-zero if the entry, its headline or its date is missing.
"""

import argparse
import datetime
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).parent.parent.parent
CHANGELOG = ROOT / "CHANGELOG.md"

DATE_LINE = re.compile(r"^\*(\d{4}-\d{2}-\d{2})\*$")


def normalise_version(raw: str) -> str:
    return re.split(r"[-+]", re.sub(r"^[vV]", "", raw.strip()), maxsplit=1)[0]


def fail(message: str) -> None:
    print(f"error: {message}", file=sys.stderr)
    sys.exit(1)


def extract(text: str, version: str) -> str:
    prefix = f"## {version}:"
    lines = text.splitlines()
    start = next((i for i, line in enumerate(lines) if line.startswith(prefix)), None)
    if start is None:
        fail(f"no '## {version}: <headline>' entry in the changelog")

    headline = lines[start][len(prefix):].strip()
    if not headline:
        fail(f"entry {version} has an empty headline")

    end = next(
        (i for i in range(start + 1, len(lines)) if lines[i].startswith("## ")),
        len(lines),
    )
    body = lines[start + 1:end]

    date_index = next((i for i, line in enumerate(body) if line.strip()), None)
    match = DATE_LINE.match(body[date_index].strip()) if date_index is not None else None
    if match is None:
        fail(f"entry {version} must have a *YYYY-MM-DD* date line right after its heading")
    try:
        datetime.date.fromisoformat(match.group(1))
    except ValueError:
        fail(f"entry {version} has an invalid date '{match.group(1)}'")

    rest = "\n".join(body[date_index + 1:]).strip()
    return f"**{headline}**\n\n{rest}\n" if rest else f"**{headline}**\n"


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument("version", help="a tag or version, e.g. v3.2.0 or 3.2.0-rc1")
    parser.add_argument("--output", help="write to this file instead of stdout")
    parser.add_argument("--changelog", default=str(CHANGELOG), help=argparse.SUPPRESS)
    args = parser.parse_args()

    path = pathlib.Path(args.changelog)
    if not path.is_file():
        fail(f"{path} not found")
    notes = extract(path.read_text(encoding="utf-8"), normalise_version(args.version))

    if args.output:
        pathlib.Path(args.output).write_text(notes, encoding="utf-8")
    else:
        sys.stdout.write(notes)


if __name__ == "__main__":
    main()
