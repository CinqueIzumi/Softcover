---
name: project_file_split_body_and_import_drift
description: Two further traps in token-hygiene file splits beyond KDoc-link scope — a dropped statement inside a declaration body, and kotlin.* imports relocated past the nl.rhaydus.* group.
metadata:
  type: project
---

Confirmed live in the Reading `08e` split (ReadingShelf.kt → `screen/section/*.kt` + `ReadingHeaderCopy.kt`):
a "pure move" brief still needs a byte-for-byte body diff per declaration, not just a skim — the split
silently dropped `Spacer(modifier = Modifier.height(2.dp))` inside `CompactBookEntry`'s
`book.currentEdition?.authorString?.let { … }` branch (present in the old file, gone in the new
`CompactBookEntry.kt`), a real visual regression with no snapshot test to catch it. Diff every declaration
body line-for-line against `git show HEAD:<old path>`, don't just confirm the function signatures and
KDoc moved.

Second, distinct from [[project_file_split_kdoc_link_scope]]: splitting a file that ends with
`import kotlin.math.roundToInt` (or any bare `kotlin.*` import) sitting in the third-party group just
above `nl.rhaydus.*` tends to have that import re-appended at the very end of the new file's import list,
after the `nl.rhaydus.*` group — three separate new files had this exact misplacement in the same split.
Per `docs/reference/code-style.md` § Review-only rules, ktlint only sorts inside the `nl.rhaydus.*` group,
so this is invisible to the gate; grep every new file's tail imports against the old file's import order
whenever a split moves declarations that use `kotlin.math.*` / other bare `kotlin.*` imports.
