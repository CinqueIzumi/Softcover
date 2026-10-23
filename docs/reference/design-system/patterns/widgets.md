# Patterns — Widgets

### Widget surface and theme

- `feature/widget`, Android only, Glance. `SoftcoverWidgetTheme` wraps every widget and resolves the app's
  `ThemeConfiguration` into a day and a night scheme: Light, Dark and Dim pin both to one brightness, System
  maps day to Light and night to Dark. Palette follows `ColorPalette`; dynamic colour applies on API 31+ except
  under Dim.
- Paper is `background` / `onBackground`, never Glance's `widgetBackground` (it maps to `secondaryContainer`,
  an accent tint). A widget showing a book sits on `surfaceContainerHigh` under its backdrop; message states
  (loading, signed out, empty) sit on `background`.
- `WidgetExtraColors` carries what `ColorProviders` lacks: `surfaceContainerHigh`, `daySurfaceContainerHigh`
  (transparent at night), the 70% `surface` `arrowScrim` and the 16% `primary` `quoteGlyph`.
- Type is the system serif (titles, headlines, `FontFamily.Serif`) and sans (everything else); Glance cannot
  load Fraunces or Inter. Spacing reads `Spacing.*`, radii `CornerRadius.*`.
- The widget corner follows `system_app_widget_background_radius` on API 31+; below it the launcher clips.

### Widget backdrop and shared pieces

- `Backdrop` layers, bottom up: the blurred cover (`BackdropArtworkRenderer`, rendered at half scale, 110dp
  by 180dp, Crop-scaled to every size) or, with no cover, a `primary` and `tertiary` gradient wash; then the
  `veil` and, in light only, the `veilLift`, both tinted `surfaceContainerHigh`. Gradients are white alpha
  masks tinted at draw time, so a palette or day/night switch needs no re-render.
- Widgets title a book with `Book.displayTitle`: the reader's own edition title, else the book title. The
  book's default edition never supplies a title. The progress sheet heading follows the same rule.
- `ReservedLinesTitle`: bold 14sp serif centred, up to two lines, holding the height of `reservedLines`
  lines (an invisible text holds it, Glance has no `minLines`).
- `SingleBookWidgetLayout` is the book state of Currently Reading and Random pick; each widget passes only its
  eyebrow and foot row. Both run on `SizeMode.Exact`, so the layout sees the widget's real size, and picks:
  - **Stacked** when the widget is not wider than tall: eyebrow, cover (inset `Spacing.xs` each side), title,
    author, one centred foot row. Under 150dp only the cover and the foot row (plus Currently Reading's
    eyebrow); from 150dp the eyebrow, the title reserving one line and the author; from 180dp the title
    reserves two lines.
  - **Side by side** when wider than tall: the 2:3 cover fills the height at the start, inset `Spacing.xs` from the
    top, bottom and start edges, and the text column
    beside it holds the eyebrow, the title (start-aligned, as many of up to three lines as fit) and author
    centred in the space left, and the foot row. The author drops when not even one title line fits beside it.
- Message states (loading, signed out, empty) share `WidgetMessage`: a centred serif headline and sans
  subline.
- The wavy progress bar is a bitmap pair (`WavyProgressBitmaps`: track, indicator) from a pure
  `CanvasDrawScope` renderer matching the app's `ProgressBar`, 12dp tall, pre-rendered at the bar width
  (`progressBarWidthDp`) of every size the launcher reports for the instance (`reportedWidgetSizes`, matching
  what Glance composes) and re-rendered when a resize changes them, tinted `secondaryContainer` and `primary`.
- Hit boxes grow in width, not height, where height would shrink the covers: Trending's header and page
  row stay 28dp tall with 40dp-wide tune and page targets; the shuffle button is 48dp; the Random pick
  and Currently Reading eyebrows are 28dp, with 40dp-wide cycle arrows (`CycleEyebrow`) on Currently Reading's
  and the Quote widget's.

### Currently Reading widget

- Min 110 x 180dp, resizable to 110 x 110dp and to any width. Uses `SingleBookWidgetLayout`. Covers render
  at the tallest cover slot of the reported sizes, again only when that height changes.
- Anatomy: the same skeleton as Random pick. The eyebrow reads `READING · n/m` (`READING` with one book)
  and, with more than one book, holds ‹ › glyphs at its ends that cycle the shelf (most recently updated
  first, wraps); the choice is kept by book id per instance and falls back to the first. The eyebrow shows
  at every height, as it holds the only cycle control. Below it the cover (fit, nothing over it), title and
  author, then one centred foot row: the wavy bar followed by "xx%" and an edit glyph.
- Taps: the widget opens the Reading tab (`OpenTab(READING)`); the foot row opens
  `UpdateProgressActivity`, the progress bottom sheet over the launcher.
- Copy: **Between books** / "The next one is waiting on your shelf."; signed out **Sign in to Softcover**.
  No settings; no configuration activity.

