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

**Two complementary mechanisms, not one — don't assume ktlint-clean means this is covered.**
ktlint's `one-type-per-file` rule only fires when a file has **2+** top-level type declarations (the
`TagGroup` case above: a data class tacked onto a file that already has other types/composables).
Detekt's **`MatchingDeclarationName`** (type-resolved, part of `detektAndroidMain`/`detektJvmMain` —
the actual gate per `typeResolvedDetektTasks` in root `build.gradle.kts`) fires on a *different*
shape: exactly **one** top-level type declaration whose name doesn't match the file name — even when
the file also bundles several top-level extension functions/properties around that one type (those
don't count as "types" for either rule). **Found 2026-07-17** (Library masthead redesign review):
`LibraryGridLayoutMapping.kt` declares one enum (`LibraryLayoutChip`) plus four extension
functions/properties (`chip`, `toLayout`, `showsTitles`, `withTitlesShown`) — passed `ktlintCheck`
clean (only one type, rule doesn't fire) but failed `detektAndroidMain`/`detektJvmMain` with
`MatchingDeclarationName` because the file name doesn't match the enum's name. Check both angles:
"does this file have 2+ types" (ktlint) AND "does this file's one type match the file name" (detekt,
type-resolved — see [[project_detekt_gate_scope]]).

**Found a third time, 2026-07-21** (Appearance 1a redesign review, [[architecture_appearance_1a_redesign]]):
`SettingsShelf.kt` — an already-large multi-region shared component file with zero other top-level
types — gained `private data class ToggleRowSpec(...)` as a single-caller helper for `DisplaySection`'s
`buildList`. Same shape as `LibraryGridLayoutMapping.kt`: exactly one top-level type, name doesn't match
file name, so it's a detekt `MatchingDeclarationName` hit, not a ktlint one. This confirms the pattern
recurs specifically in files that already hold several composables/functions (screen/shelf files) — the
author's attention is on the composables, and a small colocated data class slips through. Keep checking
every new `data class`/`enum class` for this regardless of how clean the surrounding rewrite is.
