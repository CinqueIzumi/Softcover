# Components — Platform helpers

### Desktop tooltip

`DesktopTooltip(text) { control }` wraps an icon-only control in a hover tooltip on desktop and is a no-op pass-through on touch (§2.5 desktop tooltip). Reach for it on every icon-only desktop control; controls with a visible text label do not get one.

### Desktop context menu

`DesktopContextMenu(items) { surface }` wraps a surface in a right-click menu on desktop (`ContextMenuArea`) and is a no-op pass-through on touch (and when `items` is empty), §2.5 desktop selection. `DesktopContextMenuItem(label, onClick)` is one entry. Reach for it for the desktop secondary-click affordance on an item that touch reaches via long-press.

### Haptics helper

The single entry point for haptic feedback. Exposes eight semantic cases. No ad-hoc haptics — never call `performHapticFeedback` from a call site; always go through the helper. The "no mixed icon families" rule (§2.6) has a sibling here.

- **commit** — a user-triggered action succeeded (mark-as-read, save, confirm). Celebratory in texture; reserved for commit-class actions, never navigation taps.
- **reject** — an optimistic mutation rolled back, or an action refused. Pair with the shake-on-error visual (§2.5).
- **select** — a soft tick on neutral selection: shelf chip toggle to a non-read state, segmented switch in a sheet, tab change in a carousel filter. Distinct from *commit* (which celebrates) and *reject* (which rolls back).
- **threshold** — a single firm tap when the user crosses a meaningful boundary: pull-to-refresh trigger point, drag-to-reorder pickup, long-press peek activation. No celebratory texture.
- **tickle** — a per-integer tick during a slider or picker drag. Each integer crossing on the rating control or progress page-number input fires one tick.
- **lift** — drag-to-reorder pickup. Says the item has left the page; pair with the visual lift.
- **drop** — drag-to-reorder settle. Distinguishable from *lift* so the item's return to the page is felt.
- **milestone** — a two-pulse haptic above *commit* for natural progress events: completing a year-end reading goal, hitting a 30-day streak, finishing the last book in a series. Reserved for moments that are bigger than a save.

### Lazy-item mutation animator

The canonical wiring for animating add / move / remove on a lazy list when the change is user-triggered. `rememberLazyItemMutationAnimator(keys)` snapshots the initial set of keys on first non-empty composition; the `Modifier.mutationAnimated(scope, animator, itemKey)` extension (overloaded for `LazyItemScope` / `LazyGridItemScope`) returns a `Modifier` (carrying `Modifier.animateItem()` plus a `drawWithContent` accent-bar pulse) that the caller applies to the outermost composable of each lazy item. Apply directly — never via an intermediate `Box`, since an extra layout node interferes with lazy-item measurement. Items added or removed after the snapshot fade and reflow; new items also get a brief 20×1 dp accent-bar pulse (§2.3 inline-bar hairline) at their top edge. Suppressed when system animations are disabled. Use it for shelves and chip rows where the user's own action is the "what changed" signal; do not apply it to carousels backed purely by server data (trending, continue-series).

### Staggered entry coordinator

The canonical welcome-moment animator for carousels and lazy lists. `rememberStaggeredEntryCoordinator()` captures the screen-entry timestamp; `Modifier.staggeredEntry(coordinator, index)` plays a ~240ms upward translate (~8dp) + fade in, delayed by ~60ms per index, but only for items composed within the coordinator's window (default 350ms). Items composed later render statically — never apply this expecting items to animate on scroll. Pair with the mutation animator on the same item modifier when both apply (`mutationModifier.staggeredEntry(...)`). Suppressed when system animations are disabled.

### Barcode scanner

`BarcodeScanner` (feature-owned, not a design-system component) is the full-bleed camera surface for reading a book's barcode. It lives in **`:feature:scan`** (`presentation/component/`), **not** the design system: camera + ML Kit hardware integration is a feature concern, so the CameraX/ML Kit pipeline and its `androidx.camera.*` / `mlkit-barcode-scanning` dependencies belong to that feature, never to the UI-primitives module. It is documented here only because its chrome reuses two design-system rules: the editorial scrim over the live feed (a `SCANNING` eyebrow + `headlineMedium` instruction in white — **the one place light-on-media copy is allowed**, since the surface is a camera feed, not a theme surface) and a centred rounded viewfinder reticle. Behaviour: it hosts a CameraX `PreviewView` (back camera) feeding the **bundled** ML Kit model (no Google Play Services), constrained to `FORMAT_EAN_13` / `FORMAT_EAN_8` — the formats printed on physical books — and emits the **raw value of the first barcode it reads exactly once** via `onIsbnDetected(raw)`, then stops analysing. It is deliberately **stateless about resolution** — turning the raw string into a book, and deciding what an unknown book means, belongs to the call site (the scan ScreenModel), never to this leaf. Camera permission is gated separately via `rememberCameraPermissionRequester` (also in `:feature:scan`); when permission is denied or the device has no camera, the calling screen falls back to manual ISBN entry rather than rendering this component. For any in-app barcode capture, reuse this feature surface; do not hand-roll a second CameraX/ML Kit pipeline, and do not move it back into the design system.

### Active reading session

`ActiveSessionController` is the **app-scoped single source of truth** for the running reading session (`core/presentation/session`), exposed as a `StateFlow` and consumed by the peek bar, Focus Mode, the reading tab, and the lock-screen foreground service. It is a shared `core` contract (the interface lives in `core/presentation/session`; its impl is in `:orchestration`) — not a feature internal — because ≥2 surfaces drive it. It launches the platform foreground service through the `ReadingSessionLauncher` **contract** (impl in `feature/session`), so core never depends on the service. `SessionPeekBar` and `FocusModeScreen` are the feature-owned surfaces that render its state.
