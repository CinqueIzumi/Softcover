# TP — Test prune

**Stage:** after SS, before SD, so every later step's agent rounds run and fix a smaller, faster suite.
**Delegation:** user (Phase 1 per sub-step), `softcover-test-writer` (Phase 2: deletes, merges and speed-ups).

**Goal:** every unit test under `src/*Test*/` (622 classes across `core/*`, `feature/*` and `orchestration`)
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
the reason for each and the expected change in file count, test count and time. No approval stop (user,
2026-10-06): brief `softcover-test-writer` with the list straight away. Verify with the group's `testAndroidHostTest` and
`styleCheck`, and report the before and after test count and time.

## Sub-steps

| Sub | Scope | Status |
|---|---|---|
| TP-0 | Run the suite with timings; fill this table | [x] |
| TP-1 | `core:component`, `core:uibinding`, `core:designsystem` — 50 files, 421 → 339 tests, 1.3 s | [x] |
| TP-2 | `core:book`, `core:lists`, `core:profile`, `core:personal`, `core:network` — 75 files, 918 → 656 tests, 24.7 → ~11 s | [x] |
| TP-3 | `core:preferences`, `core:database`, `core:domain`, `core:deadlines`, `core:connectivity`, `core:presentation`, `core:identity`, `core:notification`, `orchestration` — 85 files, 787 → 424 tests, 12.6 s | [x] |
| TP-4 | `feature:book_detail` — 110 files, 807 → 524 tests, 4.4 → 3.1 s | [x] |
| TP-5 | `feature:library` — 84 files, 784 tests, 4.8 s | [ ] |
| TP-6 | `feature:explore` — 76 files, 515 tests, 7.1 s | [ ] |
| TP-7 | `feature:settings`, `feature:app_update`, `feature:onboarding` — 68 files, 366 tests, 5.8 s | [ ] |
| TP-8 | `feature:reading`, `feature:profile`, `feature:scan`, `feature:lists`, `feature:session` — 74 files, 590 tests, 8.6 s | [ ] |

## TP-0 baseline

2026-10-06, JDK 21, `--no-build-cache`, fresh reports: 622 test classes, 5,208 tests, 0 failures, 69.5 s summed
test time (Gradle wall clock 1 m 33 s; modules run in parallel). "Files" counts outer classes; the XML reports
split them per `@Nested` class. Per-module figures are in the sub-step table above.

Slowest classes (s): `BooksRemoteDataSourceImplTest` 9.58 (92 tests), `SearchRemoteDataSourceImplTest` 3.54
(26), `ListsRemoteDataSourceImplTest` 2.40 (30), `BookMapperTest` 2.03 (67), `AppUpdateDataSourceImplTest`
1.59 (14), `DismissedContinueSeriesDaoTest` 1.56 (5), `OnHideUntaggedAuthorsToggledActionTest` 1.49 (5),
`BooksLocalDataSourceImplTest` 1.46 (47), `ReadingJournalHistoryRemoteDataSourceImplTest` 1.38 (4),
`OnApiKeySaveClickActionTest` 1.37 (15), `ListsLocalDataSourceImplTest` 1.27 (8),
`OnAddUnknownIsbnConfirmedActionTest` 1.23 (11), `ProfileRemoteDataSourceImplTest` 1.21 (31),
`SoftcoverCacheResolverTest` 1.19 (8), `DismissProgressSheetActionTest` 1.18 (5), `PendingListWriteStoreTest`
1.17 (6), `OnPageDraftChangedActionTest` 1.16 (7), `ApiFailureHandlingTest` 1.12 (6), `CreateListUseCaseTest`
1.08 (3), `UserTagVocabularyLocalDataSourceImplTest` 0.99 (7).

Most ~1 s entries are JVM warm-up: each module's first class pays ~0.7–1.5 s (~25 s of the 69.5 s), so a
small class on that list is not slow by itself. The real outliers are the remote data source tests and
`BookMapperTest`; TP-2 and TP-6 start with those.

## Approved shape
