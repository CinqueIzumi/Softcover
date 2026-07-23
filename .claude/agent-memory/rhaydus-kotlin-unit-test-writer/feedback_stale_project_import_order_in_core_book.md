---
name: feedback_stale_project_import_order_in_core_book
description: core/book androidHostTest files predating the actionAt-threading work use a stale 3-group import order (org.junit before nl.rhaydus); fix on touch to the ASCIIbetical single-sort convention.
metadata:
  type: feedback
---

`MarkBookAsReadUseCaseTest.kt`, `RecordBookProgressUseCaseTest.kt`, `BooksRemoteDataSourceImplTest.kt`,
`BooksRepositoryImplTest.kt`, and `OfflineUserBookSyncImplTest.kt` (all under
`core/book/src/androidHostTest/.../nl/rhaydus/softcover/core/book/...`) had import blocks ordered as
`io.* -> kotlinx.* -> org.junit.* -> nl.rhaydus.*` (project imports last) instead of the confirmed
correct convention in [[feedback_import_order_convention]] (`io.* -> kotlin.* -> kotlinx.* ->
nl.rhaydus.* -> org.junit.*`, one flat ASCIIbetical sort).

**Why:** these files predate the actionAt-threading pass (Step 2.15 backdating work); the doc at
`docs/rhaydus/0.3.1/code-style.md#import-ordering` and a confirmed-correct sibling file
(`core/profile/.../ProfileRemoteDataSourceImplTest.kt`) both back the single-ASCIIbetical-sort
convention, so the 3-group order in these `core/book` files was the stale one, not a legitimate
alternate convention.

**How to apply:** all five files above were fixed in the same pass (2026-07-22, backdating
`actionAt` tests for the finish/mark-as-read path). If you touch any of these files again, the
import block should already be correct — no further action needed unless a new violation is
introduced. If you encounter this 3-group pattern in OTHER `core/book` test files not touched by
that pass, apply the same fix per the on-touch policy.
