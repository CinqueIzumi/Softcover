---
name: Style conventions
description: Key Kotlin/Compose style rules from CODE_STYLE_GUIDE.md for Softcover, with a verification checklist to run before signing off on a review
type: feedback
---

From CODE_STYLE_GUIDE.md (read on every review):

- Trailing commas required on ALL multi-argument function declarations and call sites, including the last argument.
- Multi-argument composables: each arg on its own line, trailing comma on the last arg.
- Blank line between sibling composables inside any layout scope (Column, Row, Box, LazyColumn items).
- One `data class` per file; one `enum` per file.
- `@Nested inner class` grouping in all test files (no `@Test` at the outer class level).
- AAA test markers: `// ----- Arrange -----`, `// ----- Act -----`, `// ----- Assert -----` (five dashes, single space, capitalized).
- No fully-qualified references inline; always import and use the short name.
- Guard clauses: blank line after extraction, blank line between each guard clause, blank line after the last guard clause.
- `.not()` for boolean negation, never `!`.

**Why:** Enforced via CODE_STYLE_GUIDE.md which is the canonical style source — re-read it at the start of every review session. Style violations are the most common findings the user pushes back on.

**How to apply:** Sweep every changed file for these violations, not just the changed lines.

---

## Verification checklist — run BEFORE reporting a review complete

Re-reading the style guide and *listing the rules* is not the same as *verifying compliance*. The user has pushed back on this twice: the agent surfaces loud findings (no-op replaces, indentation glitches, design-system gaps) and silently skips the patient, methodical sweep against the layout rules. Always run these greps against the changed files before declaring the review done. Each one is a known recurring miss in this codebase.

### 1. Multi-arg function declarations on one line — RECURRING MISS

§Argument and Property Layout says: "as soon as a second argument or property is added, all of them break onto their own lines." This applies to function declarations, function calls, constructor invocations, data-class instantiations, `apply { }` / `copy(...)`, interface members — everything parenthesised with 2+ entries.

Grep recipe (run on each changed `.kt` file):
```bash
grep -nE "fun [a-zA-Z]+\([a-zA-Z][^)]*,[^)]*\)" <file>
```
Any hit is almost always a violation. Common offender shapes:
- `fun foo(bookId: Int, body: String, hasSpoilers: Boolean)` — declaration
- `override suspend fun add(bookId: Int, quote: String, page: Int?, note: String?): Long`
- `suspend operator fun invoke(tabId: String, mode: LibrarySortMode): Result<Unit> = ...`

### 2. Multi-arg call sites on one line — RECURRING MISS

Grep recipe:
```bash
grep -nE "[a-zA-Z]+\([a-z][a-zA-Z]+ ?= ?[^,()]+, [a-z][a-zA-Z]+ ?= ?" <file>
```
False positives to ignore: `Modifier.padding(start = …, end = …)`, `setOf(1, 2, 3)`, `mapOf(a to b, c to d)`, `LaunchedEffect(a, b) { … }` (single-value + trailing lambda). Real offenders:
- `repository.saveDraft(bookId = bookId, body = body, hasSpoilers = hasSpoilers)`
- `useCase(tabId = tabId, mode = mode)`
- `.copy(foo = a, bar = b)` — yes, `.copy(...)` with 2+ properties also breaks across lines per the rule.

### 3. Inline fully-qualified references

Grep recipe:
```bash
grep -nE "(androidx|java|kotlin|kotlinx|nl\.rhaydus)\.[a-z][a-zA-Z._]+\.[A-Z]" <file>
```
Any hit outside an `import` line is a violation. Add an import; use the short name.

### 4. Boolean `!` negation

```bash
grep -nE "if \(![a-zA-Z]|while \(![a-zA-Z]| && !| \|\| !" <file>
```
Replace with `.not()`.

### 5. Missing blank lines between sibling composables / multi-line constructs

Harder to grep, but spot-check inside `Column { … }` / `Row { … }` / `LazyColumn { … }` blocks: every two consecutive composable calls must have a blank line between them, including `Spacer`.

### 6. Multiple classes per file

```bash
grep -cE "^(class |data class |enum class |sealed class |sealed interface |interface |object )" <file>
```
If the count > 1, flag — one type per file, named after the type (data sources are an explicit exception: interface + Impl colocated). Use cases especially: never bundle several `*UseCase` classes in a single `XyzUseCases.kt`.

### 7. Imports outside their group / out of alpha order

The order is: androidx → third-party → project (`nl.rhaydus.*`) → kotlin/java, alphabetical within each group. A `nl.rhaydus.*` import sitting below a `kotlin.*` import, or an out-of-alpha line, is a violation.

### Reporting

**Do not report a clean style pass without running these greps.** If grep produces hits, examine each one before declaring it a false positive. When in doubt, defer to the rule as written in §Argument and Property Layout.
