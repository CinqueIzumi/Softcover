---
name: style_one_type_per_file_colocated_support_class
description: A private data class declared inside a screen/component file alongside composables violates the blocking one-type-per-file ktlint rule — check every new local data class/enum for its own file.
metadata:
  type: project
---

The foundation code-style guide (`docs/rhaydus/0.3.0/code-style.md` §"One declaration per file") is
explicit and shows this exact shape as a "Bad" example: "a second data class tacked onto a file
alongside helper functions." Per Softcover's CLAUDE.md, `one-type-per-file` is one of the five
formerly-greppable rules now promoted to a **blocking** `nl.rhaydus:ktlint-rules` check (gate-only,
fixed by hand — `ktlintCheck` fails the build on it).

**Found 2026-07-16** (tag-editor redesign review): `TagEditorBottomSheet.kt` declared
`private data class TagGroup(val category: TagCategory, val tags: List<UserTag>)` directly in the
same file as the sheet's composables — a clean rewrite that otherwise had excellent
spec/style/reuse discipline still tripped on this. Fix: extract to its own file (e.g. `TagGroup.kt`,
same package/directory as the caller per "a single-caller data class still gets its own file next to
that caller"); `private` visibility doesn't survive the move since Kotlin's file-scoped `private`
would then hide it from the caller, so it needs at least `internal`.

**How to apply:** any diff that introduces a new `data class` or `enum class` — even a small,
single-caller, seemingly load-bearing-only-here support type — gets checked for whether it lives in
its own file. The one recognized exception is a sealed hierarchy's own variants co-located in the
sealed type's file. Plain functions, typealiases, and extension properties are NOT covered by this
rule — only `data class` / `enum class` (and the action/event per-suffix corollary). This is worth a
dedicated look on every review since it's easy to miss in an otherwise-clean file and there is no
compiler error, only the ktlint gate (which may not have been run before the review).
