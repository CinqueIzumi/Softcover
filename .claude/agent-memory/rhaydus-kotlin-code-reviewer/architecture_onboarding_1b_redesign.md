---
name: architecture_onboarding_1b_redesign
description: Onboarding 1b UI-only redesign review findings — scroll regression on pager pages 1-2, ClickableText inline-content doc-sync gap, touch-target gap on new Skip/Back affordance
metadata:
  type: project
---

Reviewed 2026-07-21: `feature/onboarding` mobile pager + jvm desktop layout rebuilt as flat-row/oversized-type
"1b" surface (paste row, tonal pill `BasicTextField` key field, hand-built Canvas wavy-sine connector,
`ClickableText.inlineContent` extension for a glued external-link glyph). No TOAD contract changes
(state/action/ScreenModel untouched) — confirmed pure UI-layer, correctly scoped.

**Real regression found:** the prior mobile pager wrapped *every* page's content in its own
`verticalScroll` Column. The 1b rebuild only kept that scroll on page 3 (`ThirdIntroScreen`); pages 1
and 2 (`FirstIntroScreen`/`SecondIntroScreen`) became fixed `Column(weight(1f), Arrangement.Center)` with
no scroll fallback, carrying oversized display type (76sp/54sp hero + a 340sp decorative quote glyph on
page 1). Short-height devices, landscape, or a bumped system font-scale accessibility setting can overflow
this with no scroll to fall back on — Compose doesn't clip by default, so overflow visually collides with
the fixed footer button. Watch for this pattern (fixed hero + no scroll) recurring in future "oversized
editorial type" redesigns — it's a real accessibility/small-screen risk, not hypothetical.

**Doc-sync nuance:** `ClickableText` (`core/designsystem/.../component/`) gained a new `inlineContent`
param (default `emptyMap()`, backward compatible) so a glyph can be glued into a text run via
`appendInlineContent` — closing a real gap (`SettingsShelf.kt`'s `LibraryTabsGroupSubhead` had already
independently hand-rolled the identical `Text(inlineContent = ...)` pattern on a raw `Text`, not through
`ClickableText`, because the capability didn't exist yet). Notably `ClickableText` was **never documented
in `design-system.md`** even before this change (unlike `InlineErrorState`/`DesktopVerticalScrollbar`,
which have full paragraphs) — so the doc's per-component coverage is already selective, not exhaustive.
Still flagged this as needing a doc mention: the maintenance rule's own trigger list includes "a new
layout pattern other screens should adopt," and this literally is one (per the `LibraryTabsGroupSubhead`
precedent). Weigh the "component was never documented anyway" mitigating fact against the explicit rule
text when deciding blocker vs. important severity here.

**New touch-target gap:** the "Skip"/"Back" trailing-label affordance in the new `PrimaryActionFooterRow`
(`feature/onboarding/.../OnboardingShelf.kt`, brand new in 1b — no prior equivalent existed) is a bare
small uppercase `Text().clickable()` with no `.padding()`/`minimumInteractiveComponentSize()` — likely
under the ~48dp a11y minimum tap target. Also no `role = Role.Button` on the clickable modifier.

**Confirmed non-issues (checked, don't re-litigate):** `RhaydusButton` genuinely has no trailing-icon slot
(only a leading `icon` param) — the Box-overlay non-interactive `Icon` workaround for the forward arrow is
correct, not hacky. `SoftcoverIcon.OpenInNew` (new catalog enum entry) does *not* need a design-system.md
mention — the doc describes the `SoftcoverIcon`/`RhaydusIconResource` mechanism generically and doesn't
enumerate individual icons (unlike `SoftcoverIllustration`, which does name entries). The Canvas
wavy-sine math in `WavySineLine` (mobile-only decorative connector) is correct bezier approximation, no
edge-case div-by-zero risk. `quoteGlyphSway()` non-reuse (static quote glyph uses `clearAndSetSemantics {}`
instead) is correctly justified — that modifier is genuinely scoped to empty-state glyphs elsewhere
(`ReadingShelf`/`LibraryShelf`/`HiddenSuggestionsShelf`/`TagEditorBottomSheet`).

See also [[style_modifier_chain_padding_wrapping]] (found 3 fresh inline 2-arg Modifier-chain calls in
this diff: `OnboardingShelf.kt` `PasteFromClipboardRow`'s `.padding(horizontal=, vertical=)`,
`OnboardingScreenLayout.mobile.kt` `OnboardingTopBar`'s `.size(width=, height=)`,
`OnboardingScreenLayout.jvm.kt`'s desktop panel `.padding(horizontal=, vertical=)`) and
[[project_import_order_debt]] (one fresh out-of-order pair: `widthIn` before `width` in
`OnboardingScreenLayout.mobile.kt`).
