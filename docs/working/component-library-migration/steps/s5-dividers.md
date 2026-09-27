# S5 — Dividers & rules

**Stage:** S5 — Primitives. **Delegation:** user (Phase 1), `softcover-implementer` (Phase 2).

5 declarations collapse onto `Divider` + `DividerUiModel`.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S5-9 | All five divider declarations | [ ] |
| S5-C-dividers | Convergence pass over the family (family-procedure.md § Phase 3) | [ ] |

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
