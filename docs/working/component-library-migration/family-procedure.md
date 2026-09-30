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
4. Stop for approval. Record the approved shape in the step file's `## Approved shape`, one line per
   point, before Phase 2 starts. It is deleted with the step file; once built, the code and its KDoc are
   the record. Decisions go in the README only when they bind every step.

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

## Phase 3 — convergence pass, closing every step file

The last sub-step of every step file is a critical pass over the family as it now stands. A
variant-by-variant comparison is only the first layer. Audit every layer below before proposing
anything:

1. **Inventory.** List every call site of the family's components and every variant in use (`git grep`).
   Table the variants and their exact differences: colours, padding, type, shape, icon size and gap,
   interaction.
2. **Rendering code.** Look for near-identical bodies inside the component: the same modifier chain,
   colour resolution, padding, spacing or icon placement repeated per variant. Target one private
   scaffold, with each variant resolving only a small style (container, ink, border, weight).
3. **Metrics.** Collapse per-variant dimension tables to shared constants and as few sizes as the call
   sites need. Use one typography and weight rule, not one per variant.
4. **Content versus style.** Glyphs drawn as text ("+", "✕"), icons hard-wired into a variant, and
   content descriptions stored on a colour variant all belong in content slots on the model.
5. **Model shape.** Audit the model itself, not only its variants:
   - Does each type carry exactly one axis (colour, layout, state, content)?
   - Is any field ignored by some variant, or does the scaffold branch on the variant? If so, the model
     allows combinations it cannot render.
   - Is there logic that belongs in the model but lives in composition or the callers, or the reverse?
   - What is missing that you would expect, such as accessibility semantics (selected, disabled, icon
     descriptions)? What is present that you would not expect?
6. **Callers.** Look for logic every consumer repeats around the component, for example a model list
   kept next to a `key → payload` map with the same lookup code. A generic helper in `:core:component`
   should absorb it.
7. **Testability.** Resolution logic that is `@Composable` only to read the theme can take a
   `ColorScheme` instead and be unit-tested; this repo has no Compose UI tests.
8. **Correctness.** Check invisible or duplicate tap targets, width reflow between states, missing
   ellipsis, raw px where dp is meant, and KDoc claims the code contradicts.

Then:

1. Propose structural changes first (layers 2–7), and pairwise merges and fixes after them. Accept small
   visual shifts where no design reason justifies the difference.
2. Stop for approval. Record the outcome in the step file's `## Approved shape`, including "nothing to
   converge" and why.
3. Build the approved changes per Phase 2.

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
