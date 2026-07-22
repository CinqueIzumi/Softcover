---
name: architecture_library_tabs_all_entry_ui_followup
description: Library Tabs redesign UI+logic+test full pass reviewed clean; only new finding was trailing-lambda glomming in the new collector test file.
metadata:
  type: project
---

Reviewed 2026-07-21 (uncommitted, `release/3.1.0`), the full logic+UI+test follow-up to
[[architecture_library_tabs_all_entry]]. Verified end-to-end: the "All" entry can never leak into
`draftTabOrder` (stripped in `OnReorderLibraryTabsAction`, and the only other writer of
`draftTabOrder` is the persisted-order mirror on first load, which can't contain "all" either since
it's a brand-new id), `isDirty` genuinely ignores `statusCounts`/`totalCount` (the getter doesn't
reference them at all), and Currently Reading is `canHide = false` / `isReorderable = true` exactly
per spec (`UserBookStatus.isAlwaysVisibleInLibrary` only true for `CURRENTLY_READING`). TOAD
Dependencies/ScreenModel/DI param ordering is symmetric across all three sites (checked by hand,
16 params). No cross-feature imports into `feature.library`. `Status.isAlwaysOn` is now genuinely
dead (grepped, zero other references) — flagged as a should-fix prune, not a blocker.

**Only real style finding:** `LibraryTabCountsCollectorTest.kt` (new file) has 3 instances of the
`) }` trailing-lambda glomming pattern (`val job = launch { collector.onLaunch(\n  ...,\n) }`) — see
[[style_trailing_lambda_glomming_recurs]]. Grep `") }"` across every new/touched file every time;
this file's other `every { } returns` mockk stubs were correctly split multi-line.

Also note: `EmptyEntriesCard()`'s guard (`if (entries.isEmpty())`) in `ReorderableTabsGroup` is
unreachable dead code — `orderedEntries` always prepends the All entry unconditionally, and even
pre-diff the 4 hardcoded `defaultStatusOrder` statuses meant the reorderable universe was never
empty either. Pre-existing, not introduced by this diff; low-severity on-touch nit only.
