# Components — Buttons & controls

### Button

Five styles (filled, tonal, elevated, outlined, text) across five sizes (XS → XL). Filled is the page's primary action; tonal is a secondary action that still wants weight; outlined and text are tertiary. One filled action per region — never two competing primaries.

### Tactile interaction modifier

`:core:component` `modifier/Tactile.kt` is the app's one interaction modifier for ripple-less surfaces. It bundles, on one shared `InteractionSource`: a press scale to 0.97 on the motion scheme's fast spatial spec (no `playDecorativeMotion()` gate, the reduced-motion scheme already snaps), the desktop hover wash clipped to `hoverShape`, the hand cursor, and the matching click semantics with `indication = null`. While `enabled` is false the feedback is off and the node is announced as disabled. Three forms:

- `tactileClickable(onClick, role: Role? = Role.Button, enabled, onLongClick, hoverShape, interactionSource)`. A non-null `onLongClick` switches to `combinedClickable` and must fire its own haptic. `role` is nullable. A null `hoverShape` paints no wash on any of the three forms, for a caller that paints it on a visible inner shape through the shared `interactionSource`.
- `tactileSelectable(selected, onClick, role, enabled, hoverShape, interactionSource)`. One choice in a `selectableGroup()` parent (pass `Role.RadioButton`). Fires `Haptics.select()` only when the tap changes the selection.
- `tactileToggleable(value, onValueChange, role, enabled, hoverShape, interactionSource)`. A two-state control. Fires `Haptics.select()` on every flip.

Pass `interactionSource` only when the caller reads the same source, for example to tint a row on press. Plain `tactileClickable` fires no haptic, so a caller that wants a tap haptic fires it in `onClick`. Never import the foundation `pressScale`, `pressScaleClickable` or `pressScaleCombinedClickable` (detekt `ForbiddenImport`). Tonal, outlined, text and elevated buttons keep the Material ripple and do not use it.

### Split button

A single primary action paired with an attached chevron that opens a menu of related variants (e.g. "Mark as reading" + alternative shelves). Reach for it when an action has one obvious default and a small set of equivalents.

### Swipe row actions

`SwipeRowActions` wraps a list-layout row in a Material 3 `SwipeToDismissBox` and reveals a coloured background as the user drags. Swipe-right (start → end) is the *mark-as-read* action — tertiary container with `ic_bookmark_check` and "Mark as read" copy; suppressed via `allowMarkAsRead = false` when the book is already in Read. Swipe-left (end → start) is the *remove* action — error container with `ic_delete` and "Remove" copy. Background opacity tracks the swipe progress (`progress * 1.2f`, capped at 1) so the affordance reveals as the user pulls. The dismiss state is reset immediately on commit so the row snaps back; the calling surface is responsible for the data write and for shaking the row on failure (pair with `Modifier.shakeOnError` from `:core:component`'s `modifier/`). Reach for it on the `LIST_COMPACT` and `LIST_LARGE` library layouts; never apply it to grid cards.
