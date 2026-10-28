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
  chapters.
- Scope row: single-select `Tonal` chips with the `Eyebrow` face, "Last 30 days", "Last 6 months", "Last 12
  months", one per calendar year the reader finished a book in (newest first), "All time". Compact and
  medium scroll sideways; expanded wraps. A tap gives selection haptics. The reader lands on this year when
  they have finished a book in it, else on All time; the pick is not remembered, and a selected year that
  leaves the row falls back to All time.
- Beneath the row, a quiet italic line names the dates: "Showing 2026", "Showing 24 September to today"
  (the year is added only when the window starts in an earlier one), "Showing every year".
- The scope moves the opening tiles, the time-of-day chart, the pages-by-month chart, the authors list, the format
  split, the genre
  ranking, "Who you read" (Hide untagged authors still applies) and "How you score". "Recently loved" and
  the year chart ignore it. Values animate between scopes; labels cross-fade, and reduced motion swaps them in place.
- A scope with no finished books says so in each section's framing sentence ("No finished books in the last
  30 days.") instead of drawing an empty chart; the opening tiles show 0 and say nothing was finished. In a
  narrow scope with too few ratings to classify, "How you score" states the count ("3 rated books in the last
  30 days") instead of "Too early to tell". Captions that say "your finished books" name the scope.
- A finish date comes from the read or the user book, never from the "finished" journal event, which dates
  when a book was shelved as read. A read book without one counts in All time only, in no dated scope or chart.
- The year chart always shows every year. A selected calendar year lights its column `Lead` (the others
  `Second`, zero years a `Fifth` stub) and the caption names that year and its count for the Books / Pages
  metric; a rolling window or All time lights the busiest year.
- Contents rows (two-digit number, title, caption, chevron) scroll the page to their chapter's top. A
  chapter opens with an eyebrow "02 · Title". Numbers are fixed per chapter (01, 02, 03 and 99), following the
  design's chapter list rather than position, so row and header always agree; the other chapters land with
  later 3.3.0 issues.
- A stat the app holds no data source for yet is left out, never a 0.
- Chapter 01, "A year at a glance" (caption "Every day you read, and when."), opens with the header "Day by
  day" ("Each square is a day of 2026. Darker days held more pages. Tap one to see what you read."; the year
  is the selected calendar year, else today's), which describes the day grid under it. The grid is a
  `YearGrid` of the atlas year: twelve month rows, one square per day, tinted in four steps by quartiles of
  that year's reading days (days with equal pages share a step; with fewer than four reading days, or all
  equal, every read day is the darkest), with a "Fewer pages" to "More" key and future days left empty. It
  fades only when the year changes; the scope otherwise does not move it. Under it, "Current streak" (counted
  back from today, surviving until midnight) and "Longest streak" (the grid year's; a run across 1 January
  counts whole) tiles. A reading day everywhere has pages in the on-device history. A read day is tappable and reads
  out like "14 April, 62 pages". Tapping one opens a day-detail sheet with no haptic: the eyebrow is the date
  ("Tuesday 14 April", with the year only when it is not the current one) and the headline the total ("62
  pages across 2 books"), under a line "Day N of your longest streak." / "Day N of your current streak." /
  "Day N of a M-day streak." (none for a single day). Each book is a compact row (cover, title, a neutral badge with its
  pages that day) captioned with its pages and that book's update times as 24-hour clock times: "Pages 175 to 388 · 1 update
  at 14:41", "Pages 112 to 158 · 3 updates, 09:10 to 22:40" ("2 updates at 14:41" when the first and last
  share a minute), "Up to page 40 · 1 update at 08:05" when it started at page 0, with " · finished" added
  for a book finished that day ("Pages 380 to 412 · finished" when only its unlogged remainder fell on the
  day, with no time). A book no longer in the library is one untappable coverless row, "A book no longer in
  your library". "Done" closes the sheet; tapping a
  row closes it first and then opens that book's detail. A sync that empties the day closes the sheet. The
  first section, "When you read", is a scoped `ColumnChart` of the share of pages read in each
  three-hour block of the day, with the footnote "Each column covers three hours from the time shown.". Pages
  are the logged progress updates summed by local hour; a finished book's unlogged remainder has no time of
  day and is left out. Eight blocks start at 06 and are labelled "06" to "03". The busiest block is `Lead`,
  the next two `Second`, other non-zero `Third`, empty `Fifth`; ties go to the earliest block from 06. Every
  column shows its whole-percent share ("0%" for an empty block, "<1%" for a share that rounds to 0, ">99%"
  for one that rounds to 100 while another block has pages) and reads out as "21:00 to 00:00, 38% of your
  pages". The caption names the busiest block ("You read most after 21:00."; "early, before 09:00", "in the
  morning", "around midday", "in the afternoon", "in the early evening", "after 21:00", "after midnight", "in
  the small hours"). With no pages it drops the chart and says "No pages read in 2026." for a scope, or
  "Update your progress and your hours start to show." for All time. The labels never change, so the chart
  does not cross-fade.
