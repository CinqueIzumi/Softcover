---
name: Style conventions
description: Key Kotlin/Compose style rules from docs/reference/code-style.md for Softcover, plus the deterministic style-check to run before signing off on a review
type: feedback
---

From docs/reference/code-style.md (read on every review):

- Trailing commas required on ALL multi-argument function declarations and call sites, including the last argument.
- Multi-argument calls/declarations/instantiations break one-per-line as soon as there are 2+ args — even if they fit on one line.
- Multi-argument composables: each arg on its own line, trailing comma on the last arg.
- Blank line between sibling composables inside any layout scope (Column, Row, Box, LazyColumn items), including `Spacer`.
- One `data class` per file; one `enum` per file (data-source interface + Impl colocation is the sanctioned exception).
- `@Nested inner class` grouping in all test files (no `@Test` at the outer class level).
- AAA test markers: `// ----- Arrange -----`, `// ----- Act -----`, `// ----- Assert -----` (five dashes, single space, capitalized).
- No fully-qualified references inline; always import and use the short name.
- Guard clauses: blank line after extraction, blank line between each guard clause, blank line after the last guard clause.
- `.not()` for boolean negation, never `!`.

**Why:** docs/reference/code-style.md is the canonical style source. Style violations — especially multi-arg-on-one-line — are the findings the user pushes back on most.

**How to apply:** Sweep every changed file, not just the changed lines.

---

## Verification — tooling owns the mechanical rules now

A custom ktlint ruleset in `:ktlint-rules` **auto-fixes and gates** most mechanical layout rules — these are no longer things to hand-check:

```bash
./gradlew ktlintFormat   # auto-fix
./gradlew ktlintCheck    # gate (also run by `check`)
```

It owns: multi-arg one-per-line wrapping (incl. `.copy()`/constructors, the historical recurring miss), trailing commas on multi-line lists, blank line after `super.*()`/`Timber.e(...)`, `// region`/`// endregion` flush, no blank line after `{` / before `}`, blank line between sibling composables, and boolean `!` → `.not()` (gate-only — fix by hand).

The remaining rules are covered by `scripts/style-check.sh` (`./gradlew styleCheck`), with documented false-positive filters:
- inline fully-qualified references
- more than one top-level type per file
- project-import (`nl.rhaydus.*`) alphabetical ordering

**Examine each flagged candidate** — advisory recipes have known false positives (`Modifier.padding(start = …, end = …)`, `setOf/mapOf/listOf`, lambda-type params with an internal comma, KDoc `[fully.qualified.Refs]`, data-source interface + Impl colocation). When in doubt, defer to the rule as written in §Argument and Property Layout.

The script can **not** mechanize the blank line between sibling composables (incl. `Spacer`) or the paragraph-spacing rules — spot-check those manually inside `Column` / `Row` / `Box` / `LazyColumn` scopes.

**Do not report a clean style pass without running the script.**
