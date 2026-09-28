# S5 — Dividers & rules

**Stage:** S5 — Primitives. **Delegation:** user (Phase 1), `softcover-implementer` (Phase 2).

5 declarations collapse onto `Divider` + `DividerUiModel`.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S5-9 | All five divider declarations | [ ] |
| S5-C-dividers | Convergence pass over the family (family-procedure.md § Phase 3) | [ ] |

## Approved shape

- `DividerUiModel(variant: DividerVariant)`; `DividerVariant` is sealed and surface-named: `DebugRoutesRow`, `ReadingLifeShareCard`, `SettingsRoadmap`, `CreateListQuote`, `OnboardingOrTypeIt(label: String)` — variant-specific data rides on the variant (R2).
- `Divider(model, modifier)` resolves ink, thickness and inset per variant from a table; the centred quote glyph and label layouts are private to the component.
- All five declarations fold in, `ReadingLifeDivider` included (`ReadingLifeShareCard` → content colour at 25% alpha).
- Package `core/component/divider/`. No mapper — models are stored on the `UiState` (R10). `:feature:lists` gains its `:core:component` dependency.

## S5-9 — inventory

| Symbol(s) | Current path:line | Note |
|---|---|---|
| `DebugRowDivider` | `app/src/debug/.../DebugRoutesSection.kt:106` | |
| `ReadingLifeDivider` | `core/component/share/ReadingLifeShareCardBody.kt:454` | private to the share-card body |
| `HorizontalBreak` | `feature/settings/presentation/screen/RoadmapContent.kt:388` | |
| `QuoteRule` | `feature/lists/presentation/screen/CreateListSheetContent.kt:176` | |
| `OrTypeItDivider` | `feature/onboarding/presentation/screen/OnboardingShelf.kt:239` | |

**Phase 1 questions:** `ReadingLifeDivider` is private inside a share-card body — decide whether it
routes through the new `Divider` component or stays a private drawing detail of that card (R11 allows
a component's own internals to draw directly).
