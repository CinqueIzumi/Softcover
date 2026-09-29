# Design System — Components

Shared components; a `detail` link opens the family file with the full contract.

## Navigation & chrome

- **Top app bar** — `core:component/topbar` — page title, back/trailing actions, opaque or over-media.
- **Search top app bar** — `core:component/topbar` — mobile search chrome.
- **Section header** — `core:component/header` — region opener in three accent-bar registers.
- **Page masthead** — `core:component/header` — a page's title block.
- **Masthead refresh action** — `core:component/header` — trailing refresh button.
- **Sidebar** — `core:component/sidebar` — a desktop source list of grouped, selectable rows.
- **Editorial search field** — [detail](components/navigation.md#editorial-search-field) — persistent search pill for editorial headers.
- **Bottom navigation** — [detail](components/navigation.md#bottom-navigation) — compact-width tab chrome, docked or floating.
- **Navigation rail** — [detail](components/navigation.md#navigation-rail) — medium-width tab chrome.
- **Editorial sidebar** — [detail](components/navigation.md#editorial-sidebar) — expanded-width tab chrome.
- **Session peek bar** — [detail](components/navigation.md#session-peek-bar) — live-timer bar for a session.
- **Settings menu row** — [detail](components/navigation.md#settings-menu-row) — hairline-separated row anatomy shared by the Settings menu and About.

## Buttons & controls

- **Button** — [detail](components/buttons-controls.md#button) — five styles/sizes; filled is one primary per region.
- **Toggle button / icon toggle button** — [detail](components/buttons-controls.md#toggle-buttons) — shape-morph active state.
- **Split button** — [detail](components/buttons-controls.md#split-button) — primary action with a chevron menu of variants.
- **Theme preview tile** — `core:component/control` — mini-page theme choice.
- **Colour palette preview tile** — `core:component/control` — mini-page spine-colour choice.
- **Preview tile frame** — `core:component/control` — tiles' internal frame primitives.
- **Bookmark toggle** — [detail](components/buttons-controls.md#bookmark-toggle) — inline add/remove-library row control.
- **Swipe row actions** — [detail](components/buttons-controls.md#swipe-row-actions) — swipe-to-mark-as-read / swipe-to-remove.

## Chips

- **Pill chip** — `core:component/chip` — tonal/choice/filled/container/outlined/dashed/spoiler tone + slots.
- **Expandable flow row** — [detail](components/chips.md#expandable-flow-row) — wrapping chip/tag container, collapsing past N lines.

## Covers

- **Cover** — `core:component/cover` — the app's only book-cover renderer; variant-driven, never blank.
- **Coverless title cover** — `core:component/cover` — the monogram/title jacket `Cover` falls back to.
- **Rhaydus shimmer image** — [detail](components/covers.md#rhaydus-shimmer-image) — the loader for every non-cover image.

## States & feedback

- **Inline error state** — [detail](components/states-feedback.md#inline-error-state) — message + retry for a `String?` error.
- **Banner** — `core:component/callout` — full-width slide-in chrome for a screen-level condition.
- **Empty state** — `core:component/state` — centred headline-and-body placeholder.
- **Skeleton** — `core:component/state` — per-surface shimmer placeholder matching its loaded footprint.
- **Divider** — `core:component/divider` — hairline rule, bare, centred, or vertical.
- **Pull-to-refresh indicator** — [detail](components/states-feedback.md#pull-to-refresh-indicator) — exception to the wavy-progress rule.
- **Pull-to-refresh eyebrow** — [detail](components/states-feedback.md#pull-to-refresh-eyebrow) — eyebrow copy synced to pull progress.
- **Deadline badge / cover overlay / summary line** — `core:component/badge` + `deadline` — deadline marks.
- **Unreleased badge** — `core:component/badge` — `Primary` tone; resolved in `core:uibinding` `release/`.
- **Selection mark** — `core:component/selection` — selected-state circle mark for a cover, row or edition card.
- **Mark-as-read celebration** — `core:component/celebration` — mark-as-read particle burst.
- **Search results pagination** (feature-owned) — [detail](components/states-feedback.md#search-results-pagination) — append-on-scroll paging.
- **Connectivity chrome** — [detail](components/states-feedback.md#connectivity-chrome) — `Banner` + `EmptyState` + `rememberIsOnline()`.
- **Update highlight card** — [detail](components/states-feedback.md#update-highlight-card) — Settings app-update callout.

## Sheets

- **Loading sheet** — `core:component/sheet` — full-screen blocking wavy-progress wait.
- **Date picker dialog** — `core:component/dialog` — single-day calendar dialog; owns the UTC-millis conversion.
- **Update progress sheet** — `core:component/progress` — pages/percentage/time reading-progress editor.
- **Because-you-read genre picker** (feature-owned) — [detail](components/sheets.md#because-you-read-genre-picker) — genre-override sheet.
- **Tag editor sheet** (feature-local) — [detail](components/sheets.md#tag-editor-sheet) — manages the user's own tags on a book.
- **Choose-lists sheet** — `core:component/lists` — add/remove one or many books to the user's custom lists.
- **Change edition sheet** (feature-local) — [detail](components/sheets.md#change-edition-sheet) — picks which edition the reader tracks.

## Statistics

- **Animated stat number** — `core:component/statistic` — tweens a numeric stat into place.
- **Editorial figure / chart** (feature-local) — [detail](components/statistics.md#editorial-figure--chart) — five hand-drawn monochrome charts.

## Share

- **Share card** — `core:component/share` — one renderer for every share artefact.

## Rich text

- **Clickable text** — `core:component/richtext` — prose with a tappable substring resolved to a link event.
- **Rich text** — `core:component/richtext` — read-only renderer for formatted, spoiler-maskable prose.
- **Rich-text formatting toolbar** — `core:component/control` — bold/italic/spoiler control row inside `VerdictSheet`.

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
