---
name: style-check
description: Auto-fix mechanizable style, then run every verification gate (ktlint, type-resolved detekt, lint, module graph, dependency analysis).
---

Auto-fix the mechanizable style issues, then run the full set of checks — including
the `buildHealth` gate that `./gradlew check` omits — and report.

Steps:

1. Use the Bash tool to run `./gradlew ktlintFormat lintFix` to auto-fix the mechanizable layout and lint issues. This may modify files.
2. Use the Bash tool to run the full gate: `./gradlew check buildHealth`. This covers lint, `styleCheck` (ktlint plus type-resolved detekt), unit tests, checkModuleGraph, and iOS compilation of every KMP module (all via `check`), plus dependency analysis (`buildHealth`). Note: the iOS-compile gate only runs on macOS (Kotlin/Native iOS compilation is unavailable elsewhere), so run release prep on a Mac to exercise it.
Gradle calls are rewritten to `scripts/gradle-quiet.sh`, which prints a short summary (result, failed tasks, compiler errors, failing tests, style findings) and the path of the full log. Read the summary and grep that log for detail; do not re-run the gate verbose.

3. If the gate fails, surface the failing task(s) and the relevant output to the user. Do not attempt non-mechanical fixes automatically — `check` and `buildHealth` failures are blocking and need a human decision.
4. Report which auto-fixes were applied (mention that `git diff` is available to review them) and the gate result. Treat `check`/`buildHealth` failures, including `styleCheck` findings, as blocking.

Do not create a commit.
