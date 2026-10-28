# Patterns — Library

### Masthead-as-shelf-switcher (Library, mobile)

- The page title is the shelf switcher: eyebrow (`PullToRefreshEyebrow`) → a title row pairing the shelf
  name (`pageTitle`, step-autosized, max 3 lines) with a trailing `primary` chevron-down → the stat
  subtitle. The whole title row is one `tactileClickable` target opening the Shelves sheet.
- Below the collapsible title block: the persistent `EditorialSearchField`, the control line, then the
  shelf neighbour rail.
- With the opt-in "Swipe between shelves" preference, a `HorizontalPager` (one page per `visibleTabs`
  entry) backs the content. The header follows `currentPage`; tab selection fires only on `settledPage`.
  Swiping is off in selection and rearrange modes.
- Desktop keeps its permanent shelf sidebar instead: no title chevron, no Shelves sheet, no pager.

### Masthead control line

- One row on both platforms, under the search field. Left: the sort label (`eyebrowSmall`, uppercase) and
  a direction chevron (hidden under a positional sort), opening the Arrange sheet; beside it the
  "Reorder" / "Done" chip of rearrange mode.
- Right: a Filter pill (`inversePrimary` dot when a filter is active) and a 38dp Select circle that enters
  selection mode empty (`OnEnterSelectionModeAction(bookId = null)`); a cover long-press seeds its book.
  The Select circle is omitted on custom-list tabs and an empty shelf.
- No layout control here; layout lives in the Arrange sheet. Hidden during bulk-select.

### Shelf neighbour rail (Library, mobile)

- Beneath the control line (and any active-filter row): a fixed 48dp row between hairlines, previous shelf
  at the start, next at the end, `eyebrowSmall` uppercase, one line, ellipsised.
- Each half of the row is one `tactileClickable` target animating one shelf across, no haptic. A missing
  end is an empty weighted slot, so the row never shifts.
- Shown only with the swipe preference on, more than one visible shelf, and neither selecting nor
  rearranging. Desktop has no rail.

### Shelves sheet (Library, mobile)

- Opened from the masthead title. Canonical header (`YOUR SHELVES`) with a trailing "Manage" affordance
  to the shelf-visibility settings screen.
- One hairline-split row per `visibleTabs` entry: a shelf glyph (`onSurfaceVariant`) for built-in shelves
  or a list glyph (`primary`) for custom lists, the name (`titleLarge` italic, max 2 lines, `primary` when
  active), a tabular count (`tabStatsFor`), and a check on the active row. A tap selects and dismisses.

### Arrange sheet

- One sheet on both platforms for sort and layout, canonical header `ARRANGE THE SHELF`. LAYOUT: Grid·2 /
  Grid·3 / List chips and a "Show titles & authors" `ToggleRow` (hidden on List). SORT BY · {shelf}: the
  shelf's sort modes as chips; tapping the active chip flips its direction.
- Draft/commit: every chip and the toggle mutate a draft seeded on open; the footer "Show N titles"
  (`ButtonSize.M`, previewing the committed count) fires `OnApplyArrangeAction`; any other dismiss discards.
- "✶ Make this list ordered", under an unranked custom list's chips, applies at once
  (`OnSetListRankedAction`) and nudges the draft's sort to `ORDER`.

### Filter sheet

- Canonical header `FILTER THIS SHELF`: Ownership · Format, Release year, Year finished (Read tab only,
  `LibraryFilterOptions.readYears`), Your tags, Hardcover tags, Rating — each a `FacetSection` of `Chip`s,
  wrapping on the panel form and scrolling on the sheet form (§3.5).
- Two tag sources, never merged: Your tags (the reader's own, from Room) and Hardcover tags (every category
  of the book's community tags). Within and across sources a book needs every included tag and none left out.
- Each source section: "N chosen", the chosen chips (included: selected with a check; left out:
  `ChipTone.Excluded`; a tap flips included and left out, the trailing ✕ removes), then a `TextField`
  `Pill` search with a trailing clear. Below it, every unchosen tag is one `ExpandableFlowRow` of
  `name · count` chips (as the book-detail tags: two lines collapsed, progressive, collapsible, "Show more" /
  "Show less" chips), ordered by count with prefix matches first while searching. Tapping a chip includes
  the tag, moving it up into the chosen chips. While the search is non-blank, category `Choice` chips (All
  plus those present) and a "Matching “query”" eyebrow appear above the row; clearing the search resets the
  category to All. The row is one item whose height animates, never snapping. A source with no tags on the
  shelf is hidden. Filtering and a tag moving into the chosen chips glide on the theme motion scheme
  (instant under reduced motion), and chip taps and the ✕ give a light selection haptic.
- Draft/commit: chips and the in-sheet "Clear all" mutate a `LibraryFilters` draft; the footer "Show N
  titles" previews it live (`libraryPreviewCount`) and fires `OnApplyFiltersAction`; any other dismiss
  discards. "Clear all" is always rendered, dimmed and inert with no active filter.
- The main-screen active-filter row stays live (`OnToggleFilterValueAction` / `OnClearFiltersAction`). A
  left-out tag reads "Not romance"; chips from Your tags lead with the tag glyph.

### Library layout model

- The Arrange-sheet layout chips and titles toggle map onto Library's persisted six-value grid-layout
  enum; no other persisted field. The toggle flips a titled grid and its cover-only sibling, keeping the
  column choice; a layout chip keeps the titled-ness.
- List always resolves to the large row. The compact-list value is never offered and renders as the
  large row.

### Library cover grid and list row

- Every shelf cell is a `BookCard`: `BookCardVariant.Column` (titled grid), `CoverOnly`, or `Row` (list).
- Grid cells carry a countdown `CoverOverlay` on the cover while a deadline is tracked and a `ProgressBar`
  beneath while in progress (Reading or Paused), both height-reserved. The large row is de-carded (top hairline), with
  progress and a `NN%`, and a full-width `DeadlineSummaryLine` beneath the row when a deadline is
  tracked; the compact row carries a deadline `Badge` instead.
- A Paused book's card with a deadline shows "On hold" in place of the date and pace line, with no deadline
  badge.
- List rows end the text column with one line of the reader's own tags as inert compact chips (Genre, Mood,
  Tag, Content warning, then by name; spoiler-flagged shown plainly), the overflow collapsed into an outlined
  "+N". No tags, no strip. Custom-list rows take the parent book's tags.