### Random pick widget

- Same look, backdrop, sizing and `SingleBookWidgetLayout` as Currently Reading (min 110 x 180dp, resizable
  to 110dp tall), with the shuffle button as its foot row. Its eyebrow is left out under 150dp.
- Eyebrow `WHAT NEXT? · <SOURCE>` (Want to read, Owned, or the list's name), tapping it opens the settings
  activity. The 48dp shuffle button picks again (`ShuffleCallback`).
- Taps: a picked book opens its detail (`OpenBook(bookId)`); the empty and signed-out states open the Reading
  tab. Empty: **Nothing waiting here yet** / "Add a few books, or pick another shelf."; signed out: **Sign in
  to Softcover**.
- Settings: **Pick from** (Want to Read by default, Owned, or any of the reader's lists; a deleted list falls
  back to Want to Read) and **New pick** cadence (Daily by default, Weekly, Only when I shuffle). The pool is
  the source's not-started books; a pick that leaves the pool is replaced.
- A pick drawn from a list shows the list entry's edition (`ListBook.shownEdition`: the reader's own edition,
  else the entry's, or the owned row on Owned) for its title, author, cover and backdrop; a blank edition
  title or author falls back to the book's. A Want to read pick uses the book as `Book.displayTitle` does.

### Trending widget

- Landscape, min 250 x 110dp, resizable, on `SizeMode.Exact`, so the two cells span its real width. Two books per page
  of the period's top ten, with `1/5` and 40dp-wide page chevrons when there is more than one page.
- Anatomy: a 28dp header (eyebrow, then a 40dp-wide tune target), two book cells, the page row. Under 150dp tall a
  cell puts rank and caption beside the cover; from 150dp it stacks them under it. The cover height fills
  what the header, caption and page row leave, capped so a 2:3 cover fits its cell's width
  (`trendingCoverHeightDp`). Covers render at the tallest reported size and re-render on resize.
- Eyebrow per period: TRENDING THIS WEEK, THIS MONTH, PAST 3 MONTHS, PAST YEAR, ALL TIME.
- Taps: a cover cell opens the book (`OpenBook`); the eyebrow opens the Explore tab; the tune glyph opens the
  settings activity; the rest opens the Reading tab. Empty: **Nothing is trending yet** / "Check back after the
  next refresh."; signed out: **Sign in to Softcover**.
- Settings: the period under "Which list should it show?" (This week by default, This month, Past 3 months, Past year, All time) and **Refresh**
  (Daily by default, Twice daily). Both screens are titled "Widget settings". The widget reads the per-period Room cache the Explore tab also reads.

### Quote of the day widget

- Landscape, min 250 x 110dp, resizable; buckets 250, 320 and 390dp wide by 110, 180 and 250dp tall. Paper
  `background` under the shared `Backdrop`, blurred from the quote's own edition cover (stored on the quote,
  so a book outside the library still has one) or the washes without it; a low-alpha `quoteGlyph` opening
  mark, a vector drawable, sits left of the quote, top-aligned with it.
- Anatomy: a `CycleEyebrow` (`QUOTE OF THE DAY`, or `QUOTES · n/m` once the reader steps off the first
  quote), then the quote in italic serif `onSurface` and a 12sp `onSurfaceVariant` byline "Edition title ·
  position" (book title
  when the quote has no edition title). The quote autosizes from 18sp down to 12sp to fit the space above the
  byline.
- A hash of each quote's content and the date orders the reader's quotes, so the first changes at midnight.
  Arrows (only with more than one quote) walk that order and wrap; the pick is kept per instance with its day
  and lapses at midnight.
- Taps: the quote opens its book (`OpenBook`); the rest opens the Reading tab. Empty: **No quotes yet** /
  "Save a quote from a book's Yours tab."; signed out **Sign in to Softcover**. No settings.

### Widget configuration activities

- `WidgetSettingsActivity` hosts a TOAD screen in `Navigator`. It rejects an id whose provider is not its own
  receiver (`RESULT_CANCELED`), and otherwise sets `RESULT_OK` with the widget id before any choice, so Back
  keeps the widget placed on its defaults. A choice writes straight through and repaints.
- Random pick and Trending declare it as `android:configure` and `reconfigurable`; it opens on every
  placement and from the widget's header. Currently Reading has none.
- `WidgetSettingsLayout`: the app theme (not the widget theme), a `TopBar` with Back, then `SectionHeader`
  and `ChipFlow` rows of choice chips, in a scrolling column of `Spacing.md` gaps, content width capped.
- Settings > Personalisation > Widgets lists placed instances (title, a "Source, cadence" summary or
  **Nothing to set up**, an edit action when it has settings) under **On your home screen**, then an **Add a
  widget** row per kind where the launcher can pin (`HomeScreenWidgets.canPin`), else a gloss to long-press
  the home screen.
