# Patterns — Explore & Profile

### Profile screen

- Top to bottom: the reader header; the "Your reading" summary card (books and pages this year over an
  "Open your Reading Atlas" link; the whole card opens the Stats Atlas); the share entry row card; the
  account foot (tonal Log out over a "— · —" colophon). Profile holds no charts.

### Stats Atlas

- Pushed from Profile's summary card; one long scroll, never tabs. Top to bottom: a `PageMasthead`
  ("Your reading record" / "Stats"); the scope row and its line; the opening tiles (hero "Pages read", the
  page's one `primary`-filled surface, over a full-width "Books" tile); "In this atlas" contents; the
  chapters.
- Scope row: single-select `Tonal` chips with the `Eyebrow` face, "Last 30 days", "Last 6 months", "Last 12
  months", one per calendar year the reader finished a book in (newest first), "All time". Compact and
  medium scroll sideways; expanded wraps. A tap gives selection haptics. The reader lands on this year when
  they have finished a book in it, else on All time; the pick is not remembered, and a selected year that
  leaves the row falls back to All time.
- Beneath the row, a quiet italic line names the dates: "Showing 2026", "Showing 24 September to today"
  (the year is added only when the window starts in an earlier one), "Showing every year".
- The scope moves the opening tiles, "The genres you read most", "Who you read" (Hide untagged authors still
  applies) and "How you score". "Recently loved" and the year chart ignore it. Values animate between scopes;
  labels cross-fade, and reduced motion swaps them in place.
- A scope with no finished books says so in each section's framing sentence ("No finished books in the last
  30 days.") instead of drawing an empty chart; the opening tiles show 0 and say nothing was finished. In a
  narrow scope with too few ratings to classify, "How you score" states the count ("3 rated books in the last
  30 days") instead of "Too early to tell". Captions that say "your finished books" name the scope.
- The year chart always shows every year. A selected calendar year lights its column `Lead` (the others
  `Second`, zero years a `Fifth` stub) and the caption names that year and its count for the Books / Pages
  metric; a rolling window or All time lights the busiest year.
- Contents rows (two-digit number, title, caption, chevron) scroll the page to their chapter's top. A
  chapter opens with an eyebrow "01 · Title" numbered by position, so row and header always agree.
- A stat the app holds no data source for yet (reading days) is left out, never a 0.
- Today's only chapter, "Temporary", holds the former Profile charts: "The shape of the years" (a
  `ColumnChart` behind a Books / Pages `SegmentedControl`); "The genres you read most"; "Who you read";
  "How you score" (headline from the rating band, never the bare average); "Recently loved" (up to three
  4★+ reads). Each opens with eyebrow + italic headline (§3.2).

### Statistics charts

- Monochrome `primary` with `ChartTone` alphas; shape carries the data. Flat bars, never the wavy indicator.
- `ProportionChart` only for exclusive, exhaustive categories (gender: up to three ranked slices plus
  `Unknown` last, muted). `RankedBars` for overlapping ones (top five genres, each a share of genre-tagged
  books, track always 100%); the caption names the denominator.
- `ColumnChart`: books per year (peak `Lead`, others `Second`, zero a `Fifth` stub) and the half-star
  ratings histogram (whole stars `Lead`, halves `Fourth`, labels under whole stars only).
- "Who you read" BIPOC / LGBTQ+ bars: fixed Yes / No / Unknown, a zero bucket draws nothing, caption
  "Yes 2 · No 5 · Unknown 23 · of 30 authors". The persisted "Hide untagged authors" `ToggleRow`
  renormalises over tagged authors ("of N tagged authors"), shows only when an untagged bucket exists,
  and sits outside the empty branch.

### Profile share sheet

- Opened from the top bar's share glyph and the share entry row, both `OnShareSheetOpenAction`; dismissed
  through `OnShareSheetDismissAction`.
- A scaled, non-interactive preview of the reading-life share card with "Save image" and share actions,
  through `ShareCardCapture` like the book-detail share sheet. Desktop: a cancelled save is silent; Share
  copies to the clipboard and says so. Success settles with a commit haptic; failure shakes.

### Log-out confirm sheet

- Gates the account foot's Log out: an italic "Leaving so soon?" over a prose line, a primary "Log out"
  and a "Stay" text affordance.
- A `SheetSession` on Profile's UiState: "Log out" records the log-out as the pending intent and closes;
  the log-out runs once the sheet has hidden.

### Title-page sheet (Create list)

- A `SheetScaffold` whose header is a bare eyebrow ("A new list") and footer one primary "Create list";
  between them, centred: a hairline broken by a small `quoteGlyph` (`primary` @ 0.28), a
  `TextFieldVariant.Hero` name field with a real-title placeholder ("Summer reads"), an inset hairline,
  and the privacy prose toggle.
- Only for a sheet whose whole job is naming one short thing; more fields want the canonical header.

### Browse-by-mood grid

- `MoodGrid`: two columns of `MoodTile`, each row at `IntrinsicSize.Max` so wrapped titles share a height.
  Ink by index mod 4; titles title-cased by the mapper (`toTitleCaseWords`). A tap runs a mood-filtered
  search. Loading: four loading tiles in the same two rows.

### Because-you-read genre picker

- Anchor: uppercase `eyebrowSmall` "Genre · {genre}" with a trailing `primary` chevron; hidden with no
  genre options. Opens a collector-driven "Pick a genre" sheet of `SheetRowGroup` rows: "Auto
  (most-read)" first, then one row per genre.

### Search results pagination

- No server total: results append as the visible window nears the fetched end, gated on `hasMore`; a
  tail wavy progress row (full-span on the desktop grid) shows while appending. No "N of M" count.

### Explore search back ladder (mobile)

- Back leaves search before the screen, one rung per press, each its own `NavigationBackHandler`
  (`androidx.navigationevent.compose`) with mutually exclusive `isBackEnabled`: `hasActiveSearch` (query
  or mood browse) → `OnClearSearchAction`; `searchPhase == FOCUS` → `OnSearchDismissedAction`; `FEED` → no
  handler, back belongs to the shell.
- A handler dispatches what the on-screen control dispatches and never clears field focus itself. With
  the keyboard up, the platform's first press hides it.

### Explore search Esc (desktop)

- One rung: `Modifier.dismissOnEscape` on the layout root, always enabled; the callback acts only when
  `hasActiveSearch`, dispatching `OnQueryChangeAction(newQuery = "")` as the field's × does. Gating
  `enabled` would steal focus from the field on the first keystroke.
