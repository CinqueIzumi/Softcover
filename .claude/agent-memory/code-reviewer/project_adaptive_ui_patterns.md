---
name: project_adaptive_ui_patterns
description: Window-size adaptive UI (nav chrome + list–detail two-pane) and the Voyager screen-key gotcha
metadata:
  type: project
---

Softcover has a window-size adaptive layer (DS §2.7): `rememberWindowSizeClass()` / `widthClass`
drives the nav chrome (bottom bar → rail → sidebar) and the expanded-width list–detail two-pane
(`TwoPaneScaffold` + `BookDetailPaneHost`). List screens route book taps through
`LocalBookDetailPresenter`, so the shell decides push (compact/medium) vs. fill-the-pane (expanded).

**Two-pane Voyager screen-key gotcha (high-value review heuristic):**
A Voyager `Screen` shown in a per-selection nested `Navigator` (e.g. `BookDetailPaneHost`, wrapped in
`key(destination.id)`) MUST give the screen an id-scoped key — `override val key = "book-detail-$id"`.
Voyager's `ScreenModelStore` is keyed by the screen key, so a class-constant default key makes every
instance share one `ScreenModel`; the symptom is "every pane selection shows the first book opened."
Recreating the navigator with `key(id)` alone does NOT fix it.

**Full-screen overlays from a paned detail:** a detail rendered in the pane must push full-screen
surfaces (cover viewer, create-list) onto the ROOT navigator, not its own nested one — wired via
`LocalBookDetailOverlayNavigator` (falls back to the local navigator on the pushed path).
