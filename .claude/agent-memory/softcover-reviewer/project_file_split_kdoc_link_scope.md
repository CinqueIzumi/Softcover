---
name: project_file_split_kdoc_link_scope
description: When a token-hygiene "split one file into many" step (docs/working/token-hygiene/steps/08-file-splits.md) moves a composable to a new file, check that its KDoc [Symbol] links still resolve in the new file's import scope.
metadata:
  type: project
---

A pure-move file split (LibraryShelf.kt → screen/section/*.kt, and earlier ProfileShelf.kt /
BookDetailShelf.kt splits) can still legitimately edit a KDoc line even under a "byte-identical bodies"
brief: a `[Symbol]` doc-link only resolves if `Symbol` is importable/visible from the file it now lives
in. Seen in `SelectableCover.kt`: the moved KDoc referenced `[Cover]` (a doc-link), but `Cover` is not
imported into that file (only passed in as a lambda parameter) — the split correctly downgraded it to
plain code-font `` `Cover` `` to avoid a dangling link. This is a **correct, necessary** adaptation, not a
drive-by comment edit to flag — but the reverse (a stale `[Symbol]` link left over after a split, now
unresolvable) would be a real 🟡 finding. When reviewing a file split, grep the new files for `[` immediately
followed by an identifier inside KDoc and confirm each target is either still in scope or was deliberately
downgraded to backticks.

See also [[project_design_system_doc_drift_pattern]] for the general "one fact stated in several places"
doc-hygiene check this is a narrower cousin of.

**Backtick downgrade isn't always the best fix.** Confirmed in the SettingsShelf.kt → `screen/section/*.kt`
split: four links (`EditorialSectionHeader`/`RhaydusButton` in `AppUpdateSection.kt`, `EditorialSectionHeader`
in `LibraryTabsGroupHeader.kt`, `pressScaleClickable` in `VersionFooter.kt`, `LibraryVisibilitySaveBar` in
`LibraryVisibilityContent.kt`) resolved in the old file via an import that the split correctly dropped (the
symbol is never called in that file, only named in prose), so the plain-backtick downgrade avoids a dangling
link — legitimate. But the *same diff* also carries a fully-qualified link with no import,
`` [Chip][nl.rhaydus.softcover.core.component.chip.Chip] ``, in `AppUpdateSection.kt` itself, plus FQN corrections
in `PaletteChoice.kt`/`ThemeChoice.kt` (`[nl.rhaydus...screen.section.ThemeSection]`). A KDoc link resolves
fully-qualified without an import, so a symbol mentioned only in prose (no import wanted) should get
`` [Symbol][fully.qualified.Name] `` — preserving the link — rather than a backtick downgrade, whenever the
split's own diff already demonstrates that convention elsewhere. Flag the inconsistency (🟡), not the downgrade
itself.

**A second, distinct trap in the same steps:** the split's own PR always leaves the *deleted* file's bare
name behind in KDoc/comments of files it did not touch as part of the move — not a `[Symbol]` link, just a
plain-text mention like `` `ExploreShelf.kt:243` `` or "the shared shelf pieces (`ExploreShelf.kt`)". Two
places to check every time: (1) grep the whole repo for the old filename (outside `docs/working/`) — e.g.
`core/uibinding/.../UnreleasedMapper.kt` still cited `ExploreShelf.kt:243` after the 08d split, in a module
the split diff never touched; (2) the platform-actual layout files (`*.mobile.kt` / `*.jvm.kt`) that only
got import additions in the split's diff often carry a top-of-file KDoc sentence naming the old shelf file
as "where the shared pieces live" (confirmed in `ExploreScreenLayout.mobile.kt`, one line above the
`@Composable` — the `.jvm.kt` sibling had already generalized this to "shared shelf pieces" with no
filename, so the two platform actuals can drift out of sync with each other on this exact sentence). Both
are real 🟡 findings even though neither is inside the moved declaration bodies the "pure move" brief
otherwise gates on.
