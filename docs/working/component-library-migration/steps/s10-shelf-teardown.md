# S10 — Shelf teardown

**Stage:** S10 — the six `*Shelf.kt` files shrink to layout + composition. **Delegation:**
`softcover-implementer`, one brief per file.

Token-hygiene Step 08 (08a–08m) already split `BookDetailShelf.kt`, `ProfileShelf.kt`,
`LibraryShelf.kt`, `ExploreShelf.kt`, `ReadingShelf.kt` and `SettingsShelf.kt` into one section
composable per file under `…presentation.screen.section`, with the entry file renamed away from
`*Shelf.kt` (it now holds only the composition and the state wiring). The files below are what
`git ls-files '*Shelf.kt'` finds today (2026-09-28); this step is about whether each still carries
weight the S5–S9 families haven't already pulled out, not about repeating the Step 08 split.

## Sub-steps

| Sub | File | Lines | Status |
|---|---|---|---|
| S10-1 | `feature/book_detail/.../presentation/screen/FullScreenCoverShelf.kt` | 121 | [ ] |
| S10-2 | `feature/explore/.../presentation/screen/HiddenSuggestionsShelf.kt` | 529 | [ ] |
| S10-3 | `feature/onboarding/.../presentation/screen/OnboardingShelf.kt` | 526 | [ ] |
| S10-4 | `feature/scan/.../presentation/screen/BarcodeScannerShelf.kt` | 121 | [ ] |
| S10-5 | `feature/session/.../presentation/screen/FocusModeShelf.kt` | 309 | [ ] |
| S10-C | Convergence pass over the family (family-procedure.md § Phase 3) | — | [ ] |

## Per sub-step

- Re-measure the line count before starting (`wc -l`) — S5–S9 sub-steps that touch the same file may
  have already shrunk it.
- Confirm every remaining declaration in the file is either composition/state wiring (`## Out of
  scope` in `README.md`) or a component the relevant S5–S9 family step has not reached yet; if the
  latter, that symbol belongs to that family step instead, not to this one.
- `HiddenSuggestionsShelf.kt` and `OnboardingShelf.kt` still hold several "current" family members
  each (see `steps/s5-chips.md`, `steps/s6-list-rows.md`, `steps/s7-bookcard.md`,
  `steps/s8-controls.md`, `steps/s9-statistics.md`) — do not move those symbols here; this step only
  starts once its family step has already extracted them.
