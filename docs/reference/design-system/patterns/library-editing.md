# Patterns — Library editing modes

Bulk-select and rearrange are the two editing modes a Library shelf can enter; see `patterns/library.md` for
the masthead, sheets, and grid these modes sit inside.

### Bulk-select mode

- Entry: long-press a cover on a built-in shelf tab (desktop: right-click → "Select", Ctrl/Cmd-click,
  Shift-click range), or the control line's Select circle (opens empty). Custom-list tabs never select.
- Haptics: `threshold` on entry, `select` per toggle, `Feedback.Routine` / `Feedback.Failure` on a bulk
  move or list toggle, nothing on remove.
- The header becomes a selection header: an exit circle, an italic "N selected" count, Move (dropdown of
  shelves) and Add to list (`ChooseListsBottomSheet`, tristate per list, stays in selection afterwards)
  pills, and a trash circle opening the bulk-remove sheet (`OnBulkRemoveSheetExpandedChangeAction`) whose
  confirm runs an undoable removal. Masthead, search, control line and chip row hide.
- Move offers "Move to Paused" after Currently Reading. When the selection holds the book of the running
  reading session (timer paused or not), the move opens the end-session confirm first
  (`endSessionPauseSheetScaffold`, `:core:uibinding`): "End this session?", primary "End session and pause",
  secondary "Keep reading". Confirming stops the session once the sheet has hidden, then pauses.
- Desktop right-click on a Paused book adds "Resume reading", moving that book to Currently Reading; the
  mapper decides it (`LibraryBookMenuUiModel`), the grid never reads the book's status.
- A Paused shelf with no books reads "Nothing on hold" (masthead) over "Books you set aside wait here until
  you pick them back up."
- Per-cover selection marks are `BookCardSelection`. Back, the exit circle, a tab switch or deselecting
  the last book exits.

### Undoable removal

- Removing books (the bulk-remove sheet, book detail's overflow menu, Explore's remove affordance) leaves
  the surface at once and raises "Removed <title>" (or "Removed N books") with Undo.
- The remote delete waits for the snackbar to close, so Undo restores status, progress, rating and review.
- Every entry point goes through `UndoableBookRemover` (`:core:presentation`), which owns the copy, the
  Undo window and the failure snackbar; never a per-screen variant.

### Rearrange mode and drag-to-reorder

- A positional sort (`MANUAL` on Want-to-Read / Currently Reading / Read; `ORDER` on a ranked custom list)
  is display-only until the control line's "Reorder" chip enters rearrange mode; the same chip reads
  "Done". Exits on Done, back, a tab switch or a sort change. Mutually exclusive with bulk-select.
- Rearranging adds a top-end drag handle per card (`Modifier.draggableHandle`), `lift` on grab and `drop`
  on release, and is drag-only: no tap-to-open, no long-press select. The same grid node renders both
  states, so entering never remounts covers.
- `MANUAL` persists locally, prefix-scoped: positions `0..maxTouchedIndex` are written; untouched books
  keep their state and new books land just below the pinned prefix.
- `ORDER` persists to `list_books.position`, range-scoped: only `[minTouchedIndex, maxTouchedIndex]` is
  rewritten in one mutation; on failure the local write stands until the next refresh.
- `ORDER` is offered only for `lists.ranked = true`; "✶ Make this list ordered" flips it optimistically
  and switches the sort to `ORDER`, rolling both back with a snackbar on failure.
