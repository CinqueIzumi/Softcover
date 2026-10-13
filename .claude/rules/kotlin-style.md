---
paths:
  - "**/*.kt"
---

# Kotlin style gates

`kotlin.code.style=official`. The mechanical rules are gated by tooling through `check`; the review-only rules
are in `docs/reference/code-style.md` § Review-only rules.

- **ktlint** (`nl.rhaydus:ktlint-rules`): `./gradlew ktlintFormat` auto-fixes, `ktlintCheck` gates. It covers
  one-per-line wrapping of 2+ args/params, trailing commas, blank lines after `super.*()` / `AppLog.e(...)`,
  flush `// region`, no blank line after `{` / before `}`, and a blank line between sibling composables.
  Gate-only, fixed by hand: `!` → `.not()`, inline fully-qualified references, one type per file, project-import
  ordering, inline mockk stubs (`coEvery` / `every` open onto their own line), and bare `runCatching` in a use
  case (use `runCatchingLogged`).
- **detekt** is type-resolved and gates from zero, with no baseline. The shared foundation config
  (`config/detekt.yml`, unpacked by `extractRhaydusDetektConfig`) sits under Softcover's deltas in
  `config/detekt/detekt.yml`. Without type resolution, rules such as `rhaydus:UnguardedFlowTerminalRead`
  (`Flow.first()` vs `Collection.first()`) are silently inert.
- `./gradlew styleCheck` runs `detektAndroidMain` / `detektJvmMain` / `detektMain` / `detektAndroidHostTest`
  across every module: `commonMain`, the platform source sets **and the unit tests**, excluding generated code.
  `iosMain` is not covered (detekt has no type resolution for native targets).
- `LongMethod` is exempt in test sources (Softcover delta).
- **Never add a per-file `@Suppress` to quiet a finding.** Either fix it, or change one of the two config
  layers and write down the reason there.
- Never reintroduce a greppable style script; a new mechanical rule becomes a ktlint or detekt rule.
