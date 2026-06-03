---
name: update-readme
description: Bring README.md's Features and Tech Stack sections up to date with the actual feature modules, libraries, and recent work.
---

Keep `README.md` in sync with what the app actually ships — both the **Features**
section (against the feature modules) and the **Tech Stack** section (against the
version catalog) — so the README never goes stale. README only — do not commit.

Steps:

1. Use the Bash tool to list the directories under `feature/` — this is the source of truth for what features exist.
2. Read the **Features** section of `README.md` (grouped by user-facing area: Library, Reading, Explore, Book Details, Profile, Settings). Use the Bash tool to run `git log` since the last release tag/version to see what changed recently.
3. Compare: find features present in the code (or recent commits) that are missing or described inaccurately in the README.
4. Use the Edit tool to update the Features section. Keep the existing editorial grouping and tone, and add or adjust only what changed — do not rewrite sections that are still accurate. Preserve the heading structure and the Table of Contents.
5. Read `gradle/libs.versions.toml` — the source of truth for libraries and their versions. Compare it against the **Tech Stack** tables in the README. Use the Edit tool to:
   - Correct any version string that has drifted.
   - Add a row for any library that is now in the catalog (and meaningfully part of the stack) but missing from the tables, placing it in the right table (Core, Data & Networking, App Architecture, Testing) and matching the existing row format.
   - Remove rows for libraries that have been dropped from the catalog.
   Keep the tables' existing structure and column layout.
6. Report a short summary of what changed (features + tech stack).

Do not create a commit. Do not modify anything outside `README.md`.
