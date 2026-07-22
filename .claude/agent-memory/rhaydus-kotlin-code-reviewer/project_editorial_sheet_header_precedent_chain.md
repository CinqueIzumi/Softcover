---
name: project_editorial_sheet_header_precedent_chain
description: Bottom-sheet headers are being migrated one-by-one off EditorialSectionHeader onto a locally-composed accent-bar+eyebrow+AnnotatedString-span header; each new sheet cites the prior one as its precedent.
metadata:
  type: project
---

`EditorialSectionHeader` has no trailing slot and cannot tint a substring of its own headline/description.
Sheets that need either of those (naming the acted-on entity in a `primary` `AnnotatedString` span, or a
trailing mini-jacket `EditionImage`) are being rebuilt with a **locally composed header** instead: accent
bar (32×4dp) + full `eyebrow` + italic `editorialTypography.headlineMedium` + italic `body` description,
with the entity image trailing in a `Row`.

Precedent chain (each cites the previous explicitly in its KDoc):
1. `TagEditorBottomSheet.kt` (`feature/book_detail`) — first to do this, for the tag-editor sheet.
2. `ChooseListsBottomSheet.kt` (`core/designsystem`, shared) — cites the Tag-editor sheet.
3. `EditionBottomSheetSelector.kt` (`feature/book_detail`) — cites the Choose-lists sheet.

Shared anatomy worth checking for consistency whenever a 4th sheet joins this pattern: 32×4dp accent bar,
`eyebrow` style, `headlineMedium` italic headline (often an `AnnotatedString` naming the book/entity in a
`primary` span via `remember(key, primaryColor) { buildAnnotatedString { ... } }`), `body` italic
description, trailing `EditionImage` sized ~56×84 (single) with `cornerRadius = 4.dp`, `elevation = 4.dp`,
`shadowColor = Color.Black.copy(alpha = 0.5f)` — the same shadow literal recurs in `ReadingShelf.kt` (0.6f)
and `FocusModeShelf.kt` (0.5f), so `Color.Black.copy(alpha = ...)` inline (not a design-system color role)
is the accepted idiom for cover-shadow tinting in this codebase, not a violation to flag.

Design-system doc entries for these live in `docs/reference/design-system.md` §4 under "Tag-editor sheet",
"Choose-lists sheet", "Change edition sheet" — cross-check new entries against this anatomy list rather
than re-deriving it from scratch.

See also [[style_one_type_per_file_colocated_support_class]] (private colocated helper data classes like `ListMembershipInfo` in these sheet files pass the one-type-per-file gate).
