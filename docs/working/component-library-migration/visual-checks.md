# Visual checks — final UI pass (S12)

Accepted visual shifts to confirm on device and in the gallery. Append one line per step that lands a
shift; tick each during S12.

- [ ] The Arrange / Filter sheets no longer render empty or stale for a frame on open.
- [ ] The Arrange sheet's "Show titles" row does not pop in a frame after open (collector-only writer now).
- [ ] S5-6: 4dp eyebrow gap, 1-line titles, reading's mobile masthead line height.
- [ ] S5-C-headers: "Your tabs" tracking (2.2 → 1.2sp); `FeaturedCard`'s eyebrow gap (10 → 8dp).
- [ ] The whole badge family in the gallery and on device (never visually checked since S4-5b): SemiBold
  everywhere, TrackingTag pill → 6dp, Owned 14 → 12dp icon, cover selection mark 26 → 24dp, Expired on
  `surfaceContainerHigh`.
- [ ] S5-C-dividers: quote-glyph gap (14 → 12dp), the "Or type it" label on plain `eyebrowSmall`, the
  version footer on `eyebrowSmall`.
- [ ] S6-C-rows: settings rows (16dp vertical everywhere, 4dp title gap, top rule and press feedback on
  toggle / choice lists).
- [ ] S6-3: sheet header gaps (12/8/8), 56dp jackets, sheet padding (top 8 / bottom 16, LoadingSheet
  32 → 16), the progress sheet's tap-to-clear-focus covering the body only.
- [ ] S6-4: verdict cover as a 56dp header jacket (was above the header).
- [ ] S6-5: Filter footer inset (22 → 24), bulk-remove and re-auth as sheets, the animated close on
  Apply / Save / Delete / top action.
- [ ] S6-6: tag editor and edition jackets (56dp), the edition footer divider, ShareBook's eyebrow bar,
  the unknown-ISBN footer.
- [ ] S6-7: streak sheet insets, log-out no longer centred, the genre / Continue-series headers.
- [ ] S6-C-sheets: header trailing gap (16 → 12), verdict jacket radius 4, the TEXT dismiss label's right
  edge, ShareBook's TEXT Cancel, animated state-driven closes.
- [ ] SL-5: the hero title block while loading.
- [ ] S7-2/4/5/6/7: cards.
- [ ] SL-10b/11: loading.
- [ ] Desktop chip gap (24dp).
- [ ] S7-C2: bars. S7-C3: eyebrows, bylines. S7-C5: uncrossfaded swap.
- [ ] S8-1: quote glyph now on the offline screen and the desktop detail pane; Library and tag-editor glyph
  tint (`primary` → `onSurfaceVariant`) and type one step up; Reading's glyph at standard size and its
  "Find a book" pill as a `RhaydusButton`; tag editor gaps 6/4 → 8/12; Library's empty state top-aligned.
- [ ] S8-2: callouts at 16 dp radius (status 24, app update 12); status eyebrow `eyebrowSmall` primary and body
  `bodySmall`; app-update pill → filled `RhaydusButton`; pace nudge animates out, says "Dismiss".
