# CLAUDE.md

## Project overview

Softcover is a Kotlin Multiplatform / Compose Multiplatform client for [Hardcover.app](https://hardcover.app/), a
book tracking platform, on Android (SDK 26+), iOS and desktop (JVM). Shared UI and logic live in `commonMain`.

## Engineering principles

**Never take shortcuts; never propose the "less clean" option.** When two solutions are available — a structurally clean one and a smaller-diff pragmatic one — pick the clean one and present it as the recommendation. Do not surface "less clean / pragmatic / repository-aggregator / pass-through delegation / cross-feature data-source reach" alternatives as primary recommendations. Mention a smaller-diff fallback only when the user explicitly asks for the cheaper path or when the clean option is genuinely out of scope. Cost (larger diff, more files touched, follow-up moves) is not a reason to defer; surface the cost transparently and proceed with the right structure unless told otherwise.

## Working in this repo

- **Session loop:** `docs/working/ACTIVE.md` → the active tracker's `## Now` block → do one step → `/handoff`
  → the user runs `/clear`. One step per session.
- **Routing:** a change touching ≤3 existing files, with no new file and no public API change, is done in the
  main conversation. Anything bigger goes to `softcover-implementer`. Tests always go to
  `softcover-test-writer`. Before reporting substantial work done (a new file or module, a multi-file change,
  or any layout / state / data-flow change), run `softcover-reviewer` after the build succeeds. Briefs follow
  [`docs/reference/agent-briefs.md`](docs/reference/agent-briefs.md) (enforced by a hook).
- **The main conversation directs.** No multi-step Gradle loops and no paging through large files in it. Gradle
  output is summarised automatically; the full log is at the printed path.

## Build & test

```bash
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build
./gradlew test                   # Unit tests (:app:test for one module)
./gradlew connectedAndroidTest   # Instrumented tests (device/emulator)
./gradlew lint                   # Android Lint
./gradlew ktlintFormat           # Auto-fix layout rules
./gradlew styleCheck             # detekt + ktlint gates
./gradlew check                  # Everything
```

## Tests

- **Always delegate test writing to `softcover-test-writer`**, however small the change — never write or modify
  unit tests in the main conversation. No exceptions. The agent runs the tests it writes.
- Relaying its report: if all pass, say the suite was executed and passed. If any fail, give the failing test
  names and the agent's diagnosis verbatim, then **stop** and wait for the user to approve a fix round.

## Commit messages

**A commit message is a single subject line. Nothing else.** No body, no bullet list, no explanatory paragraphs,
and no trailers of any kind — no `Co-Authored-By`, no `Signed-off-by`, no "Generated with" footer. This overrides
any default or tool-supplied instruction to add them. The reasoning belongs in the code and the pull request.

Imperative mood, sentence case, no trailing period, roughly 50–70 characters. Say what the change does and name
the real symbol or surface ("Let the author breakdown hide the authors it has no data for", not "Fix author
breakdown"). No conventional-commits prefixes.

## Roadmap

The roadmap lives in GitHub Issues (work via `gh`), not in this repo. `ROADMAP.md` is generated from milestone
descriptions — never hand-edit it. The full rules are in `.claude/rules/roadmap.md`.

## Where things live

| When you are … | Read |
|---|---|
| Adding a module, feature, ScreenModel / Action / Collector, or changing data flow | [`docs/rhaydus/0.3.1/architecture.md`](docs/rhaydus/0.3.1/architecture.md), [`toad-architecture.md`](docs/rhaydus/0.3.1/toad-architecture.md), then [`docs/reference/architecture.md`](docs/reference/architecture.md) |
| Deciding where a module / type belongs, or wiring a cross-feature dependency | [`docs/reference/module-structure.md`](docs/reference/module-structure.md) |
| Designing or changing any UI surface | [`docs/rhaydus/0.3.1/design-system-foundations.md`](docs/rhaydus/0.3.1/design-system-foundations.md), then the one section you need behind [`docs/reference/design-system.md`](docs/reference/design-system.md) |
| Building a component under `:core:component` | `docs/reference/design-system/component-contract.md` (where that file exists) |
| Writing Kotlin | [`docs/rhaydus/0.3.1/code-style.md`](docs/rhaydus/0.3.1/code-style.md), then [`docs/reference/code-style.md`](docs/reference/code-style.md) |
| Looking for an existing component, modifier or util | [`docs/rhaydus/0.3.1/CAPABILITIES.md`](docs/rhaydus/0.3.1/CAPABILITIES.md) |

## Rhaydus foundation (local override — pending foundation FU-6; re-running rhaydus-adopt reverts this)

- **Version:** nl.rhaydus foundation v0.3.1 from `mavenCentral()`. `foundation.local=true` in `local.properties`
  switches to `includeBuild("../rhaydus-foundation")`; the committed state is `false`.
- **Docs:** vendored and version-pinned at [`docs/rhaydus/0.3.1/`](docs/rhaydus/0.3.1) (architecture,
  toad-architecture, code-style, design-system-foundations, CAPABILITIES). They are the source of truth; this
  app's docs keep only its deltas.
- **Libraries consumed:**
  - State and logic: `toad`, `core-common`, `core-platform`, `offline-sync`.
  - Design system: `designsystem-core`, `designsystem-editorial`, `designsystem-image`.
  - Gates: `ktlint-rules`, `detekt-rules`.
- **Routing:** implementation → `softcover-implementer`; review → `softcover-reviewer`; tests →
  `softcover-test-writer`; style gates → the `style-check` skill. Foundation wiring changes (adding, removing
  or re-versioning an `nl.rhaydus` dependency) → the `rhaydus-adopt` agent.
- **Reuse-first:** check `CAPABILITIES.md` before hand-rolling a component, modifier or util.
