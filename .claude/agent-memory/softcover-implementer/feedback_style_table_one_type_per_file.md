---
name: style-table-one-type-per-file
description: A brief's "style table file (e.g. FooStyle.kt)" naming a single file is a suggestion, not literal — split the ink/token sealed type from the style data class into separate files before ktlintCheck runs.
metadata:
  type: feedback
---

When a brief asks for a private style/ink lookup table (ink token + a style
data class bundling it with dp values), putting both top-level types in the
one file the brief names (e.g. `ChipStyle.kt`) fails
`rhaydus:one-type-per-file` at `ktlintCheck` — it only allows a single sealed
type's own variants, or an interface plus its implementing class, to
co-locate. A sealed ink/token type plus an unrelated data class is neither.

**Why:** `ktlint-rules`' one-type-per-file gate is silent until
`ktlintCheck`/`styleCheck` runs — `compileKotlin` passes fine with two
top-level types in one file, so the mistake surfaces late, after the
Verify command's compile-only step already reported success. See
[[project_component_migration_style_drops]] for the sibling pattern of
migration briefs dropping detail; this is the same "brief names one file"
literalism trap but for file layout instead of dp/colour values.

**How to apply:** before writing a style-table file, check whether it needs
an ink/token sealed type and a style data class — if so, split it into two
files (`FooInk.kt` for the token + its resolver, `FooStyle.kt` for the data
class + its per-variant lookup) from the start.
