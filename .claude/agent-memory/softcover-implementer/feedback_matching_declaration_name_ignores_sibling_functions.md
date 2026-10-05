---
name: matching-declaration-name-ignores-sibling-functions
description: detekt's MatchingDeclarationName fires on a file with one top-level enum/class plus unrelated top-level functions, even though there's no "two types" conflict — split the type into its own file.
metadata:
  type: feedback
---

Putting a small top-level enum (e.g. an accessory/variant type for a
composable) in the same file as the composable functions that use it fails
`detekt`'s `MatchingDeclarationName` at `styleCheck`, even though the file
has no second top-level *type* — the rule matches the file name against
the file's one top-level class/interface/enum and ignores that the rest of
the file is functions. `compileKotlin` passes; only `styleCheck` (run
later in Verify) catches it.

**Why:** unlike ktlint's one-type-per-file gate, which is about two
co-located *types*, this fires with exactly one type present, purely
because the enum's name differs from the file's name.

**How to apply:** when a file is named after its primary function/composable
(`SettingsMenuRow.kt`) and also declares a small companion enum or sealed
type (`SettingsRowAccessory`) used only by that function, give the type its
own file up front rather than discovering the failure at `styleCheck`.
