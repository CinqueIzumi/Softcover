# S5-T — Prune change-detector tests

**Stage:** between S5 and S6. **Delegation:** `softcover-test-writer`.

Every remaining step reshapes models, and each one then breaks tests that only restate constants. Pruning
them before S6 stops that churn. The rule is in `.claude/rules/tests.md` (decided 2026-09-29).

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S5-T | Every `androidHostTest` under `core/component`, plus feature UiState tests that assert a model default | [ ] |

## S5-T

- Delete: assertions on a style table's dp, alpha, colour role or typography (for example
  `ChipStyleTest`'s padding and border cases), and every `… defaults to …` constructor-default test.
- Keep: fixture-coverage tests (every variant appears in `previews`, unique keys), `resolve` functions with a
  real branch, and label, format and mapping logic.
- A test file left empty is deleted. Its `@Nested` class goes if nothing else remains in it.
- Verify with `:core:component:testAndroidHostTest` plus each touched feature module's `testAndroidHostTest`.
