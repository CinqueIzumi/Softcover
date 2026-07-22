---
name: architecture_library_tabs_all_entry
description: Library Tabs (LibraryVisibilitySettings) 1a redesign - logic-side landing of the pinned "All" entry; UI not yet gated on the new fields
metadata:
  type: project
---

feature/settings's Library Tabs screen (LibraryVisibilitySettingsUiState/LibraryTabEntry) got a
logic-first redesign pass (2026-07-21): a new `LibraryTabEntry.All` sealed variant is pinned as
`orderedEntries[0]` always, with a new `count`/`isReorderable`/`canHide`/`isList` field quartet
added to the sealed base so `Status`/`CustomList` declare them explicitly. Persistence-side
invariant is correctly enforced: `OnReorderLibraryTabsAction` filters `LibraryTabEntry.ALL_ID` out
of `newOrderedIds` before writing `draftTabOrder`, and `isDirty` correctly ignores the new
`statusCounts`/`totalCount` state fields (they're derived display data, not user edits).

**UI follow-up landed 2026-07-21, gap resolved:** `SettingsShelf.kt`'s Library-tabs region was fully
rebuilt (flat borderless rows, grip/pin leading column, trailing eye toggle gated on `entry.canHide`,
count line, "New list" foot tile). Verified the three previously-known gaps are all fixed: (1)
`Modifier.draggable` is now applied only `if (entry.isReorderable)` (All gets plain `Modifier`); (2)
`targetIndexFor` gained a `minIndex`/`REORDERABLE_MIN_INDEX = 1` floor that keeps All un-displaceable
in both directions; (3) the old `Switch` was replaced entirely by an `EyeToggle` rendered only
`if (entry.canHide)`, so All (and Currently Reading, which is reorderable-but-not-hideable) simply
render no toggle at all rather than a no-op-enabled one. `docs/reference/design-system.md` gained a
"Library tabs reorderable row" pattern entry (§5) matching the new anatomy, and correctly documents
`SettingsGroup`/`SettingsRowDivider` as fully retired (grepped clean, zero remaining references). See
[[architecture_settings_1a_redesign]] for the sibling Settings-screen redesign this pattern continues
from, and [[style_one_type_per_file_colocated_support_class]] for a correction re: `ToggleRowSpec` in
this same file.

**Count-sourcing detail worth remembering:** `LibraryTabCountsCollector` combines 5 separate
`core:book` flows (`GetAllUserBooksUseCase` + 4 per-status use cases) rather than deriving the 4
status counts by filtering the "all" list locally. This is *correct*, not redundant - verified via
`BooksLocalDataSource.getBooksFlowByStatus` (core/book/.../data/datasource/BooksLocalDataSource.kt)
that each per-status query has extra event-based filtering (e.g. CURRENTLY_READING requires a
`progress_updated`/`user_book_read_started` journal event, DID_NOT_FINISH requires
`status_stopped`) that a naive `book.status == X` filter over the "all" list would NOT reproduce.
Don't flag re-combining these 5 flows as a reuse/efficiency issue - it's mirroring exactly what the
real Library screen's shelf tabs use.

**`BookList.books` count-safety:** confirmed (via `RefreshLibraryUseCaseImpl` ->
`ListsRepositoryImpl.refreshUserLists`) that `list.books` and list metadata are always fetched and
cached together in the same `GetUserBookLists.graphql`/`cacheUserBookLists` round-trip - there is no
separate "shallow overview vs. deep-fetch" split that would leave `books` empty for an unopened
list after a normal full sync. Safe to use `list.books.size` as a real count post-sync.
