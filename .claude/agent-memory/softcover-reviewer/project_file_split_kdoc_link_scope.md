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
