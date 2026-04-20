---
name: Style conventions
description: Key Kotlin/Compose style rules from CODE_STYLE_GUIDE.md for Softcover
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

**Why:** Enforced via CODE_STYLE_GUIDE.md which is the canonical style source — re-read it at the start of every review session.
**How to apply:** Sweep every changed file for these violations, not just the changed lines.
