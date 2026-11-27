# Patterns — Explore & Profile

### Profile screen

- Top to bottom: the reader header; the "Your reading" summary card (books, pages and reading days this
  year, then "The last 12 weeks. You read on N of 84 days." over a `RecentDaysGrid`, both centred, the grid
  tinted like the year grid and not tappable, then an "Open your Reading Atlas" link; the whole card opens the
  Stats Atlas); the share entry row card; the account foot (tonal Log out over a "— · —" colophon). Profile
  holds no charts beyond the 12-week grid.

### Stats Atlas

- Pushed from Profile's summary card; one long scroll, never tabs. Top to bottom: a `PageMasthead`
  ("Your reading record" / "Stats"); the scope row and its line; the opening tiles (hero "Pages read", the
  page's one `primary`-filled surface, over a full-width "Books" tile); "In this atlas" contents; the
  chapters. Each chapter's copy, layout and thresholds follow the design artifact; the mappers hold them.
- Scope row: single-select `Tonal` chips with the `Eyebrow` face, "Last 30 days", "Last 6 months", "Last 12
  months", one per calendar year the reader finished a book in (newest first), "All time". Compact and
  medium scroll sideways; expanded wraps. A tap gives selection haptics. The reader lands on this year when
  they have finished a book in it, else on All time; the pick is not remembered, and a selected year that
  leaves the row falls back to All time. Beneath it, a quiet italic line names the dates: "Showing 2026",
  "Showing 24 September to today" (the year only when the window starts in an earlier one), "Showing every year".
- Every section follows the scope except "Waiting the longest", "Recently loved", the year chart and the day
  grid. Values animate between scopes; labels cross-fade, and reduced motion swaps them in place. A scoped
  framing sentence names the scope ("in the last 6 months", none for All time).
- Every chapter is always shown, so the contents count never changes. A chapter with no data in the scope
  keeps its header and one line saying what would show there ("No finished books in the last 30 days."); a
  section within it may hide its own empty block. A stat the app holds no data source for is left out,
  never a 0. A section with no data yet shows masked while loading.
- Dates: a finish date comes from the read or the user book, never the "finished" journal event; a read
  book without one counts in All time only. Pages read credit logged progress on its day, a Read book's
  remainder on its finish date and a Did not finish book's pages up to its stop point on its stop date. A
  reading day has pages in the on-device history. "Added" is Hardcover's date added, the day a book
  entered the library. Chapter 08 alone also dates a book by a read's start and by logged progress (below).
- Contents rows (two-digit number, title, caption, chevron) scroll to their chapter's top. A chapter opens
  with an eyebrow "02 · Title". Numbers follow the design's chapter list (01, 02, 03, 05, 06, 08 and 99), not
  position, so row and header always agree.
- A tapped book opens book detail, prefetched on press; a tapped tag closes the Atlas and opens Library on
  the Read shelf (All when Read is hidden) filtered to that tag.
- Tone: a reading list is something to look forward to and putting a book down is part of reading. No
  "backlog", no projections, no warning colour.

### Statistics charts

- Monochrome `primary` with `ChartTone` alphas; shape carries the data. Flat bars, never the wavy indicator.
- `ProportionChart` only for exclusive, exhaustive categories (gender: up to three ranked known slices,
  then `Mixed`, then `Unknown` last, muted). `RankedBars` for overlapping ones (genres, each a share of books; position,
  name over a track, share, with each bar filled relative to the lead row, which is always full); the
  description names what the shares count.
- The format split is a `Keys` `ProportionChart` with a fixed format order and tones (Print `Lead`, Ebook
  `Second`, Audiobook `Third`, Print and audio `Fourth`), zero formats dropped and `Unknown` muted last.
  Shares are whole percents that sum to 100; a non-zero share never shows 0%.
- Did not finish genres are count chips (`ChipTrailing.Count`), not bars: a book counts in several genres,
  so the counts do not sum to the books.
- `ColumnChart`: books per year (peak `Lead`, others `Second`, zero a `Fifth` stub); pages per month (the
  peak `Lead` and the only column with a value, the next two `Second`, other non-zero `Third`, zero or
  future `Fifth`; ties go to the earliest month; labels on every third column); time of day (eight
  three-hour blocks, every column labelled and valued with its share); and the half-star
  ratings histogram (whole stars `Lead`, halves `Fourth`, labels under whole stars only).
- Chapter 08 "Diversity and publication": five bars counted in books (gender, authors of colour, LGBTQ+
  authors, BIPOC characters, LGBTQ+ characters; the character bars are the reader's own How it read
  answers). Books without a bar's detail are always their own muted bucket, a zero bucket draws nothing,
  and each caption names its own total ("Yes 2 · No 5 · Unknown 23 · of 30 books"). One persisted "Hide
  books without this detail" `ToggleRow` drops those buckets everywhere and renormalises per bar ("of N
  books with this detail"); it shows only when a bucket exists and sits outside the empty branch.
  The chapter counts every Reading, Paused, Read and Did not finish book the reader read in the scope
  window, each once: a read finished or started in it, a stop date in it, or progress logged on a day in
  it. All time counts every book of those statuses.

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

- One rung: `Modifier.onEscape(enabled = hasActiveSearch)` on the layout root, dispatching
  `OnQueryChangeAction(newQuery = "")` as the field's × does. See the foundation's Esc-to-dismiss rule.
