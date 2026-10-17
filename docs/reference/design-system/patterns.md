# Design System — Patterns

Screen behaviour and recipes that compose the components in `components.md`; grep the name, then open its
file under `patterns/`.

## Editorial

- [Editorial section](patterns/editorial.md#editorial-section) — the default region opener.
- [Hero stat](patterns/editorial.md#hero-stat) — one oversize live-bound number with an editorial suffix.
- [Editable hero stat](patterns/editorial.md#editable-hero-stat) — the hero stat for a number the reader edits.
- [Editorial quote](patterns/editorial.md#editorial-quote) — glyph-backed pull quote, also the empty-state flourish.
- [Standfirst](patterns/editorial.md#standfirst) — a book's one-line editorial lead above its description.
- [Prose toggle](patterns/editorial.md#prose-toggle) — a sentence whose words are the control.
- [Rendered markdown document](patterns/editorial.md#rendered-markdown-document) — markdown block renderer.

## Book detail

- [Book-detail tag block](patterns/book-detail.md#book-detail-tag-block) — read-only, category-grouped tags on The Book lens.
- [Your tags section](patterns/book-detail.md#your-tags-section) — the reader's own tags on the Yours lens.
- [Tag editor sheet](patterns/book-detail.md#tag-editor-sheet) — add, suggest and remove the reader's tags.
- [Change edition sheet](patterns/book-detail.md#change-edition-sheet) — searchable single-choice edition picker.
- [Shelve control](patterns/book-detail.md#shelve-control) — three-row Want to read / Reading / Read control.
- [Lens toggle](patterns/book-detail.md#lens-toggle) — sticky switch between The Book and Yours.
- [Book-detail in-progress stat](patterns/book-detail.md#book-detail-in-progress-stat) — Reading-shelf hero stat with pace and deadline.
- [Edition colophon line](patterns/book-detail.md#edition-colophon-line) — one-line publisher, format and ISBN readout.
- [External-links strip](patterns/book-detail.md#external-links-strip-labeled-pills) — labelled pills to off-app lookups.
- [Verdict](patterns/book-detail.md#verdict-rating-and-review) — rating and review as one sheet and one block.
- [Verdict prompt](patterns/book-detail.md#verdict-prompt-open-on-finish) — verdict sheet raised on a genuine finish.
- [Spoiler reveal](patterns/book-detail.md#spoiler-reveal) — whole-review gate and inline-span reveal.

## Library

- [Masthead-as-shelf-switcher](patterns/library.md#masthead-as-shelf-switcher-library-mobile) — the page title switches shelves.
- [Masthead control line](patterns/library.md#masthead-control-line) — sort, filter and select row under the search field.
- [Shelf neighbour rail](patterns/library.md#shelf-neighbour-rail-library-mobile) — previous and next shelf row for the swipe.
- [Shelves sheet](patterns/library.md#shelves-sheet-library-mobile) — sheet form of the shelf switcher.
- [Arrange sheet](patterns/library.md#arrange-sheet) — sort and layout, draft then commit.
- [Filter sheet](patterns/library.md#filter-sheet) — faceted filter picker, draft then commit.
- [Library layout model](patterns/library.md#library-layout-model) — layout chips mapped onto the persisted layout enum.
- [Library cover grid and list row](patterns/library.md#library-cover-grid-and-list-row) — the shelf's `BookCard` cells.

## Library editing modes

- [Bulk-select mode](patterns/library-editing.md#bulk-select-mode) — N-target selection under a selection header.
- [Undoable removal](patterns/library-editing.md#undoable-removal) — remove now, delete after the Undo window.
- [Rearrange mode and drag-to-reorder](patterns/library-editing.md#rearrange-mode-and-drag-to-reorder) — opt-in drag on a positional sort.

## Reading

- [Backdate a logged action](patterns/reading.md#backdate-a-logged-action) — pill-plus-stepper editor for logging the past.
- [Reading-session peek bar](patterns/reading.md#reading-session-peek-bar) — live-timer bar above the nav during a session.
- [Focus Mode](patterns/reading.md#focus-mode) — full-screen surface for the running session.
- [Streak strip](patterns/reading.md#streak-strip) — 21-day binary activity row.
- [Reading featured-hero card](patterns/reading.md#reading-featured-hero-card) — blurred-cover hero for the featured book.
- [Reading secondary row](patterns/reading.md#reading-secondary-row) — flat rows for the other books in progress.
- [Mark-as-read celebration](patterns/reading.md#mark-as-read-celebration) — one burst and haptic per finish.

## Settings

- [Settings rows](patterns/settings.md#settings-rows) — the feature's menu and selectable row anatomy.
- [Desktop settings master-detail](patterns/settings.md#desktop-settings-master-detail) — sidebar and inline pane on desktop.
- [Theme picker](patterns/settings.md#theme-picker-preview-tiles) — Light / Dark / System tiles leading Appearance.
- [Spine-colour picker](patterns/settings.md#spine-colour-picker-the-featured-personalisation) — five palette tiles.
- [Appearance settings rows](patterns/settings.md#appearance-settings-rows-flat-hairline-separated) — flat sections of Appearance.
- [Desktop display scale](patterns/settings.md#desktop-display-scale) — desktop-only UI-scale selector.
- [About screen body](patterns/settings.md#about-screen-body) — Credits, Source, Contact and the one version line.
- [Library tabs reorderable row](patterns/settings.md#library-tabs-reorderable-row) — reorder and show/hide Library tabs.

## Widgets

- [Home-screen widgets](patterns/widgets.md) — Android Glance widgets and their settings.

## Explore & Profile

- [Profile screen](patterns/explore-profile.md#profile-screen) — reading-life section order.
- [Statistics charts](patterns/explore-profile.md#statistics-charts) — which chart each Profile statistic uses.
- [Profile share sheet](patterns/explore-profile.md#profile-share-sheet) — share-card preview with save and share.
- [Log-out confirm sheet](patterns/explore-profile.md#log-out-confirm-sheet) — destructive confirm gating Log out.
- [Title-page sheet](patterns/explore-profile.md#title-page-sheet-create-list) — centred single-field naming sheet.
- [Browse-by-mood grid](patterns/explore-profile.md#browse-by-mood-grid) — two-column mood tiles.
- [Because-you-read genre picker](patterns/explore-profile.md#because-you-read-genre-picker) — genre anchor and sheet.
- [Search results pagination](patterns/explore-profile.md#search-results-pagination) — append on scroll, no total.
- [Explore search back ladder](patterns/explore-profile.md#explore-search-back-ladder-mobile) — back leaves search first.
- [Explore search Esc](patterns/explore-profile.md#explore-search-esc-desktop) — Esc clears an active search.

## Cross-cutting

- [List-detail two-pane](patterns/cross-cutting.md#list-detail-two-pane) — expanded-window list and detail panes.
- [Section footprint reserved while loading](patterns/cross-cutting.md#section-footprint-reserved-while-loading) — reserve or reveal.
- [Sheet host](patterns/cross-cutting.md#sheet-host) — one collector, one `SheetSession` writer.
- [Blocking loading sheet](patterns/cross-cutting.md#blocking-loading-sheet) — a long wait as a non-dismissible sheet.
- [App-shell navigation chrome](patterns/cross-cutting.md#app-shell-navigation-chrome) — bar, rail or sidebar by width.
- [Barcode scanner](patterns/cross-cutting.md#barcode-scanner) — camera ISBN scan with manual fallback.
- [Inline filter chip strip](patterns/cross-cutting.md#inline-filter-chip-strip) — chip row that scopes a grid or list.
- [Active-filter chip row](patterns/cross-cutting.md#active-filter-chip-row) — removable chips for the applied filters.
- [Pace-nudge ribbon](patterns/cross-cutting.md#pace-nudge-ribbon) — dismissable nudge fused to its card.
- [Actionable inline banner](patterns/cross-cutting.md#actionable-inline-banner) — a nudge with one action.
- [Adaptive empty state](patterns/cross-cutting.md#adaptive-empty-state) — next-best content instead of an empty panel.
