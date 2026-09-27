# S11 — Contract retrofit (R10 + R11)

**Stage:** S11 — sweeps every call site that builds a UI model in composition, or passes a loose
render parameter, before the migration's final verification. **Delegation:** user (Phase 1),
`softcover-implementer` (Phase 2).

**Completion bar:** `component-contract.md` § 7.4a ends with no exceptions.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| S11-1 | R10 sweep: every UI model built outside a collector, behind a build gate | [ ] |
| S11-2 | R11 holdouts | [ ] |
| S11-3 | `TopBarSurface`, the `ShareCardPalette` fold, the final § 7.4a check | [ ] |
| S11-C | Convergence pass over the family (family-procedure.md § Phase 3) | [ ] |

## S11-1 — R10 sweep

A full sweep, not a list: every UI model constructed, copied or mapped outside the state layer moves onto
the `UiState`, assembled by the collector.

1. **Gate first.** Add a detekt rule that fails when render code (composables, `presentation/screen/**`,
   `presentation/component/**`, `:core:component` render files, `app/src/debug`) calls a `*UiModel`
   constructor, a `*UiModel.copy(…)`, or a `to*UiModel(…)` mapper — in composition, in `remember`, or as
   a file-level constant. Allowed: collectors, ScreenModels, actions, state builders, mappers, fixtures
   (`UiModelPreviews`), the gallery, and each recorded § 7.4a exception (named by symbol in the rule's
   config).
2. **Sweep.** Run the gate; its findings are the work list. Fix every one, split into feature-sized
   briefs.
3. **Done** when the gate is green on the whole repo with no suppressions and no baseline entries.

S11 settles the three R10 edges `component-contract.md` names. Two stay open on purpose, as recorded
exceptions rather than holdouts: a component resolving its own copy from `composeResources`
(`offlineBannerUiModel()`), and the preview fixtures / Component Gallery, which construct models by
definition. The third, a model that depends on a value only composition has (scroll position, window
size class, a `CompositionLocal`), is decided in S11-3 through `TopBarSurface`.

## S11-2 — R11 holdouts

Components still taking a loose render parameter instead of the model deciding it: `RichText`,
`StatNumber`, `ClickableText`, `MarkAsReadBurst`, `CoverlessTitleCover`, and `VerdictBlock` /
`VerdictSheet` (whose R1 exception closes here too).

## S11-3 — `TopBarSurface`, `ShareCardPalette`, final check

- Settle book detail's scroll-derived `TopBarSurface`, and with it R10's composition-only-value edge:
  either the rule for it goes into `component-contract.md` R10, or it becomes a § 7.4a exception.
- Fold `ShareCardPalette`'s two parallel colour `when`s into a per-variant lookup.
- Run the final `component-contract.md` § 7.4a check: no remaining exception rows.
