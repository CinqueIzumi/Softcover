---
name: style_extraction_refactor_multiarg_glomming
description: Extracting a duplicated loop into a helper often turns a previously-wrapped multi-arg call into a compact one-liner that violates rhaydus:multi-arg-wrapping; always run ktlintCheck on extraction PRs, not just detekt.
metadata:
  type: feedback
---

When a refactor collapses several lines of a call (e.g. a query built across many named-arg lines)
into a short expression at a new call site, the multi-arg wrapping rule can end up violated even
though the *original* code was compliant — nobody intentionally typed the violation, it fell out of
the extraction. Two confirmed instances in the `fetchAllPages` pagination-helper extraction
(2026-07-24, `core/network/.../helper/Pagination.kt`, `core/personal/.../ReadingJournalHistoryRemoteDataSource.kt`):

- `val page = fetchPage(pageSize, offset)` inside the new helper — two positional args glommed on
  one line.
- `fetchAllJournalRows(bookId = bookId, userId = userId).mapNotNull { ... }` — two named args glommed
  on one line, only appeared because the extraction replaced a multi-line `apolloClient.safeQuery(query = ...(...))`
  call with a short delegating call.

**Why:** detekt (type-resolved, `detektJvmMain`/`detektAndroidMain`) is silent on this — it's a
layout rule, not a correctness rule, so a clean detekt run gives false confidence. Only
`ktlintCheck` (root-level task, `rhaydus:multi-arg-wrapping`) catches it, and it's a hard CI gate.

**How to apply:** on any extraction/refactor review, run root `./gradlew ktlintCheck` (not just
detekt) and scan the output for hits in touched files before signing off, even when the diff "looks"
like pure structural movement with no new logic. Don't rely on eyeballing call sites — two-arg calls
that fit on one line visually are exactly what this rule flags and exactly what's easy to miss by eye.
