---
name: style-check
description: Auto-fix mechanizable style, then run every verification gate (ktlint, detekt, lint, module graph, dependency analysis, mechanical style checks).
---

Auto-fix the mechanizable style issues, then run the full set of checks — including
the gates `./gradlew check` omits (`buildHealth` and `styleCheck`) — and report.

Steps:

1. Use the Bash tool to run `./gradlew ktlintFormat lintFix` to auto-fix the mechanizable layout and lint issues. This may modify files.
2. Use the Bash tool to run the full gate: `./gradlew check buildHealth styleCheck`. This covers lint, detekt, unit tests, ktlintCheck, checkModuleGraph, and iOS compilation of every KMP module (all via `check`), dependency analysis (`buildHealth`), and the mechanical style script (`styleCheck`). Note: the iOS-compile gate only runs on macOS (Kotlin/Native iOS compilation is unavailable elsewhere), so run release prep on a Mac to exercise it.
3. If the gate fails, surface the failing task(s) and the relevant output to the user. Do not attempt non-mechanical fixes automatically — `check` and `buildHealth` failures are blocking and need a human decision.
4. Report which auto-fixes were applied (mention that `git diff` is available to review them) and the gate result. Treat `styleCheck` findings as advisory; treat `check`/`buildHealth` failures as blocking.

Do not create a commit.
