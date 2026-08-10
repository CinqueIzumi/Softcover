#!/usr/bin/env python3
"""
Generate the public ROADMAP.md from GitHub milestones.

    python3 scripts/roadmap/generate_roadmap.py            # print to stdout
    python3 scripts/roadmap/generate_roadmap.py --write    # write ROADMAP.md
    python3 scripts/roadmap/generate_roadmap.py --check    # exit 1 if ROADMAP.md is stale (CI)

ROADMAP.md is a BUILD OUTPUT. Do not hand-edit it — edit the milestone description on
GitHub and re-run this. That is the whole point: the in-app Roadmap screen (D.11) fetches
this file raw at runtime, so a milestone edit reaches users with no app release, and there
is no second copy of the plan to drift.

Sources:
  - each OPEN milestone's `description` field  -> one release section, ordered by version
  - `versionName` in app/build.gradle.kts      -> the "Current release" line
  - header.md next to this script              -> the static intro and caveats

Every section, including "Under consideration", is a milestone description. Nothing about
the plan is hardcoded here.

Reads only public data, so it needs no token (a token just raises the rate limit).
"""

import argparse
import json
import pathlib
import re
import sys
import urllib.request
import os

REPO = "CinqueIzumi/Softcover"
HERE = pathlib.Path(__file__).parent
ROOT = HERE.parent.parent
HEADER = HERE / "header.md"
TARGET = ROOT / "ROADMAP.md"
GRADLE = ROOT / "app" / "build.gradle.kts"

ALLOW_PARTIAL = False



def version_key(title):
    """'3.10.0' sorts after '3.9.0'. Non-numeric titles sort last."""
    parts = re.findall(r"\d+", title)
    return ([int(p) for p in parts] or [999]) + [0] * (3 - len(parts))


def current_version():
    m = re.search(r'versionName\s*=\s*"([^"]+)"', GRADLE.read_text())
    return m.group(1) if m else "unknown"


def fetch_milestones():
    url = f"https://api.github.com/repos/{REPO}/milestones?state=open&per_page=100"
    req = urllib.request.Request(url)
    req.add_header("Accept", "application/vnd.github+json")
    req.add_header("User-Agent", "softcover-roadmap-generate")
    token = os.environ.get("GH_TOKEN") or os.environ.get("GITHUB_TOKEN")
    if token:
        req.add_header("Authorization", f"Bearer {token}")
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read())


def build():
    milestones = sorted(fetch_milestones(), key=lambda m: version_key(m["title"]))

    described = [m for m in milestones if (m.get("description") or "").strip()]
    if not described:
        print("  ERROR: no open milestone carries a description — nothing to generate.",
              file=sys.stderr)
        sys.exit(1)

    # Guard against gutting the public roadmap. An open milestone with no description is
    # almost always a half-finished import, not a deliberate omission — and since the
    # in-app Roadmap screen fetches this file, generating from a partial set would quietly
    # delete whole releases from what users see.
    missing = [m["title"] for m in milestones if m not in described]
    if missing and not ALLOW_PARTIAL:
        print(f"  ERROR: {len(missing)} open milestone(s) have no description: "
              f"{', '.join(missing)}\n"
              f"         Generating now would drop them from the public roadmap.\n"
              f"         Add their descriptions, or pass --allow-partial if the omission "
              f"is deliberate.", file=sys.stderr)
        sys.exit(1)
    if missing:
        print(f"  warning: --allow-partial, omitting {', '.join(missing)}", file=sys.stderr)

    header = HEADER.read_text().replace("{{CURRENT_VERSION}}", current_version())
    sections = [m["description"].strip() for m in described]

    return header.rstrip() + "\n\n---\n\n" + "\n\n".join(sections) + "\n"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--write", action="store_true", help="write ROADMAP.md")
    ap.add_argument("--check", action="store_true", help="exit 1 if ROADMAP.md is stale")
    ap.add_argument("--allow-partial", action="store_true",
                    help="generate even though some open milestones have no description")
    args = ap.parse_args()

    global ALLOW_PARTIAL
    ALLOW_PARTIAL = args.allow_partial

    out = build()

    if args.check:
        current = TARGET.read_text() if TARGET.exists() else ""
        if current != out:
            print("ROADMAP.md is stale — run: python3 scripts/roadmap/generate_roadmap.py --write",
                  file=sys.stderr)
            sys.exit(1)
        print("ROADMAP.md is up to date.")
        return

    if args.write:
        TARGET.write_text(out)
        print(f"wrote {TARGET.relative_to(ROOT)} ({len(out.splitlines())} lines)")
    else:
        sys.stdout.write(out)


if __name__ == "__main__":
    main()
