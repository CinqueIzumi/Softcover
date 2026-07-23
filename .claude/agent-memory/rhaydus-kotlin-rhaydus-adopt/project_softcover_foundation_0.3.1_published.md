---
name: project-softcover-foundation-0.3.1-published
description: Softcover switched off foundation.local back onto published nl.rhaydus 0.3.1 from Maven Central
metadata:
  type: project
---

As of 2026-07-23, Softcover consumes the `nl.rhaydus` foundation as **published `0.3.1`** artifacts from
`mavenCentral()` — `local.properties` now has `foundation.local=false` (the committed default), superseding
the `foundation.local=true` / local `../rhaydus-foundation` includeBuild state recorded in
[[project-softcover-foundation-0.3.0-local]]. All nine `0.3.1` `nl.rhaydus:*` artifacts were verified
resolving from Central. `gradle/libs.versions.toml` (`rhaydusFoundation = "0.3.1"`) and `local.properties`
were already correct going into this pass — the dependency wiring itself needed no change, and the full
gate set (assembleDebug, ktlintCheck, styleCheck/type-resolved detekt, 3235 unit tests, lint, buildHealth,
checkModuleGraph, compileKotlinIosSimulatorArm64) had already passed against 0.3.1.

**Why:** this was a docs-and-references-only refresh pass, not a wiring pass — see
[[feedback-rhaydus-adopt-scope]].

**How to apply:** re-vendored the five foundation conventions docs from the local sibling checkout
`../rhaydus-foundation` (pinned at its `v0.3.1` tag on `main`) into `docs/rhaydus/0.3.1/`, replacing the
retired `docs/rhaydus/0.3.0/` directory. Every live citation of `docs/rhaydus/0.3.0/...` across the repo
(CLAUDE.md's managed block, README.md, `docs/reference/*.md`, `settings.gradle.kts`,
`config/detekt/detekt.yml`, and the path citations inside `.claude/agent-memory/rhaydus-kotlin-code-reviewer/`
and `.claude/agent-memory/rhaydus-kotlin-unit-test-writer/` files) was repointed to `docs/rhaydus/0.3.1/`.
`docs/working/foundation-upstream-candidates.md` and `docs/working/now.md` had their "Softcover runs on
local 0.3.0" framing corrected to "published 0.3.1 / `foundation.local=false`", since the project is no
longer on the local includeBuild. **Historical mentions of `0.3.0` describing a specific past event were
deliberately left alone** (e.g. the `core-ui` → `core-common`/`core-platform` split note, `release/0.3.0`
branch-name references in past code-reviewer batch notes, dated Batch-I/F8/F9/F10 memory entries) — those
are accurate records of what happened at that release, not stale pointers, and rewriting them would be
revisionist.
