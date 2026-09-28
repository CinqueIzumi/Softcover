# S5 — Dividers & rules

**Stage:** S5 — Primitives. **Delegation:** user (Phase 1), `softcover-implementer` (Phase 2).

5 declarations collapse onto `Divider` + `DividerUiModel`; every raw Material divider follows.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S5-9 | All five divider declarations | [x] |
| S5-C-dividers | Convergence: model split, one scaffold + style table, the five existing callers | [x] |
| S5-10 | Absorb the 34 raw `HorizontalDivider(` / `VerticalDivider(` call sites | [ ] |

## Approved shape

- `DividerUiModel` is sealed: `Horizontal(surface: HorizontalDividerSurface, center: DividerCenter? = null)`
  and `Vertical(surface: VerticalDividerSurface)`. A vertical rule cannot carry a centre.
- `DividerCenter` is sealed: `QuoteMark` (decorative, cleared from semantics) and `Label(text)`.
- Surfaces are surface-named data objects, one entry per surface (R2); a surface decides style only, so any
  horizontal surface renders any centre. Existing: `DebugRoutesRow`, `ReadingLifeShareCard`,
  `SettingsRoadmap`, `CreateListQuote`, `OnboardingOrTypeIt`.
- One private scaffold (rule, or rule · centre · rule) replaces the five bodies. A per-surface style table
  gives ink, inset and centre gap; ink is outline or content tint at an alpha, resolved from a `ColorScheme` +
  content colour so it is unit-testable. Thickness 1dp everywhere.
- Accepted shifts: centre gap 12dp for both centres (glyph was 14dp); the label uses plain `eyebrowSmall`
  (drops the 10sp / 2sp-tracking overrides).
- S5-10: each raw site gets a surface entry (a surface may host several sites); models stored on the UiState
  (R10). `VersionFooter` becomes `Horizontal` + `Label`. `TagEditorCollection:85` is header anatomy and goes to
  the headers family, not `Divider`.
