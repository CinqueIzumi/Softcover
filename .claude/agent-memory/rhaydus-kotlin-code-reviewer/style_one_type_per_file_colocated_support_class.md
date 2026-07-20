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
`buildList`. Flagged at the time as presumed `MatchingDeclarationName` bait, same shape as
`LibraryGridLayoutMapping.kt`.

**Correction (2026-07-21, Library Tabs redesign review, same file touched again):** actually ran
`./gradlew :feature:settings:detektJvmMain --rerun` (forced, not up-to-date) against `SettingsShelf.kt`
with `ToggleRowSpec` still present, unmodified, sole top-level type in the file — **zero findings**,
checkstyle XML report genuinely empty. So `MatchingDeclarationName` does **not** fire here after all.
The likely difference from the `LibraryGridLayoutMapping.kt` case that did fire: `ToggleRowSpec` is
`private`, `LibraryLayoutChip` (the enum that fired) was not — the rule plausibly only considers
non-private top-level declarations as "the file's matching type." Ktlint's one-type-per-file rule also
doesn't fire (needs 2+ types; this file has exactly one). **Bottom line: don't flag a `private`
single top-level data/enum class as a `MatchingDeclarationName` hit on assumption alone — verify with a
forced (`--rerun`) detekt run before reporting it, since the up-to-date cache silently returns stale
green/red results.** Still worth eyeballing every new `data class`/`enum class` for its own-file
placement — the rule question is genuinely subtle and worth a real gate run, not a memory citation.

**Accepted-pattern corpus (private helper data class colocated in a component file whose primary
top-level declaration is a `@Composable fun`, all verified passing `ktlintCheck`):**
`EditionImage.kt` → `private data class EditionImageResolution`; `MarkAsReadBurst.kt` →
`private data class ParticleSeed`; `ChooseListsBottomSheet.kt` → `private data class ListMembershipInfo`
(added in the 2026-07-22 Choose-lists/Change-edition sheet redesign, reviewed clean). The gate does not
fire on a `private`, single-file-scoped helper data class that exists purely as a helper-function return
type inside a component file (as opposed to two unrelated public/exported types sharing a file). Do **not**
flag a new instance of this shape on the strength of the guide's prose alone — the prose reads absolute
("a single-caller data class still gets its own file"), but the tooling and this precedent corpus say
otherwise; verify against `ktlintCheck` behavior first.