- [ ] S8-3: desktop back bars' end padding 16 → 8; desktop book detail's bar gains 8 dp on top; onboarding's accent bar now cross-fades with its eyebrow.
- [ ] S8-C-a: desktop Profile and Scan back bar (`DesktopBackStrip` → `EditorialTopBar`: padding, editorial title on Scan); SearchTopBar clear "×" now a 48 dp tap target, field and scan button 46 → 48 dp tall, scan button radius 14 → 16.
- [ ] S8-C-b: app-update callout accent bar 30×4 → 20×1 hairline, gap 8; Callout and Banner dismiss targets 48 dp (Banner taller when dismissible); Banner padding 16×10 in both variants; offline banner colour fills behind the status bar; Banner dismiss glyph sits ~32 dp from the edge, Callout's lower against the eyebrow — check on device.
- [ ] S8-C-c: inline empty-state title titleLarge → headlineSmall; Library empty book/edition list loses its 32 dp bottom spacing (top 40, sides 32); tag editor empty state padding now 10 sides + 40 top; offline screens blank for one frame at a cold offline start; Reading's empty block centred on phones, 40 dp top on adaptive.
- [ ] S8-6: pills move to fixed heights (38 / 44 dp) with one metric set (17 dp icon, 6 dp gap, `labelMedium` SemiBold, 14 dp side padding); tag editor "Add" `labelLarge` → `labelMedium`; external-link pills' icon 16 → 17 dp and height from 10 dp padding to fixed 38 dp; the Update pill's icon 15 → 17 dp; Library select, exit and trash glyphs 18/19 → 17 dp; Explore sort pill's ink `onSurfaceVariant` → `onSurface`, now 38 dp tall.
- [ ] Row gutter: Reading's also-reading rows regain 24 dp sides / 18 dp top-bottom (divider + hover full-bleed); Library list rows 12 → 14 dp (compact) / 18 dp (large) top-bottom.
- [ ] S8-4: share sheet variant toggle, Profile year metric and progress-sheet tabs move from M3 outlined segmented buttons to the filled pill track (primary selected segment); a disabled year metric / share toggle now dims at 40% instead of M3's outline treatment.
- [ ] S9-2: demographic "No" fill alpha .55 → .48 (`ChartTone.Fourth`); genre ranking and author-representation charts now load as their real rows and legend with masked text, swatches and tracks (was a shimmer over bare tracks / the whole bar and legend).
- [ ] S9-3: year and ratings charts share one column metric set — ratings plot 88 → 96 dp, bars 14 dp-capped at 72% / 48% of the slot → 20 dp capped to the slot, corners 2 → 3 dp, minimum 10 → 12 dp, gaps 4 → 6 dp; half-stars full width at .55 → .48; an empty ratings bucket now a 3 dp stub; year zero column .24 → .34; both load as their real columns with masked bars and labels (was a shimmer).
- [ ] S9-4: featured-card bar 10 → 12 dp, book-detail in-progress bar 14 → 12 dp; Focus, featured and in-progress bars animate on change.
- [ ] S9-C: library and reading card bars 8 dp / M3 default → 12 dp, M3 track colour (was `outlineVariant`), animated (check the grid card); card % label masks alone while loading; genre and legend rows one 12 dp label-to-share gap, legend labels ellipsize.
- [ ] S8-C-controls: 48 dp hit areas on toggles, segments and field affordances; ghost placeholder ink on every field; Numeral's "0" placeholder; Hero field at a fixed height; segmented controls in the Track look; Library arrange switch rows press with the row wash; disabled pills and chips 45% → 40%.
- [ ] SS-3: gallery coverless `Edition` fixture's selection mark Image → Surface backdrop; Explore search-result loading card's series eyebrow uppercased (wider stand-in).
- [ ] SS-4: Explore hidden-series stack covers 3 → 4 dp corner; Reading row thumb 6 → 8 dp and pick-up-next tile 10 → 8 dp corner (trending tile unchanged at 8 dp).
- [ ] SS-6: verdict sheet and Library `ToggleRow` titles take the `Plain` / `Inset` variant's type instead of a per-call style.
- [ ] SS-8: Explore mood grid tiles no longer crossfade from loading to loaded.
- [ ] SS-7: book detail reviews show their stand-ins and load independently of the book details (no longer held behind the details fetch).
- [ ] SS-10: book share card's rating now shows the ×2 verdict score, matching the verdict block.
- [ ] SS-11: gallery drops DeadlineSummary "Expired" and TextField "Hero, long value"; palette tiles read "Selected" / "Unselected".
- [ ] SS-C: share sheets lose Cancel (Share primary, Save secondary, close only on success); share card "p. 120 of 300 · 40%" and tag chips; library "Make this list ordered" as a text button (no ✶); explore chip gap and when-read stepper 9 → 8 dp, stepper icons; pace text ends "/day" ("0h 30m/day").
- [ ] SD-2/3 (desktop): Reading rows enter staggered while loading; Library filter chips animate in and out, and on both platforms the last chips now fade while the row collapses (was an empty row shrinking); Library list stand-ins 12 → 6 (large) / 10 (compact); two-column book detail shows the scan-edition callout under the shelve card, 20 dp gap.
- [ ] SD-1/4/5/6: desktop Explore shows the search error with retry and its rails enter staggered; mobile Featured gutter 16 → 24 dp; desktop hidden suggestions and every settings screen / pane on the shared desktop page shell (padding, gutter, masthead spacing, bottom-bar inset); settings copy "Your profile", "Personalisation", hidden suggestions under Personalisation (no Privacy section), About icon `Info`; desktop Profile bio separator, header → atlas 36 dp, single-column foot 40 + 24 dp.
- [ ] Never checked on device: lens toggle TalkBack ("selected, tab"); Numeral select-all vs tap cursor; fast typing in time fields; scan manual-entry focus (desktop); share card rating; `StatTile`, charts, `ProgressBar`, 12 dp card bars in the gallery; desktop Profile single-column and search results; onboarding page-3 keyboard gap; Library tabs discard sheet (system back, exit animation, swipe while saving); Focus Mode exit from the notification; Explore no-results; Undo snackbars; snackbar above both bar styles; tablet back and desktop Esc.
