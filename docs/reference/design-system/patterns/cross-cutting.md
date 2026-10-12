# Patterns — Cross-cutting

### List-detail two-pane

- On expanded windows (§2.7) a root list surface keeps its list in a fixed-width leading pane and the
  selected book's detail in the trailing pane (`TwoPaneScaffold`, §3.6). The pane swaps instantly, with no
  cover morph; before a pick it shows a quiet editorial placeholder.
- `BookDetailPresenter` owns push-vs-pane: compact and medium widths, and a tab that is not detail-capable,
  push the detail full-screen. Focus Mode, the scanner, the full-screen cover and onboarding stay full-bleed.

### Section footprint reserved while loading

- A section driven by its own `isLoading` keeps its footprint for the whole loading phase (real header,
  masked leaves), so it never pops in from zero height.
- Collapse to nothing only on `isLoading.not() && <nothing to show>`, never "any unresolved value means
  loading". Example: `BecauseYouReadGenreControl` (`model.isLoading.not() && model.hasOptions.not()`).
- A section or trailing line that arrives on its own timer and often never arrives reserves nothing and
  reveals in place (`RevealedWhenPresent`) instead of popping in.

### Sheet host

- The open action sets a request input on UiState (a flag or an id) and does nothing else.
- One collector per sheet builds the model from that input and its data and is the only writer of the
  `SheetSession`: `openOrUpdate` while the input is set, `requestClose()` otherwise. It re-reads the input
  inside its `setState` lambda, so a stale snapshot cannot reopen a sheet an action has closed.
- An action that closes a collector-owned sheet clears the request input and leaves the hide to the
  collector; one that must record a `pending` intent calls `requestClose(pending)` in the same `setState`.
- The screen renders from `session.modelOrNull`, sends `Dismissed` to an action that sets the session to
  `Closed`, then runs `dismissed()`'s pending intent. It never copies state into the model at render time.

### Blocking loading sheet

- A long blocking wait is a non-dismissible sheet, never a dialog: onboarding's
  `onboardingLoadingSheet(progress)` builds a `LoadingSheetUiModel` over
  `SheetScaffoldUiModel(dismissible = false)` with the canonical header (eyebrow "Setting up", headline
  "Getting your shelves ready…", a short description), then wavy progress. No close button.

### App-shell navigation chrome

- `:orchestration` (`orchestration/presentation/`). Compact: `CompactNavShell` renders one foundation
  `BottomBarScaffold` and maps the `BottomBarStyle` preference onto `BottomBarPlacement.DOCKED` / `.OVERLAY`,
  so switching re-places the bar without rebuilding the tab body (`DockedBottomNavigationBar`,
  `BottomFloatingBar`, whose transparent tap shield keeps gap taps off the content beneath).
- Medium (600–840dp): `NavigationRail`. Expanded (≥ 840dp): a permanent sidebar, wordmark in `pageTitle`
  over the four destinations, active row `secondaryContainer` / `onSecondaryContainer`, idle
  `onSurfaceVariant`.
- All three read one `rememberNavItems()` source, so selection, the app-update badge and the library pulse
  never drift. They enumerate feature tabs, so they are app-shell code, not `:core:component`.

### Barcode scanner

- `:feature:scan`, `BarcodeScanner`: full-bleed CameraX back-camera preview into bundled ML Kit,
  `FORMAT_EAN_13` / `FORMAT_EAN_8` only; emits the first raw value once via `onIsbnDetected(raw)`, then
  stops analysing. Resolution belongs to the scan ScreenModel.
- Chrome: a white `SCANNING` eyebrow and `headlineMedium` instruction over a scrim (the one light-on-media
  copy surface) and a centred rounded reticle.
- Permission via `rememberCameraPermissionRequester`; denied or no camera falls back to manual ISBN entry.

### Inline filter chip strip

- A row of `Chip`s directly above a grid or list that scopes it, led by an "All …" chip that clears the
  filter. Selected `secondaryContainer` / `onSecondaryContainer`, idle `surfaceContainer` /
  `onSurfaceVariant`.
- Compact and medium scroll horizontally; expanded wraps (`FlowRow`).

### Active-filter chip row

- Only the currently applied filters, as removable chips (trailing close glyph), with the picker in a
  separate sheet; chips enter and leave with `expandHorizontally` + fade. Two or more active chips append a
  tonal "Clear all" chip.
- Compact and medium scroll horizontally; expanded wraps (`FlowRow`). Library fills them `primaryContainer`
  / `onPrimaryContainer`; another consumer picks the container pair that contrasts with its own surface.

### Pace-nudge ribbon

- A dismissable `Banner` fused to the top of the card it speaks to, never an in-flow row above it.
- Reading's featured-hero card uses it for a per-day pace target from the book's deadline; dismissal holds
  per book per day.

### Actionable inline banner

- A dismissable `Callout` with one action, for a transient, condition-gated prompt with a concrete next
  step; its own list item, directly beneath the section it speaks to.
- Book detail's `ScanEditionCallout`: arriving from a scan on a book shelved with a different edition, the
  action switches the shelved edition; dismiss hides the offer and keeps the scanned edition displayed.

### Adaptive empty state

- An empty root surface shows the next-best content, not a static panel. Empty Reading: the top three
  Want-to-Read books as a "Pick up next" row, else one "Trending now" tile into book detail.
- Paired with `EmptyState`'s quote glyph and headline; never three stacked carousels.