- Chapter 02, "Seasons and records" (caption "Pages by month, authors and records."), opens with a
  headline naming the busiest month ("A March to remember", "An April to remember"; "A year in pages" when
  nothing was read) over a scoped pages-by-month `ColumnChart`. Pages are the pages read in each month,
  summed from the progress updates logged that month, so a book read across March and April counts in both
  and unfinished books count. When a book is finished in the scope and its last recorded position is short of
  its page count, the remainder lands on its finish date; a finished book with no updates puts all its pages
  there. It has a column for every calendar month the scope touches, so a rolling window shows 12 or 13
  months, 6 months 6 or 7, 30 days 1 to 3, a calendar year 12, and the first column of a window that starts
  mid-month is a partial month ("From 27 October 2025, 412 pages"). All time sums each calendar month across
  every year. Month labels are three letters, on every third column. The chart cross-fades only when the
  set of months changes (year to all time animates in place). A month after today with no pages is a "still
  to come" stub, named in the description.
  Under the chart, "Authors you return to" (caption "Your five most-read authors, with your average rating
  of their books.") lists up to five rows, ranked by books read in the scope. Only primary authors count:
  translators, illustrators, narrators, editors and cover artists are left out. Each row is a compact
  `BookCard` with the cover of the author's most recently finished book, the author's name, "N books" ("1
  book") and, trailing, a bare gold star with your average rating of those books ("4.6"), left out when none
  is rated. The rows are not tappable. With no finished books the caption gives the scope's sentence, or
  "Finish a few books and the authors you read most show up here." for All time; when books were finished
  but none lists an author it reads "None of these books lists an author." and the rows are dropped.
  Under that, "Print, ebook or audio" shows what share of the books finished in the scope were read in
  print, as an ebook, as an audiobook or in print and audio, as a `ProportionChart` with inline keys. A book
  counts once, under your own edition's format; books with no known format form a muted "Unknown" segment.
  The caption names the lead: "Mostly paper, with a third on a screen.", "Mostly ebooks.", "Only
  audiobooks.", or "Split between paper and ebooks." when no format passes half. When half or more of the
  books have no known format it reads "At least half of these books have no known format.", and with none known "None
  of these books has a known format.". With no finished books the caption gives the scope's sentence, or
  "Finish a few books and the formats you read in show up here." for All time, and the chart is dropped.
  The chart animates in place across scopes rather than cross-fading.
- Chapter 03, "Genres and how you read" (caption "What you reach for, and how it reads."), opens with a
  headline naming the top genre ("Mostly fantasy", each word lowercased unless it is all capitals, like
  "LGBTQ+"; a tie goes to the ranking's order) over a `RankedBars` ranking of the top five genres. A
  "Show more" outlined `PillButton` under the rows, present while genres remain hidden, reveals ten more
  per tap with a light haptic tick, and a "Show less" button beside it, present while more than five are
  shown, first scrolls the Atlas to the top of the chapter and then folds the ranking back to five. Each
  row past the visible count unfolds its own height and fades in, or folds its height, so the buttons
  ride the list's bottom edge; rows off screen when the fold starts drop out at once. The buttons fade
  and slide in and out. The header and its description stay the same throughout, so a tap animates only
  the rows and the buttons. Rows visible at first data, the loading swap and a scope switch do not
  animate in. A scope switch folds the ranking back to five. A scope with no genres says "The genres you read most" over its empty
  sentence, with no rows.
- Chapter 99, "Temporary" (removed later), holds the former Profile charts: "The shape of the years" (a
  `ColumnChart` behind a Books / Pages `SegmentedControl`); "Who you read";
  "How you score" (headline from the rating band, never the bare average); "Recently loved" (up to three
  4★+ reads). Each opens with eyebrow + italic headline (§3.2).

### Statistics charts

- Monochrome `primary` with `ChartTone` alphas; shape carries the data. Flat bars, never the wavy indicator.
- `ProportionChart` only for exclusive, exhaustive categories (gender: up to three ranked slices plus
  `Unknown` last, muted). `RankedBars` for overlapping ones (genres, each a share of books; position,
  name over a track, share, with each bar filled relative to the lead row, which is always full); the
  description names what the shares count.
- The format split is a `Keys` `ProportionChart` with a fixed format order and tones (Print `Lead`, Ebook
  `Second`, Audiobook `Third`, Print and audio `Fourth`), zero formats dropped and `Unknown` muted last.
  Shares are whole percents that sum to 100; a non-zero share never shows 0%.
- `ColumnChart`: books per year (peak `Lead`, others `Second`, zero a `Fifth` stub); pages per month (the
  peak `Lead` and the only column with a value, the next two `Second`, other non-zero `Third`, zero or
  future `Fifth`; ties go to the earliest month; labels on every third column); time of day (eight
  three-hour blocks, every column labelled and valued with its share); and the half-star
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
