---
name: architecture_foundation_press_modifiers
description: nl.rhaydus:designsystem-core press/click modifier family (pressScale, pressScaleClickable, noRippleClickable) and when hand-rolling one is correct, not a reuse-first violation
metadata:
  type: project
---

The foundation's `nl.rhaydus.designsystem.modifier` package (designsystem-core, source at
`nl.rhaydus.designsystem.modifier.ModifierExtensions.kt`) ships:

- `Modifier.pressScale(interactionSource)` — scale-only visual feedback (0.97f while pressed,
  gated by `playDecorativeMotion()`); does not attach a click listener.
- `Modifier.pressScaleClickable(onClick)` — bundles `pressScale` + `clickable(indication = null)`
  behind a single **internally-created** `MutableInteractionSource` that the caller cannot access.
- `Modifier.noRippleClickable(onClick)` — plain ripple-less clickable, no scale feedback.
- `Modifier.pointerHandCursor()` — desktop-only hover cursor, no-op on touch.
- `Modifier.hoverHighlight(interactionSource, shape, color)` — desktop hover wash, sibling of `pressScale`.

**When hand-rolling `pressScale(interactionSource).clickable(interactionSource=, indication=null,
onClick=)` instead of calling `pressScaleClickable(onClick)` is correct, not a reuse-first
violation**: `pressScaleClickable` hides its `InteractionSource`, so it cannot be reused whenever
the caller also needs that same source for another derived animation (e.g. an `animateColorAsState`
press-wash on the row background, as in Settings 1a's `SettingsMenuRow`,
`feature/settings/.../SettingsScreenLayout.mobile.kt`). In that case the manual
`pressScale(interactionSource) + clickable(interactionSource=...)` pairing is the only way to share
one `InteractionSource` across two effects, and exactly matches `pressScaleClickable`'s own internal
implementation — so don't flag it as hand-rolling something the foundation already ships.
