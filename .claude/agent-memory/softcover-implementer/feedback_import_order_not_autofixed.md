---
name: import-order-not-autofixed
description: ktlintFormat does not fix rhaydus:project-import-order or one-type-per-file; sort imports yourself after bulk edits
metadata:
  type: feedback
---

The custom `rhaydus:project-import-order` rule (ASCII order, `java./javax./kotlin.` last, alias imports sorted by path among the rest) and `one-type-per-file` are reported by `ktlintCheck` but NOT fixed by `ktlintFormat`.

**Why:** a scripted edit that prepends imports leaves dozens of violations; ktlintFormat reports success and changes nothing.

**How to apply:** after multi-file edits, run a small script that sorts each touched file's import block, and put every new `data object` / marker type in its own file named after it (detekt MatchingDeclarationName also rejects a lone type in a differently named file).
