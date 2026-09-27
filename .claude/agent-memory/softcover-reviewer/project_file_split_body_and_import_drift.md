---
name: project_file_split_body_and_import_drift
description: A further trap in token-hygiene file splits beyond KDoc-link scope — a dropped statement inside a declaration body.
metadata:
  type: project
---

Confirmed live in a Reading split (ReadingShelf.kt → `screen/section/*.kt` + `ReadingHeaderCopy.kt`):
a "pure move" brief still needs a byte-for-byte body diff per declaration, not just a skim — the split
silently dropped `Spacer(modifier = Modifier.height(2.dp))` inside `CompactBookEntry`'s
`book.currentEdition?.authorString?.let { … }` branch (present in the old file, gone in the new
`CompactBookEntry.kt`), a real visual regression with no snapshot test to catch it. Diff every declaration
body line-for-line against `git show HEAD:<old path>`, don't just confirm the function signatures and
KDoc moved.

See also [[project_file_split_kdoc_link_scope]] for the other traps in this same kind of split.
