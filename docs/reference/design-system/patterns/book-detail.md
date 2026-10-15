# Patterns — Book detail

### Book-detail tag block

- The Book lens's `Tags` section, after About: Genre, Mood, Other tags (the Hardcover Tag category) and
  Content warning, in that order, each all its tags by popularity (`taggable_counts.count`) as a
  `labelLarge` sub-label over an `ExpandableFlowRow` of read-only `Chip`s, under a bar-less `TAGS` label.
- Each row collapses to two lines (`Progressive(linesPerExpand = 2)`, collapsible); the "Show more" /
  "Show less" affordances are `ChipTone.Dashed` `Chip`s, and the height change rides
  `animateContentSize`. The expansion lives in the row, so it resets on a lens switch.
- Content-warning chips render as `ChipTone.Spoiler` and reveal in place one per tap (the inline spoiler
  reveal); revealed keys live in `BookDetailUiState.revealedTagKeys` for the screen instance.
- The edition colophon line closes the section; the section is skipped only with neither tags nor a
  colophon fact.

### Your tags section

- The Yours lens, between the in-progress/status block and the review, only for a shelved book
  (`book.userBook != null`): a bar-less `YOUR TAGS` label over one flat wrapping `FlowRow` of the reader's
  own tags, spoiler-flagged ones shown plainly.
- The row ends with a `ChipTone.Dashed` chip, "Edit tags" (or "+ Add tags" with no tags yet), opening the
  tag editor sheet. Edits are optimistic and roll back on failure.

### Tag editor sheet

- `TagEditorBottomSheet`: `SheetScaffold`, `SheetLayout.Pinned`, dismiss label "Done". Add block: four
  category chips (Genre / Mood / Tag / Content warning), a `TextField` `Pill` naming field whose Add pill
  exists only for a non-blank draft (IME Done commits too), then a category-scoped suggestion cloud of the
  reader's own tags in an `ExpandableFlowRow` (`Progressive(linesPerExpand = 3)`), ranked by usage and
  narrowed by substring, prefix matches first (`computeTagSuggestions`).
- The collection groups Genre → Mood → Tag → Content warning (others under "Other") as `Chip`s with
  `ChipTrailing.Dismiss`; a new chip rises in, tracked by `newlyAddedTagKey`. Add block and collection
  share one scroll region, never a nested scroller.

### Change edition sheet

- `EditionBottomSheetSelector`: `LazySheetScaffold` (`SheetLayout.Pinned`, `fillHeight`), primary "Track this edition"
  (enabled once a different edition is picked). `EditorialSearchField` ("Search by ISBN or publisher"), an
  "{n} EDITIONS" count, then one `BookCard` row per edition: the format as title with a format icon, a meta
  line (publisher · year · pages or duration), "ISBN …"; single choice. Owned editions carry "Owned", the
  tracked one a "Tracking now" badge.

### Shelve control

- A bar-less `SHELVE THIS BOOK` label over a `surfaceContainerLow` card of four rows: Want to read /
  Reading / Paused / Read. The active row fills `primary` / `onPrimary`; active Reading or Paused shows
  `{pct}% · p. {page}` (time-based for audiobooks), active Read the read date when known.
- All four rows are enabled for an off-shelf book: Reading, Paused or Read shelves it there in one step.
  Reading on a paused book resumes it.
- Paused while the book's reading session is open (timer running or paused) asks first through the
  shared end-session sheet. "End session and pause" stops the session, then pauses; "Keep reading"
  leaves both as they were.
- Writes are optimistic; failure shakes the card (`Modifier.shakeOnError`) with "Couldn't save — tap to
  retry". Read plays the mark-as-read celebration. While loading, each row is masked and inert.

### Lens toggle

- A sticky `SegmentedControl` (Track) in a `stickyHeader` under the top bar: The Book (default) then
  Yours. Yours is disabled until the book has a user copy.
- The inactive lens's sections are not composed.

### Book-detail in-progress stat

- The Reading-shelf hero stat, shown for a Paused book too (its `Update` pill records progress, which
  resumes the book), though the pace forecast stays Reading-only. An "In progress" `SectionHeader` with a
  trailing `Update` pill (opens the Update progress sheet, whose "Mark as read" runs the shelve control's
  Read path), the percentage in `statHero`, a `ProgressBar`, a `p. {x} of {y}` / `{n} pages to go` row, an
  optional pace forecast ("Finish in {x} reading days"), and the `DeadlineRow` when a deadline is set.
- A Paused book never reads as in progress. The section header reads "On hold", the counts row reads
  `{n} pages left` (`{t} left` for an audiobook), and the progress sheet it opens carries an "On hold"
  eyebrow and an "Update and resume" primary button. The `DeadlineRow` drops its status badge and
  its goal reads "On hold. Resume to get back on pace." The percentage, bar and `p. {x} of {y}` stay.
- The forecast and `DeadlineRow` reveal in place; The Book lens's Tags and Find it sections do the same.
  Voices and About reserve their footprint with masked stand-ins while loading (see "Section footprint
  reserved while loading", cross-cutting.md).
- `ReadingPaceForecast` is a `:core:personal` capability shared with Reading's featured-hero card.

### Edition colophon line

- One `bodySmall` / `onSurfaceVariant` line for the selected edition: publisher, format + release year,
  and `ISBN-13`-prefixed ISBN, joined with ` · `. Unknown parts drop; with none known the line is omitted.
- It closes the book-detail tag block; never a labelled metadata table.

### External-links strip (labeled pills)

- A bar-less `FIND IT` label over a wrapping `FlowRow` of labelled outline pills (Bookshop, Amazon,
  OpenLibrary), each searching the selected edition's ISBN (`isbn13`, else `isbn10`).
- A tap dispatches `OnExternalLinkClickAction`; the screen's `Content()` collects `OpenExternalLinkEvent`
  and calls `LocalUriHandler.openUri`. No leaf composable touches `LocalUriHandler`.
- The section, label included, is omitted when the edition has no ISBN.

### Verdict (rating and review)

- Rating and review are one verdict: edited together in `VerdictSheet`, shown together in the read-only
  `VerdictBlock`; never two surfaces.
- `Book.toVerdictSheetUiModel` (`:core:uibinding`) takes a `VerdictSheetContext` (`FINISHED` / `EDIT`) that
  swaps the copy and whether Delete is offered, never the anatomy.
- Tapping `VerdictBlock` (or its "Edit" link) is the only way back into the sheet, always in `EDIT`.

### Verdict prompt (open-on-finish)

- `VerdictSheet` in `FINISHED` context opens from book detail and, automatically, from Reading right after
  a mark-as-read, only on a genuine `ShelfMutationOutcome.Applied` transition.
- On Reading it opens after the mark-as-read celebration settles. No Delete action. Library's bulk
  mark-as-read never raises it.
- The save is `SaveBookVerdictUseCase` (`:core:book`); each feature owns only the trigger and the hosting
  state.

### Spoiler reveal

- Whole-review gate: a review flagged as containing spoilers withholds its body behind a quiet "Show
  spoiler" text button (reveal held transiently per review id); used on community review cards.
- Inline span reveal: a spoiler run inside a visible review renders as a solid `spoilerCover` block and
  reveals in place on tap (`RichText`). The two compose but stay separate affordances.
- In the editor the same mark previews as the translucent `spoilerEditorHighlight` wash.
