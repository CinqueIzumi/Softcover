---
paths:
  - "**/*.kt"
---

# Kotlin style gates

`kotlin.code.style=official`. The mechanical rules are gated by tooling through `check`; the review-only rules
are in `docs/reference/code-style.md` § Review-only rules.

- **ktlint** (`nl.rhaydus:ktlint-rules`) only formats: `./gradlew ktlintFormat` fixes everything it reports,
  `ktlintCheck` gates. It covers one-per-line wrapping of 2+ args/params (`Modifier` calls included),
  trailing-lambda braces over a multi-line body, trailing commas, statement paragraphs and guard-clause spacing,
  brace blank lines, flush `// region`, sibling-composable spacing, `!` → `.not()`, unused imports, and the
  `ij_kotlin_imports_layout` import order.
- **detekt** is type-resolved and gates from zero, with no baseline. The shared foundation config
  (`config/detekt.yml`, unpacked by `extractRhaydusDetektConfig`) sits under Softcover's deltas in
  `config/detekt/detekt.yml`. The `rhaydus` ruleset judges call sites and bodies: raw spacing and corner dp,
  file-level dp, UI models built in composition, inline mockk stubs, bare `runCatching` in a use case, raw
  logging, inline fully-qualified references, `/*` in KDoc, and the AAA markers. Type resolution is what lets
  `rhaydus: UnguardedFlowTerminalRead` tell `Flow.first()` from `Collection.first()`.
- **Konsist** (`nl.rhaydus:konsist-rules`, run by `:konsist:test`) judges declarations: the import bans
  (Material `Text` and `Button`, `kotlinx.datetime.Instant`, `rememberHaptics`), layering, one type per file,
  plumbing visibility, `@Nested` tests, the `action` field name, `UiState` getters that build a UI model, and
  serializable Screen arguments.
- `./gradlew styleCheck` runs `detektMainAndroid` / `detektMainJvm` / `detektMain` / `detektHostTestAndroid`
  across every module: `commonMain`, the platform source sets **and the unit tests**, excluding generated code.
  `iosMain` is not covered (detekt has no type resolution for native targets).
- `LongMethod` is exempt in test sources (Softcover delta).
- detekt's `MatchingDeclarationName` fails a file whose one top-level type is named differently from the file,
  even when the rest of the file is functions; give such a type its own file.
- **Never add a per-file `@Suppress` to quiet a finding.** Either fix it, or change one of the two config
  layers and write down the reason there.
- Follow `foundation/docs/code-style.md`.
- Never reintroduce a greppable style script; a new mechanical rule becomes a ktlint or detekt rule.
