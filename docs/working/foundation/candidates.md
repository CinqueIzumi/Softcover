# Foundation candidates

The foundation backlog: findings from building Softcover that belong in the nl.rhaydus foundation, not in this
app. The capability surface is indexed in [`CAPABILITIES.md`](../../../foundation/docs/CAPABILITIES.md).
F-numbers are stable and never reused or renumbered; the next free number is F74.

**Process.** Fix it in `foundation/` on a branch, following `.claude/rules/foundation.md`, then move the
pointer. Or record it here until it is done.

Each entry: **type** (bug / enhancement / gate), **home** (target foundation module), **status**, and enough
context for whoever picks it up. Adopted items are pruned from this doc.

---

# Open candidates

Filed but not yet implemented in the foundation.

### F69 — `SnackBarManager` drops a snackbar posted from another's callback

- **Type:** bug. **Home:** `designsystem-core` (`SnackBarManager`). **Status:** open; no screen hits it yet.
- `showSnackBar` runs `onActionClick` / `onDismiss` inside its `try`, before `finally` resets `isShowing`, so
  a snackbar posted from the callback (an "Undone" confirmation) is silently dropped.
- **Build:** reset `isShowing` before invoking the callback; test that a snackbar posted from
  `onActionClick` shows.

### F70 — `ObserveAsEvents` runs its first-composition lambda forever

- **Type:** bug. **Home:** `designsystem-core` (`ObserveAsEvents`). **Status:** open.
- `LaunchedEffect(flow, lifecycle)` collects with the `onEvent` it first saw, so anything the lambda captures
  from composition (a keyed `remember`ed controller, haptics) goes stale when its keys change. Softcover's
  progress-sheet review caught a mark-as-read write that would never have run.
- **Build:** collect through `rememberUpdatedState(onEvent)`; test that a recomposed lambda receives the next
  event.

### F71 — Design-preset exporter as a convention plugin

- **Type:** enhancement. **Home:** `build-logic` (new `rhaydus.design-preset`) and a `rhaydus-kotlin` skill.
  **Status:** open.
- Softcover's `core/designsystem/src/jvmDesignPreset` exporter, the `DesignPresetBundle` task and plugin, and
  the `publish-design-system` skill. Fifteen of its twenty files import nothing from Softcover (SVG
  conversion, CSS / JSON writers, fonts, usage checks, `Main`).
- **Build:** move the generic files; each app supplies only its token readers (`ColorTokens`, `SpacingTokens`,
  `TypeTokens`, `PalettesMarkdown`, `PresetTheme`).

### F72 — A "near the end of a lazy list" effect

- **Type:** enhancement. **Home:** `designsystem-core`. **Status:** open.
- Softcover's `BookCardRailNearEndEffect` (`:core:component/bookcard`) is generic apart from its name and
  stand-in flag: it calls `onNearEnd` once the visible window ends within a threshold of the last item.
- **Build:** a general effect over `LazyListState`, item count, `canLoadMore`, `isAppending` and a threshold;
  Softcover's rail calls it.

### F73 — Re-apply edge-to-edge bar contrast when the resolved theme flips

- **Type:** enhancement. **Home:** `designsystem-core` (Android). **Status:** open.
- `enableEdgeToEdge` resolves the bar-glyph contrast once, at call time, so an in-app dark-mode switch leaves
  the launch-time contrast. Softcover's `ApplySystemBarAppearance` (`:core:designsystem`, androidMain)
  re-applies it in a `DisposableEffect(activity, isDark)`.
- **Build:** ship it with `transparentSystemBarStyle`; Softcover drops its copy.
