---
name: feedback-file-split-import-verification
description: When splitting a large Compose file into one-file-per-section (token-hygiene moves), verify each new file's imports against every symbol it actually uses, not just the imports that seem to belong to that section
metadata:
  type: feedback
---

When splitting a big file (e.g. `ProfileShelf.kt`, `BookDetailShelf.kt`) into per-section files under a
`screen/section/` sub-package, compiling caught missing imports on the first pass more than once —
`Modifier.size(...)`, `.background(...)`, `.height(...)`, `Box` — because the import list was assembled by
skimming the section's own code rather than checking every extension/composable call against the accumulated
import set.

**Why:** the source file had ~100 shared imports at the top; when a section is carved out, only some of those
imports actually apply to it, and it is easy to drop one that a helper composable three screens down in the
original file also happened to use (e.g. `size` used once in a star-icon modifier deep in a helper).

**How to apply:** for each new file, after drafting its import list, re-scan the *whole* body once for every
bare Compose modifier/layout function call (`.size(`, `.height(`, `.width(`, `.background(`, `Box(`, `Row(`,
`Column(`, `Icon(`, etc.) and cross-check each against the import list, rather than trusting the list built up
while reading the section top-to-bottom once. A ktlint/compile failure on a missing import is cheap to fix,
but doing this check before running Verify saves a full Gradle round trip. Related: [[feedback_check_working_tree_before_starting]].
