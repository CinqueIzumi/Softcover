# CLAUDE.md

## Project overview

Softcover is a Kotlin Multiplatform / Compose Multiplatform client for [Hardcover.app](https://hardcover.app/), a
book tracking platform, on Android (SDK 26+), iOS and desktop (JVM). Shared UI and logic live in `commonMain`.

## Engineering principles

**Never take shortcuts; never propose the "less clean" option.** When two solutions are available — a structurally clean one and a smaller-diff pragmatic one — pick the clean one and present it as the recommendation. Do not surface "less clean / pragmatic / repository-aggregator / pass-through delegation / cross-feature data-source reach" alternatives as primary recommendations. Mention a smaller-diff fallback only when the user explicitly asks for the cheaper path or when the clean option is genuinely out of scope. Cost (larger diff, more files touched, follow-up moves) is not a reason to defer; surface the cost transparently and proceed with the right structure unless told otherwise.

## Working in this repo

- **Session loop:** `docs/working/ACTIVE.md` → the active tracker's `## Now` block → do one step → `/rhaydus-kotlin:handoff`
  → the user runs `/clear`. One step per session.
- **The main conversation directs.** No multi-step Gradle loops and no paging through large files in it. Gradle
  output is summarised automatically; the full log is at the printed path.
- Routing, tests and the doc map are in the Rhaydus block below; work inside `foundation/` follows
  `.claude/rules/foundation.md`.

## Build & test

```bash
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build
./gradlew test                   # :app / :desktopApp unit tests only — no KMP suites
./gradlew testAndroidHostTest --continue   # KMP unit tests
./gradlew :smoketest:pixelApi36MinifiedAndroidTest   # Device suite, R8-minified app (needs HARDCOVER_SMOKE_TOKEN)
./gradlew :app:checkR8KeepContract   # R8 keep contract on the release and minified builds
./gradlew lint                   # Android Lint
./gradlew ktlintFormat           # Auto-fix layout rules
./gradlew styleCheck             # detekt + ktlint gates
./gradlew check koverXmlReport -x lint  # PR gate: what CI runs on PRs into release/*
./gradlew check                  # Everything except buildHealth; iOS compiles on macOS only
./gradlew check buildHealth      # Full gate: CI on every push to main, release/*, hotfix/* and chore/*
```

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

<!-- rhaydus:start -->
## Rhaydus foundation (managed by rhaydus-adopt - do not hand-edit)

- **Version:** the nl.rhaydus foundation, built from the `foundation/` git submodule through an `includeBuild`, pinned at v0.4.0.
- **Docs:** `foundation/docs/` at the pinned commit (CAPABILITIES, architecture, toad-architecture, code-style, design-system-foundations). They are the source of truth; this app's docs keep only its deltas.
- **Libraries consumed:** state and logic `toad`, `core-common`, `core-platform`, `offline-sync`; design system `designsystem-core`, `designsystem-editorial`, `designsystem-image`; gates `ktlint-rules`, `detekt-rules`, `konsist-rules`.
- **This app's design system:** `docs/reference/design-system.md`.

### Where things live

| When you are ... | Read |
|---|---|
| Adding a module, feature, ScreenModel / Action / Collector, or changing data flow | `foundation/docs/architecture.md`, `toad-architecture.md`, then `docs/reference/architecture.md` |
| Deciding where a module / type belongs, or wiring a cross-feature dependency | `docs/reference/module-structure.md` |
| Designing or changing any UI surface | `foundation/docs/design-system-foundations.md`, then the one section you need behind `docs/reference/design-system.md` |
| Building a component under `:core:component` | `docs/reference/design-system/component-contract.md` |
| Writing or changing any user-facing text | `docs/reference/design-system/voice.md` |
| Writing Kotlin | `foundation/docs/code-style.md`, then `docs/reference/code-style.md` |
| Looking for an existing component, modifier or util | `foundation/docs/CAPABILITIES.md` |
| Adding a manifest component, worker, widget, Screen argument, or anything reached by name / touching keep rules | `docs/reference/r8.md` |
| Changing or publishing the Claude design preset | `design/README.md` |

### Routing

- A change touching 3 or fewer existing files, with no new file and no public API change, is done in the main conversation. Anything bigger goes to `rhaydus-kotlin:implementer`, one per vertical slice.
- Tests always go to `rhaydus-kotlin:test-writer`, however small. Never write or modify unit tests in the main conversation.
- Before reporting substantial work done (a new file or module, a multi-file change, or any layout / state / data-flow change), run `rhaydus-kotlin:reviewer` after the build succeeds.
- Briefs follow `docs/reference/agent-briefs.md` (enforced by the brief-check hook).
- Style gates: `/rhaydus-kotlin:style-check`.
- Reuse-first: check `foundation/docs/CAPABILITIES.md` before hand-rolling a component, modifier or util.
- A foundation version change moves the submodule pointer: fetch in `foundation/`, check out the new tag, `git add foundation`.

### Relaying the test-writer's report

- All pass: say the suite was executed and passed.
- Any fail: give the failing test names and the agent's diagnosis verbatim.
- The diagnosis points at production behaviour: stop and wait for the user to approve a fix round.
- The failure is test-side fallout from an intended change (setup the change now correctly rejects, a style gate on the test file): send the fix round straight away.

### Agent parameters

| Parameter | Value |
|---|---|
| Compile verify task | `compileKotlinJvm` |
| Unit test task / source set | `testAndroidHostTest` / `src/androidHostTest` |
| Full gate / auto-fix | `check buildHealth` / `ktlintFormat lintFix` |
| Test package prefix | `nl.rhaydus.softcover` |
| Doc paths | review-only rules `docs/reference/code-style.md`, tests rules `.claude/rules/tests.md`, R8 `docs/reference/r8.md`, design system `docs/reference/design-system.md`, voice `docs/reference/design-system/voice.md` |
| Foundation docs | `foundation/docs/` |
| Module roles | database home `:core:database`, component home `:core:component` |
| Stability task | `stabilityDump` |
<!-- rhaydus:end -->
