# Design System — Patterns

Recurring recipes that compose the primitives above; grep the name, then open its file under `patterns/`.

## Editorial

- **Editorial section** — [detail](patterns/editorial.md#editorial-section) — accent bar → eyebrow → headline → body, the default region opener.
- **Hero stat** — [detail](patterns/editorial.md#hero-stat) — an oversize tweening number for a live-bound count.
- **Editable hero stat** — [detail](patterns/editorial.md#editable-hero-stat) — Hero-stat treatment for a number the reader edits.
- **Editorial quote** — [detail](patterns/editorial.md#editorial-quote) — Glyph-backed pull quote, also the empty-state flourish.
- **Standfirst** — [detail](patterns/editorial.md#standfirst) — Book's one-line editorial lead above its description.
- **Prose toggle** — [detail](patterns/editorial.md#prose-toggle) — Sentence whose word-choice is the control.
- **Rendered markdown document** — [detail](patterns/editorial.md#rendered-markdown-document) — Block renderer for a markdown-shaped document.

## Book detail

- **Book-detail tag block** — [detail](patterns/book-detail.md#book-detail-tag-block) — Read-only, category-grouped tags on The Book lens.
- **Your tags section** — [detail](patterns/book-detail.md#your-tags-section) — Reader's own tags on the Yours lens.
- **Shelve control** — [detail](patterns/book-detail.md#shelve-control) — Three-row Want to read / Reading / Read status control.
- **Lens toggle** — [detail](patterns/book-detail.md#lens-toggle) — Sticky segmented switch between The Book and Yours.
- **Book-detail in-progress stat** — [detail](patterns/book-detail.md#book-detail-in-progress-stat) — Reading-shelf hero stat with pace and deadline.
- **Edition colophon line** — [detail](patterns/book-detail.md#edition-colophon-line) — Single-line publisher/format/ISBN readout.
- **External-links strip** — [detail](patterns/book-detail.md#external-links-strip-labeled-pills) — Labeled pills to Bookshop/Amazon/OpenLibrary.
- **Book-detail lens-section reveal** — [detail](patterns/book-detail.md#book-detail-lens-section-reveal) — animate-in vs. reserve-height for gated sections.
- **Lens-toggle enable fade** — [detail](patterns/book-detail.md#lens-toggle-enable-fade) — Animated dim-to-enabled flip on the Yours segment.
- **Verdict (rating and review)** — [detail](patterns/book-detail.md#verdict-rating-and-review) — Combined rating+review sheet and read-only block.
- **Verdict prompt (open-on-finish)** — [detail](patterns/book-detail.md#verdict-prompt-open-on-finish) — Cross-feature sheet raised on a genuine mark-as-read.
- **Spoiler reveal** — [detail](patterns/book-detail.md#spoiler-reveal) — Whole-review gate and inline-span reveal for spoilers.

## Library

- **Masthead-as-shelf-switcher (Library, mobile)** — [detail](patterns/library.md#masthead-as-shelf-switcher-library-mobile) — Page title as the shelf switcher.
- **Masthead control line** — [detail](patterns/library.md#masthead-control-line) — Sort/filter/select row under the search field.
- **Shelf neighbour rail (Library, mobile)** — [detail](patterns/library.md#shelf-neighbour-rail-library-mobile) — Prev/next shelf row for the swipe gesture.
- **Shelves sheet (Library, mobile)** — [detail](patterns/library.md#shelves-sheet-library-mobile) — Sheet form of the shelf switcher.
- **Arrange sheet** — [detail](patterns/library.md#arrange-sheet) — Sort + layout sheet.
- **Filter sheet** — [detail](patterns/library.md#filter-sheet) — Faceted filter picker, draft/commit.
- **Library layout model** — [detail](patterns/library.md#library-layout-model) — Arrange-sheet chips mapped onto the persisted layout enum.
- **Library cover grid and de-carded list row** — [detail](patterns/library.md#library-cover-grid-and-de-carded-list-row) — Shelf's grid cells and flat list row.

## Library editing modes

- **Bulk-select mode** — [detail](patterns/library-editing.md#bulk-select-mode) — N-target selection with a selection header.
- **Rearrange mode** — [detail](patterns/library-editing.md#rearrange-mode) — Opt-in gate before drag handles appear on a sort.
- **Drag-to-reorder shelf (built-in shelves, MANUAL)** — [detail](patterns/library-editing.md#drag-to-reorder-shelf-built-in-shelves-manual) — Local, prefix-scoped reorder.
- **Drag-to-reorder custom list (ORDER, ranked-gated)** — [detail](patterns/library-editing.md#drag-to-reorder-custom-list-order-ranked-gated) — Server-owned, range-scoped reorder.

## Reading

- **Backdate a logged action** — [detail](patterns/reading.md#backdate-a-logged-action) — Pill-plus-stepper editor for logging an action in the past.
- **Reading-session peek bar** — [detail](patterns/reading.md#reading-session-peek-bar) — Live-timer bar above bottom nav while a session runs.
- **Focus Mode** — [detail](patterns/reading.md#focus-mode) — Distraction-free full-screen surface for the session.
- **Streak strip** — [detail](patterns/reading.md#streak-strip) — 21-day binary activity row.
- **Reading featured-hero card** — [detail](patterns/reading.md#reading-featured-hero-card) — Blurred-cover-backdrop hero for the featured book.
- **Reading secondary row** — [detail](patterns/reading.md#reading-secondary-row) — Flat hairline row for "also reading" books.

## Settings

- **Desktop settings master-detail** — [detail](patterns/settings.md#desktop-settings-master-detail) — sidebar+pane for settings-form tabs on desktop.
- **Theme picker (preview tiles)** — [detail](patterns/settings.md#theme-picker-preview-tiles) — Light/Dark/System picker that leads Appearance.
- **Spine-colour picker** — [detail](patterns/settings.md#spine-colour-picker-the-featured-personalisation) — App's answer to Material You, five palettes.
- **Appearance settings rows** — [detail](patterns/settings.md#appearance-settings-rows-flat-hairline-separated) — Flat toggle/selectable-row anatomy for Appearance.
- **Desktop display scale** — [detail](patterns/settings.md#desktop-display-scale) — Desktop-only UI-scale selector.
- **About screen body** — [detail](patterns/settings.md#about-screen-body) — Credits/Source/Contact layout, the app's one version line.
- **Library tabs reorderable row** — [detail](patterns/settings.md#library-tabs-reorderable-row) — drag-to-reorder + show/hide list under Library tabs.

## Explore & Profile

- **Profile screen** — [detail](patterns/explore-profile.md#profile-screen) — Reading-life section order.
- **Profile share sheet** — [detail](patterns/explore-profile.md#profile-share-sheet) — Reading-life share-card preview, save/share actions.
- **Log-out confirm sheet** — [detail](patterns/explore-profile.md#log-out-confirm-sheet) — Destructive-confirm sheet gating Log out.
- **Title-page sheet (Create list)** — [detail](patterns/explore-profile.md#title-page-sheet-create-list) — Centred single-field naming composition.

## Cross-cutting

- **List–detail two-pane** — [detail](patterns/cross-cutting.md#list-detail-two-pane) — Expanded-window leading-list/trailing-detail pane pair.
- **Reserved-row card** — [detail](patterns/cross-cutting.md#reserved-row-card) — Reserves space for optional rows so cards never reflow.
- **Tonal grouping** — [detail](patterns/cross-cutting.md#tonal-grouping) — Steps container shade instead of a divider.
- **Inline filter chip strip** — [detail](patterns/cross-cutting.md#inline-filter-chip-strip) — Chip row that scopes a grid or list.
- **Active-filter chip row** — [detail](patterns/cross-cutting.md#active-filter-chip-row) — Removable chips for only the currently applied filters.
- **Pace-nudge ribbon** — [detail](patterns/cross-cutting.md#pace-nudge-ribbon) — Dismissable, card-fused single-sentence nudge.
- **Actionable inline banner** — [detail](patterns/cross-cutting.md#actionable-inline-banner) — Nudge's heavier sibling with a primary action.
- **Adaptive empty state** — [detail](patterns/cross-cutting.md#adaptive-empty-state) — Surfaces next-best content, not a static empty panel.
