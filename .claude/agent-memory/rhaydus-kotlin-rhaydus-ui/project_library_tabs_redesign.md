---
name: project_library_tabs_redesign
description: Library Tabs 1a rebuild — retired SettingsGroup/SettingsRowDivider, new eye-toggle/grip row, inline-dot subhead technique
metadata:
  type: project
---

Rebuilt `LibraryVisibilityContent` (Settings → Library tabs) in `feature/settings/.../screen/SettingsShelf.kt` per the "Library Tabs 1a" spec: editorial header (`EditorialSectionHeader`, now with a `description`) → bar-less group eyebrow/subhead → one flat borderless reorderable row list (radius 12dp, no card, no dividers) → "New list" foot action tile → existing save bar unchanged.

**SettingsGroup / SettingsRowDivider are retired.** They had exactly one consumer (this screen's old boxed/switch row model) and are now deleted from `SettingsShelf.kt`. Do not resurrect them for a new settings list — reach for the flat "Library tabs reorderable row" anatomy documented in `docs/reference/design-system.md` §5 instead, or the "Settings menu row" anatomy for pure navigation.

**Row model:** `LibraryTabEntry` already carries independent `isReorderable`/`canHide`/`isList` flags (not a single "is All" special case) — read those, don't re-derive from `is Status && isAlwaysOn`. A row is "hidden" = `canHide && !isEnabled(state)`. The fixed front row ("All") gets no `draggable` modifier at all (rather than a modifier that's a no-op) and `targetIndexFor` takes a `minIndex` param so nothing can be dragged above it.

**Inline dot in body text:** to embed a small colored dot mid-sentence (not as a leading icon), use `buildAnnotatedString { append(...); appendInlineContent(id, alt); append(...) }` + `Text(inlineContent = mapOf(id to InlineTextContent(Placeholder(widthSp, heightSp, PlaceholderVerticalAlign.TextCenter)) { Box(...) }))`. `appendInlineContent` needs `import androidx.compose.foundation.text.appendInlineContent` (easy to miss — it's not a member of `AnnotatedString.Builder`, and `InlineTextContent`/`Placeholder`/`PlaceholderVerticalAlign` come from three different packages: `androidx.compose.foundation.text` / `androidx.compose.ui.text` / `androidx.compose.ui.text`). Convert a `Dp` legend size to the required `TextUnit` via `with(LocalDensity.current) { 6.dp.toSp() }`.

**Row-level "press wash" without a whole-row click:** when a row has no single onClick (only sub-controls like a grip and an eye toggle are interactive), don't wire a fake `MutableInteractionSource`/`clickable` just to get a press wash — key the `animateColorAsState` background off the semantically-real state instead (here: `isDragging`), gated by `playDecorativeMotion()`, matching the same convention `SettingsMenuRow`/`AppUpdateSection` use for an actual tap-driven wash.

**Added two new `SoftcoverIcon` catalog entries** (`Add`, `Pin`) — `ic_add.xml`/`ic_pin.xml` in `core/designsystem/src/commonMain/composeResources/drawable/`, 960-viewport single-path convention matching `ic_check.xml`/`ic_close.xml` (not the older 24-viewport `ic_drag_handle.xml` tint-attr style). `ic_pin.xml`'s path is a best-effort reconstruction of Material's `push_pin` glyph from memory — compiles and renders a recognizable pin silhouette, but wasn't visually verified pixel-for-pixel; flag for a visual QA pass if the client ever cares about exact fidelity.

See [[project_inline_error_state]] for the other standing shared-component memory in this project.
