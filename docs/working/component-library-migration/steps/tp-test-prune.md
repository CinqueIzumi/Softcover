# TP — Test prune

**Stage:** after SS, before SD, so every later step's agent rounds run and fix a smaller, faster suite.
**Delegation:** user (Phase 1 per sub-step), `softcover-test-writer` (Phase 2: deletes, merges and speed-ups).

**Goal:** every unit test under `src/*Test*/` (606 files across `core/*`, `feature/*` and `orchestration`)
earns its place. What is left checks behaviour, would be missed, is not a copy of another test, and runs
fast. Production code does not change. A slow test caused by slow production code is reported to the user
and not fixed here.

## Keep or cut

Every test is measured against the checks in [`.claude/rules/tests.md`](../../../../.claude/rules/tests.md):
cut it, merge it, rewrite it to assert observable output, or speed it up.

The same files also get a style sweep: Arrange / Act / Assert, no glommed `) }` or `),)` closers
(including the ~24 in `ChooseListsMapperTest`), and Kotest `shouldNotBeNull()` instead of `!!`.

## Procedure

**TP-0 first, in its own session:** run `testAndroidHostTest --continue` under JDK 21 (README § Local
verification). From the `build/test-results/**/*.xml` reports, list each module's test files, test count
and total time, plus the 20 slowest classes. Split the modules into session-sized groups in the table below.

Per sub-step: name every test the questions above would cut, merge or speed up, by class and method, with
the reason for each and the expected change in file count, test count and time. Stop for approval. Then
brief `softcover-test-writer` with the approved list. Verify with the group's `testAndroidHostTest` and
`styleCheck`, and report the before and after test count and time.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| TP-0 | Run the suite with timings; fill this table | [ ] |

## Approved shape
