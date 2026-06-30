---
name: import-rewrite-ordering
description: When a feature.* import is replaced with a core.* equivalent in a bulk rewrite, the new import must be re-sorted into the correct group/alphabetical position — not left in-place where the old import sat.
metadata:
  type: feedback
---

When feature.* imports are replaced by core.* equivalents (as in modularization steps), the replacement import lands out of order: `core.*` ends up below `feature.*` in the project imports group. The style guide requires project imports sorted `core.*` before `feature.*`, alphabetical within each group.

**Why:** The style guide (docs/reference/code-style.md) mandates alphabetical ordering within the project group, and `core` sorts before `feature`. This violation appeared in 26 files during modularization step 3.

**How to apply:** After any import rewrite that changes a `feature.*` import to a `core.*` import, verify the import block is re-sorted. Do not insert the new import in the same line position as the old one.
