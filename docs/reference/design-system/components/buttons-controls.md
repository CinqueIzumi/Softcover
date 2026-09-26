# Components — Buttons & controls

### Button

Five styles (filled, tonal, elevated, outlined, text) across five sizes (XS → XL). Filled is the page's primary action; tonal is a secondary action that still wants weight; outlined and text are tertiary. One filled action per region — never two competing primaries.

### Toggle buttons

Same size scale as Button; the active state expresses itself via shape morph and fill, not via a separate badge.

### Split button

A single primary action paired with an attached chevron that opens a menu of related variants (e.g. "Mark as reading" + alternative shelves). Reach for it when an action has one obvious default and a small set of equivalents.

### Bookmark toggle

A 44×44dp, 12dp-radius add/remove-from-library control: unset is `surfaceContainerHigh` fill with a `primary` glyph (`SoftcoverIcon.BookmarkAdd`), set is `primary` fill with an `onPrimary` glyph (`SoftcoverIcon.BookmarkAdded`). Used on Explore's search-result rows; reach for it on any other row-style book listing that offers an inline add/remove-from-library action rather than hand-rolling an `IconToggleButton`.

### Swipe row actions

`SwipeRowActions` wraps a list-layout row in a Material 3 `SwipeToDismissBox` and reveals a coloured background as the user drags. Swipe-right (start → end) is the *mark-as-read* action — tertiary container with `ic_bookmark_check` and "Mark as read" copy; suppressed via `allowMarkAsRead = false` when the book is already in Read. Swipe-left (end → start) is the *remove* action — error container with `ic_delete` and "Remove" copy. Background opacity tracks the swipe progress (`progress * 1.2f`, capped at 1) so the affordance reveals as the user pulls. The dismiss state is reset immediately on commit so the row snaps back; the calling surface is responsible for the data write and for shaking the row on failure (pair with `Modifier.shakeOnError`). Reach for it on the `LIST_COMPACT` and `LIST_LARGE` library layouts; never apply it to grid cards.
