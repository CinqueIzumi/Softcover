# Design System — Components

Shared components; a `detail` link opens the family file with the full contract.

## Navigation & chrome

- **Top app bar** — `core:component/topbar` — page title, back/trailing actions, opaque or over-media.
- **Editorial top bar** — `core:component/topbar` — desktop page bar: back affordance, start-aligned editorial headline, trailing actions.
- **Search top app bar** — `core:component/topbar` — mobile search chrome.
- **Section header** — `core:component/header` — region opener in three accent-bar registers.
- **Page masthead** — `core:component/header` — a page's title block.
- **Masthead refresh action** — `core:component/header` — trailing refresh button.
- **Sidebar** — `core:component/sidebar` — a desktop source list of grouped, selectable rows.
- **DesktopScrollPage** — `core:component/layout` (jvmMain) — the desktop scroll page shell with top bar, masthead and bottom bar slots; anatomy in `layout.md`.
- **Editorial search field** — [detail](components/navigation.md#editorial-search-field) — persistent search pill for editorial headers.
- **Bottom navigation** — [detail](components/navigation.md#bottom-navigation) — compact-width tab chrome, docked or floating.
- **Navigation rail** — [detail](components/navigation.md#navigation-rail) — medium-width tab chrome.
- **Editorial sidebar** — [detail](components/navigation.md#editorial-sidebar) — expanded-width tab chrome.
- **Session peek bar** — [detail](components/navigation.md#session-peek-bar) — live-timer bar for a session.
- **Settings rows** — [detail](components/navigation.md#settings-rows) — one anatomy for every Settings row.

## Buttons & controls

- **Button** — [detail](components/buttons-controls.md#button) — five styles/sizes; filled is one primary per region.
- **Pill button** — `core:component/button` — action pill or icon-only circle in five tones.
- **Toggle** — `core:component/control` — two-state control: filled or bare icon toggle, or a display-only switch inside a row that owns the tap.
- **Text field** — `core:component/field` — single-line input as a tonal pill, the Material outlined box, a display-size hero, or a centred numeral.
- **Segmented control** — `core:component/control` — one-of-N choice as a pill track or as words in a sentence.
- **Split button** — [detail](components/buttons-controls.md#split-button) — primary action with a chevron menu of variants.
- **Theme preview tile** — `core:component/control` — mini-page theme choice.
- **Colour palette preview tile** — `core:component/control` — mini-page spine-colour choice.
- **Preview tile frame** — `core:component/control` — tiles' frame, `PageMiniature`, `PreviewTileEvent`.
- **Swipe row actions** — [detail](components/buttons-controls.md#swipe-row-actions) — swipe-to-mark-as-read / swipe-to-remove.

## Chips

- **Pill chip** — `core:component/chip` — tonal/choice/filled/container/outlined/dashed/spoiler tone + slots.
- **Chip flow** — [detail](components/chips.md#chip-flow) — wrapping row of a `ChipSet`.
- **Expandable flow row** — [detail](components/chips.md#expandable-flow-row) — wrapping chip/tag container, collapsing past N lines.

## Covers

- **Cover** — `core:component/cover` — the app's only book-cover renderer; variant-driven, never blank.
- **Coverless title cover** — `core:component/cover` — the monogram/title jacket `Cover` falls back to.
- **Book card** — `core:component/bookcard` — the one book/series card (cover-only, column or row) for every list, rail and grid surface; `bookCardStaggeredEntry` skips the entry animation for stand-ins.
- **Rhaydus shimmer image** — [detail](components/covers.md#rhaydus-shimmer-image) — the loader for every non-cover image.

## States & feedback

- **Inline error state** — [detail](components/states-feedback.md#inline-error-state) — message + retry for a `String?` error.
- **Banner** — `core:component/callout` — full-width slide-in strip for a screen-level condition or a card-fused nudge; `BannerVariant` (`Status`, `Editorial`) sets colour, voice, icon and dismiss.
- **Empty state** — `core:component/state` — placeholder for content a surface has nothing to draw; `EmptyStateVariant.Centered(button?)` (quote glyph, headline, optional button) or `Inline` (start-aligned title and body); the caller pads it full pane or in flow.
- **SkeletonMask** — `core:component/state` — `Modifier.skeletonMask(visible, shape)`: shimmers a leaf over its bounds while loading, keeping its real layout.
- **Divider** — `core:component/divider` — hairline rule, bare, centred, or vertical.
- **Pull-to-refresh indicator** — [detail](components/states-feedback.md#pull-to-refresh-indicator) — exception to the wavy-progress rule.
- **Pull-to-refresh eyebrow** — [detail](components/states-feedback.md#pull-to-refresh-eyebrow) — eyebrow copy synced to pull progress.
- **Deadline row** — `core:component/deadline` — "Finish by" card: icon, title, status `Badge`, goal sentence.
- **Deadline badge / cover overlay / summary line** — `core:component/badge` + `deadline` — deadline marks.
- **Revealed when present** — `core:component/state` — expands a row vertically while its value is non-null.
- **Unreleased badge** — `core:component/badge` — `Primary` tone; resolved in `core:uibinding` `release/`.
- **Selection mark** — `core:component/selection` — selected-state circle mark for a cover, row or edition card.
- **Mark-as-read celebration** — `core:component/celebration` — mark-as-read particle burst.
- **Search results pagination** (feature-owned) — [detail](components/states-feedback.md#search-results-pagination) — append-on-scroll paging.
- **Connectivity chrome** — [detail](components/states-feedback.md#connectivity-chrome) — `Banner`, `EmptyState` and `rememberIsOnline()` for offline.
- **App snackbar host** — `orchestration` `App` — on phones it rides above the compact bottom chrome via `BottomChromeAnchor` (bar plus session peek bar, both styles); with no chrome on screen it sits at the navigation-bar inset.
- **Callout** — `core:component/callout` — in-flow card for one condition, optionally with a single next step; `CalloutVariant` is `Neutral` or `Highlight`. `Banner` and `Callout` share one dismiss button (`DismissButton`, internal).

## Sheets

- **Sheet scaffold** — [detail](components/sheet-scaffold.md) — chrome, rows, close.
- **Sheet row count** — `core:component/sheet` — a row's trailing count and spoken text.
- **Loading sheet** — `core:component/sheet` — full-screen blocking wavy-progress wait.
- **Date picker dialog** — `core:component/dialog` — single-day calendar dialog; owns the UTC-millis conversion.
- **Update progress sheet** — `core:component/progress` — pages/percentage/time reading-progress editor.
- **Progress bar** — `core:component/progress` — determinate wavy bar; the wave holds still.
- **Because-you-read genre picker** (feature-owned) — [detail](components/sheets.md#because-you-read-genre-picker) — genre-override sheet.
- **Tag editor sheet** (feature-local) — [detail](components/sheets.md#tag-editor-sheet) — manages the user's own tags on a book.
- **Choose-lists sheet** — `core:component/lists` — add/remove one or many books to the user's custom lists.
- **Change edition sheet** (feature-local) — [detail](components/sheets.md#change-edition-sheet) — picks which edition the reader tracks.

## Statistics

- **Animated stat number** — `core:component/statistic` — tweens a stat into place.
- **Stat tile** — `core:component/statistic` — a labelled stat on a hero, standard or compact tile; only the number masks while loading.
- **Ranked bars** — `core:component/chart` — a bar per category on its own 100% track.
- **Proportion chart** — `core:component/chart` — a stacked bar with its legend.
- **Editorial figure / chart** — [detail](components/statistics.md#editorial-figure--chart) — the editorial charts.

## Share

- **Share card** — `core:component/share` — one `ShareCardUiModel` renderer; `ScaledShareCardPreview` scales it.
- **Share card sheet** — `core:component/share` — `ShareCardSheetContent`: the one share sheet; owns capture, busy and snackbars.

## Rich text

- **Softcover text** — `core:component/text` — the one text leaf; lays out `standIn` under `skeletonMask` while loading or while `text` is null.
- **Clickable text** — `core:component/richtext` — prose with a tappable substring resolved to a link event.
- **Rich text** — `core:component/richtext` — read-only formatted, spoiler-maskable prose.
- **Rich-text formatting toolbar** — `core:component/richtext` — bold/italic/spoiler chip row inside `VerdictSheet`.

## Editorial

- **Drop-cap text** — [detail](components/editorial.md#drop-cap-text) — body prose with a 3-line drop cap.
- **Editable hero-stat field** — [detail](components/editorial.md#editable-hero-stat-field) — hero-numeral input for progress sheets.
- **Star rating input** — [detail](components/editorial.md#star-rating-input) — half-star interactive/read-only rating control.
- **Verdict sheet** — `core:component/verdict` — combined rating-and-review surface.
- **Verdict block** — `core:component/verdict` — book-page rating+review card that opens the verdict sheet.
- **Personal review section** — [detail](components/editorial.md#personal-review-section) — book-detail entry point to review editor.
- **Browse-by-mood grid** (feature-owned) — [detail](components/editorial.md#browse-by-mood-grid) — 2-column mood-tile grid.

## Platform helpers

- **Desktop tooltip** — [detail](components/platform-helpers.md#desktop-tooltip) — hover tooltip on desktop, no-op on touch.
- **Desktop context menu** — [detail](components/platform-helpers.md#desktop-context-menu) — right-click menu on desktop, no-op on touch.
- **Haptics helper** — [detail](components/platform-helpers.md#haptics-helper) — eight-case entry point for haptic feedback.
- **Lazy-item mutation animator** — [detail](components/platform-helpers.md#lazy-item-mutation-animator) — animates list add/move/remove.
- **Staggered entry coordinator** — [detail](components/platform-helpers.md#staggered-entry-coordinator) — stagger for carousels/lists.
- **Barcode scanner** (feature-owned) — [detail](components/platform-helpers.md#barcode-scanner) — CameraX/ML Kit ISBN-scan surface.
- **Active reading session** — [detail](components/platform-helpers.md#active-reading-session) — controller behind peek bar and Focus Mode.
