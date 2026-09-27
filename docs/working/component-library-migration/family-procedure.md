# Family procedure

What every S5–S9 family step shares. Read this once per session; a step file only adds what is
specific to it.

An inventory table's `path:line` is the audit's starting address, not a guarantee — paths drift.
Everywhere else, cite symbols (`fun Name(`), never line numbers.

## Phase 1 — design, with the user

1. Re-verify the sub-step's inventory by symbol: `git grep -n "fun <Name>("` for every row, main
   source sets only, tests excluded.
2. Read the family's `docs/reference/design-system/components/<family>.md` entries and
   `component-contract.md` § 7 (R1–R11). Never copy the rules into the step file or the brief.
3. Propose to the user:
   - the UI model / variant / event shape;
   - which call sites fold into the one component, which stay feature-local, and why;
   - the target package under `:core:component`;
   - any mapper, and whether it starts feature-local or promotes straight to `:core:uibinding` (R6:
     promote when two features converge on it).
4. Stop for approval. Record the approved shape in [`README.md`](README.md) § Decisions as a new
   D-number before Phase 2 starts.

## Phase 2 — build

One brief per sub-step to `softcover-implementer`, filled per
[`agent-briefs.md`](../../reference/agent-briefs.md). Verify:

```
scripts/gradle-quiet.sh :core:component:compileKotlinJvm :<feature>:compileKotlinJvm \
  checkModuleGraph :<module>:projectHealth ktlintCheck styleCheck
```

under JBR 21 (see [`README.md`](README.md) § Local verification). Then `softcover-test-writer` for
every new UI model and mapper. Then `softcover-reviewer`, with `## Scope` = the sub-step's changes.

Phase 1 and Phase 2 may share a session; hand off between them if the context-budget notice fires.

## Standard acceptance, every sub-step

- Verify is green.
- Tests are written and passing.
- The reviewer has no blockers.
- Each new component has fixtures (`UiModelPreviews`) and a `GalleryRegistry` entry.
- R10 and R11 hold for the new code — no model built in composition, no loose render parameter.
- The migrated `[detail]` entries in `components/<family>.md` are converted to KDoc on the UI model
  (through the `document-code` skill) and deleted, with the `components.md` index line repointed.
- The family's section files have moved into `:core:component` where they are components.
- `module-structure.md`'s package layout is updated (`·` → `+`).

## Practices carried forward

- Splitting a file always widens the visibility of whatever crosses the new file boundary — expect it
  rather than treating a split as free.
- Hunting an unused import set: delete every import and let the compiler name what it cannot resolve,
  round by round, rather than eyeballing which imports are dead.
