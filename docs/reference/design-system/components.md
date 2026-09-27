# Design System — Components

An index of shared components. Each line links to its family file under `components/` for the full contract — grep the name, then open that one file.

## Navigation & chrome

- **Top app bar** — `core:component/topbar` — page title, optional back/trailing actions, opaque or over-media.
- **Search top app bar** — `core:component/topbar` — mobile search chrome with a scan button.
- **Editorial search field** — [detail](components/navigation.md#editorial-search-field) — persistent inline search pill for editorial headers.
- **Bottom navigation** — [detail](components/navigation.md#bottom-navigation) — compact-width tab chrome, docked or floating.
- **Navigation rail** — [detail](components/navigation.md#navigation-rail) — medium-width tab chrome.
- **Editorial sidebar** — [detail](components/navigation.md#editorial-sidebar) — expanded-width permanent tab chrome.
- **Session peek bar** — [detail](components/navigation.md#session-peek-bar) — live-timer bar for an active session.
- **Settings menu row** — [detail](components/navigation.md#settings-menu-row) — hairline-separated navigational menu row anatomy.
- **About link/username row** — [detail](components/navigation.md#about-linkusername-row) — leaf-action variant of the settings row.

## Buttons & controls

- **Button** — [detail](components/buttons-controls.md#button) — five styles/sizes; filled is the one primary per region.
- **Toggle button / icon toggle button** — [detail](components/buttons-controls.md#toggle-buttons) — active state via shape morph and fill.
- **Split button** — [detail](components/buttons-controls.md#split-button) — primary action with an attached chevron menu of variants.
- **Theme preview tile** — `core:component/control` — miniature-page choice control for the theme picker.
- **Preview tile frame** — `core:component/control` — internal frame/mini-bar primitives the tiles build from.
- **Colour palette preview tile** — `core:component/control` — miniature-page choice control for the spine-colour picker.
- **Bookmark toggle** — [detail](components/buttons-controls.md#bookmark-toggle) — inline add/remove-from-library row control.
- **Swipe row actions** — [detail](components/buttons-controls.md#swipe-row-actions) — swipe-to-mark-as-read / swipe-to-remove.

## Chips

- **Pill chip** — `core:component/chip` — one family; tonal/spoiler/add/outline/remove/format.
- **Expandable flow row** — [detail](components/chips.md#expandable-flow-row) — wrapping chip/tag container that collapses past N lines.

## Covers

- **Cover** — `core:component/cover` — the only way the app draws a book cover; variant-driven, never blank.
- **Coverless title cover** — `core:component/cover` — the monogram/title jacket `Cover` falls back to.
- **Rhaydus shimmer image** — [detail](components/covers.md#rhaydus-shimmer-image) — the loader for every non-cover image.

## States & feedback

- **Inline error state** — [detail](components/states-feedback.md#inline-error-state) — message + retry for a `String?` error slot.
- **Banner** — `core:component/callout` — full-width slide-in chrome reporting a screen-level condition.
- **Empty state** — `core:component/state` — centred headline-and-body placeholder for missing content.
- **Pull-to-refresh indicator** — [detail](components/states-feedback.md#pull-to-refresh-indicator) — the one exception to the wavy-progress rule.
- **Pull-to-refresh eyebrow** — [detail](components/states-feedback.md#pull-to-refresh-eyebrow) — contextual eyebrow copy synced to pull progress.
- **Deadline badge / cover overlay / summary line** — `core:component/badge` — status badge, cover overlay, and date+pace line.
- **Unreleased badge** — [detail](components/states-feedback.md#unreleased-badge) — the `Badge`/`Release` mark for a book not yet out.
- **Mark-as-read celebration** — `core:component/celebration` — particle-burst hero moment for a mark-as-read commit.
- **Search results pagination** (feature-owned) — [detail](components/states-feedback.md#search-results-pagination) — append-on-scroll paging, no total count.
- **Connectivity chrome** — [detail](components/states-feedback.md#connectivity-chrome) — `Banner` + `EmptyState` + `rememberIsOnline()`, offline surfaces.
- **Update highlight card** — [detail](components/states-feedback.md#update-highlight-card) — Settings app-update `primaryContainer` callout.

## Sheets

- **Loading sheet** — `core:component/sheet` — full-screen blocking wavy-progress surface for an unavoidable wait.
- **Date picker dialog** — `core:component/dialog` — single-day calendar dialog; owns the UTC-millis conversion.
- **Update progress sheet** — `core:component/progress` — the pages/percentage/time reading-progress editor sheet.
- **Because-you-read genre picker** (feature-owned) — [detail](components/sheets.md#because-you-read-genre-picker) — genre-override sheet, Shelves-sheet anatomy.
- **Tag editor sheet** (feature-local) — [detail](components/sheets.md#tag-editor-sheet) — manages the user's own tags on a book.
- **Choose-lists sheet** — `core:component/lists` — add/remove one or many books to the user's custom lists.
- **Change edition sheet** (feature-local) — [detail](components/sheets.md#change-edition-sheet) — picks which edition of a book the reader tracks.

## Statistics

- **Animated stat number** — `core:component/statistic` — renders a numeric stat that tweens in place.
- **Editorial figure / chart** (feature-local) — [detail](components/statistics.md#editorial-figure--chart) — five hand-drawn monochrome chart shapes.

## Share

- **Share card** — `core:component/share` — renders any share artefact (book, stat, quote, recap) into one composition.

## Rich text

- **Clickable text** — `core:component/richtext` — prose with a tappable substring resolved to a link event.
- **Rich text** — `core:component/richtext` — the read-only renderer for formatted, spoiler-maskable prose.
- **Rich-text formatting toolbar** — `core:component/control` — bold/italic/spoiler control row inside `VerdictSheet`.

## Editorial

- **Drop-cap text** — [detail](components/editorial.md#drop-cap-text) — editorial body prose with a 3-line drop cap.
- **Editable hero-stat field** — [detail](components/editorial.md#editable-hero-stat-field) — borderless hero-numeral input for progress sheets.
- **Star rating input** — [detail](components/editorial.md#star-rating-input) — the half-star interactive/read-only rating control.
- **Verdict sheet** — `core:component/verdict` — the combined rating-and-review surface.
- **Verdict block** — `core:component/verdict` — the book-page rating+review card that opens the verdict sheet.
- **Personal review section** — [detail](components/editorial.md#personal-review-section) — book-detail entry point into the review editor.
- **Browse-by-mood grid** (feature-owned) — [detail](components/editorial.md#browse-by-mood-grid) — the 2-column mood-tile grid.

## Platform helpers

- **Desktop tooltip** — [detail](components/platform-helpers.md#desktop-tooltip) — hover tooltip on desktop, no-op on touch.
- **Desktop context menu** — [detail](components/platform-helpers.md#desktop-context-menu) — right-click menu on desktop, no-op on touch.
- **Haptics helper** — [detail](components/platform-helpers.md#haptics-helper) — the eight-case single entry point for haptic feedback.
- **Lazy-item mutation animator** — [detail](components/platform-helpers.md#lazy-item-mutation-animator) — animates user-triggered list add/move/remove.
- **Staggered entry coordinator** — [detail](components/platform-helpers.md#staggered-entry-coordinator) — welcome-moment stagger for carousels/lists.
- **Barcode scanner** (feature-owned, not a DS component) — [detail](components/platform-helpers.md#barcode-scanner) — CameraX/ML Kit ISBN-scan surface.
- **Active reading session** — [detail](components/platform-helpers.md#active-reading-session) — controller behind the peek bar and Focus Mode.
