# Component Library Migration — Implementation Plan

> **Lifecycle.** Delete this directory in the PR that finishes S12
> ([`steps/s12-close-out.md`](steps/s12-close-out.md)). The roadmap lives in GitHub Issues (see
> `CLAUDE.md` § Roadmap); this is a working plan for one rollout.

**Rollout model:** one branch, one PR, merged all at once. Stages are commit boundaries on that
branch, not separate pull requests. Every stage boundary must leave the branch compiling.

**Branch:** `275-migrate-every-component-into-a-corecomponent-library-driven-by-ui-models`
**Issue:** [#275](https://github.com/CinqueIzumi/Softcover/issues/275) — tag `E.1`, labels
`area:cross-cutting` / `kind:tech` / `scope:L`, no milestone. Keep its Stages and Acceptance
checkboxes in step with `## Steps` and `## Gates` here.

## Now

- **State:** S11-11 closed: reviewer round 2 clean. `StatNumber` springs only between loaded values. "Show N titles" waits on F42. PF-4/7 wait for S12-4.
- **Next:** S11-12, the `StatNumber` count-up starts when the mask clears (Phase 1 design with the user).
- **Open questions:** none. Accepted deltas: masthead `Counts` doesn't wrap; `SelectionHeader` lost ellipsis. Known limits, accepted: detekt 1.23.8 does not reject typos under `softcover:`; `padding(if (x) 4.dp else 8.dp)` and spacing held in vals / model fields are not flagged; corner radii in parentheses (`(8).dp`) or held in vals / model fields are not flagged; `UiModelBuiltInRender` sees calls only, not property reads (`Foo.Default`); `iosMain` is not gated (dead imports slip through).
- **Verification:** JBR 21 (detekt fails on JDK 26). Host tests of `:core:component`, reading, book_detail, library, explore, settings passed (new: `StatNumberTextTest`, `ShelveReadingStatus{Mapper,Collector}Test`, `SelectionCount{,Collector}Test`, masthead cases, percent clamps). `styleCheck` passed after the last Kotlin edit; AAA markers complete and glom check clean.
- **Uncommitted:** all S11-11 code and tests, the step file's `## Approved shape`, F42 in `foundation-upstream-candidates.md`, the `components.md` StatNumber and masthead lines, `module-structure.md`'s header row, this block.
## Local verification

Two pre-existing toolchain problems on this machine:

1. The aggregate `check` lifecycle cannot complete — `:app:compileDebugJavaWithJavac` fails inside
   `JdkImageTransform` (`jlink` from Homebrew JDK 26.0.2 against `android-37.0`).
2. detekt cannot run on JDK 26 at all — prefix every Gradle call with JDK 21:
   `JAVA_HOME=/Users/bartpeereboom/Library/Java/JavaVirtualMachines/jbr-21.0.11/Contents/Home`.

Run Gradle through `scripts/gradle-quiet.sh`, not `./gradlew` directly. The plain `test` lifecycle task
runs no KMP suites (`:app` / `:desktopApp` only); use `testAndroidHostTest --continue` under JDK 21 for
the KMP suites.

**Reliable per-change gates:** `checkModuleGraph`, `<module>:projectHealth`,
`<module>:compileKotlinJvm`, `ktlintCheck`, `<module>:detektJvmMain`, and **`styleCheck`** — all under
JDK 21. `styleCheck` is the only gate that catches detekt `MatchingDeclarationName`; include it in
every sub-step's Verify.

## How to run a step

1. Start a fresh session (`/clear`) and ask it to resume from this `## Now` block.
2. Read this file's `## Now` block, [`family-procedure.md`](family-procedure.md), and the one step
   file `## Now` names. Do not read the other step files.
3. Every sub-step ends the same way: acceptance passes → update `## Now` → tick the sub-step in the
   step file's `## Sub-steps` table and the matching row below → tick the box on
   [#275](https://github.com/CinqueIzumi/Softcover/issues/275) via `gh` → delete the step file once its
   last sub-step lands, and drop its `## Steps` link → **ask the user before committing** (subject-only
   message) → `/handoff` → the user types `/clear`.

## Steps

| Stage | Step file | Sub-steps | Status |
|---|---|---|---|
| S0–S4 | — | module scaffolding through tokens-only designsystem | [x] |
| S5 | — (chips) | S5-1–S5-3b, S5-C-chips | [x] |
| S5 | — (headers) | S5-4–S5-6, S5-C-headers | [x] |
| S5 | — (badges) | S5-7, S5-C-badges | [x] |
| S5 | — (skeletons) | S5-8, S5-C-skeletons | [x] |
| S5 | — (dividers) | S5-9–S5-10 | [x] |
| S5 | — (test prune) | S5-T | [x] |
| S6 | — (rows) | S6-1, S6-2 (audit only), S6-C-rows | [x] |
| S6 | — (sheet chrome) | S6-3–S6-7, S6-C-sheets | [x] |
| S7 | — (book cards) | S7-1–S7-7, S7-C1–C5 | [x] |
| SL | — (lean loading) | SL-1–SL-15e; SL-3b moved to foundation F34 | [x] |
| S8 | — (screen states) | S8-1–S8-3, S8-C-a–C-c | [x] |
| S8 | — (controls) | S8-4–S8-6, S8-C-controls | [x] |
| S9 | — (statistics & progress) | S9-1–S9-4, S9-C | [x] |
| SS | — (simplicity audit) | SS-0–SS-11, SS-C; after S9, before PF / S10 | [x] |
| TP | — (test prune) | TP-0 timings, TP-1–TP-8 per module group | [x] |
| SD | — (screen de-duplication) | SD-0 inventory, SD-P shell, SD-1–SD-6 per screen group | [x] |
| PF | [steps/pf-premium-feel.md](steps/pf-premium-feel.md) | PF-1–PF-7, any session before S11 | [ ] |
| S10 | — (shelf teardown) | S10-1–S10-5, S10-C | [x] |
| SP | — (spacing scale) | SP-1–SP-5, SP-C; after S10, before S11 | [x] |
| SH | — (shape scale) | SH-1–SH-4, SH-C; after S10, before S11 | [x] |
| S11 | [steps/s11-contract-retrofit.md](steps/s11-contract-retrofit.md) | S11-1–S11-40, S11-C | [ ] |
| PC | [steps/pc-performance-checks.md](steps/pc-performance-checks.md) | PC-1–PC-5 | [ ] |
| S12 | [steps/s12-close-out.md](steps/s12-close-out.md) | S12-1–S12-5 | [ ] |

## Decisions

- Book detail's shelve rows mask while the book loads, never seeded from the entry's known status (Explore entries don't know it).
- Test posture: no Compose UI tests; unit tests follow `.claude/rules/tests.md`; the Component Gallery is the visual-acceptance surface.
- A `:core:uibinding` → `:core:<data>` edge is pre-approved when a mapper needs one; write it when the edge exists.
- Loading follows `component-contract.md` R12; SL retrofits the existing surfaces.
- Every step builds the simplest shape: no style tables, single-variant fields or one-caller layers.
- A step spanning several sessions splits into sub-steps, one session each, in the step file's `## Sub-steps`.
## Baseline

Measured 2026-08-21, `release/3.2.0` @ `ada51050`.

| Metric | Value |
|---|---|
| `@Composable` declarations, whole repo | ~650 |
| …in `core:designsystem` at baseline | 132 |
| …feature-local | ~500 |
| Lines in composable-bearing `commonMain` files | 29,159 |
| …concentrated in six `*Shelf.kt` files | 12,182 |
| Types named `*UiModel` in the repo at baseline | 0 |

| File | Lines |
|---|---|
| `feature/book_detail/.../screen/BookDetailShelf.kt` | 2,864 |
| `feature/profile/.../screen/ProfileShelf.kt` | 2,368 |
| `feature/library/.../screen/LibraryShelf.kt` | 2,050 |
| `feature/explore/.../screen/ExploreShelf.kt` | 1,853 |
| `feature/reading/.../screen/ReadingShelf.kt` | 1,620 |
| `feature/settings/.../screen/SettingsShelf.kt` | 1,427 |

## Gates

Convention does not hold a boundary — `:core:designsystem` is the proof. Every rule below is a build
failure.

- [x] **G1 — `:core:component` ban list.** `checkModuleGraph`'s `componentLibraryAllowedProjects` /
      `componentLibraryBannedGroups` / `conventionProvidedCoordinates`, paired with a scoped detekt
      `ForbiddenImport` rule (`component-contract.md` § 7.4) that catches usage the coordinate check
      cannot see.
- [x] **G2 — `:core:designsystem` has zero project dependencies.** `zeroProjectDependencyModules` in
      `checkModuleGraph`, paired with a detekt `ForbiddenImport` on `core.domain.**` scoped to
      `**/core/designsystem/**`.
- [x] **G3 — The `:core:designsystem` → `:core:book` api allowlist row is gone.**
- [ ] **G4 — Composable budget ratchet.** A `checkComponentBudget` task counting `@Composable`
      declarations outside `:core:component` (excluding `*Preview` functions and platform
      `expect`/`actual`s). Ceiling set to the post-migration measured value; a rise fails the build.
      Wire into `check`. `steps/s12-close-out.md` S12-1 writes it.
- [ ] **G5 — Doc rule.** `docs/reference/design-system/` updated in the same change as any
      design-system change — enforced by `softcover-reviewer`, which treats a design-system change
      with no doc update as a blocker.
- [ ] **G6 — `./gradlew check` green**, including `styleCheck` (type-resolved detekt across every
      module) and `ktlintCheck`.
- [ ] **G7 — Human walkthrough** of every screen on device, `main` as baseline (S12-4).

**Open:** `:app:projectHealth` is unreachable on this machine (the pre-existing `JdkImageTransform`
failure); CI on a working toolchain covers it. `checkComponentBudget` (G4) is not written yet — the
count is still falling.

## Out of scope

Not components; they stay where they are.

- Screen composition roots — `*Screen`, `*ScreenLayout`, `*Shelf`, `Content`, `*Overlays`. S10 shrinks
  them; it does not move them.
- Navigation shells — `orchestration/presentation/`: `App`, `DesktopApp`, `CompactNavShell`,
  `WideNavShell`, `TabRootHost`, `BottomFloatingBar`, `DockedBottomNavigationBar`, `NavigationRailBar`,
  `EditorialSidebar`, `SidebarItem`, `BookDetailPaneHost`.
- Platform `expect`/`actual` — `BarcodeScanner` (common/android/ios/jvm), `isCameraAvailable`,
  `isCameraPermissionGranted`, `rememberCameraPermissionRequester`, `Theme.{android,ios,jvm}`,
  `TransientNavArg.{android,jvm}`.
- Debug screens — `MotionDebugScreen`, `ShareCardDebugScreen`, `DebugRoutesSection`, in
  `app/src/debug/`. `DebugRoutesContent`, the seam that binds them per build type, is in
  `:core:presentation`.
- Share cards keep their own family — never folded into `BookCard` — but migrated in S4 with
  everything else in `:core:designsystem` (§ 7.0's history, closed).
- `@Preview` functions — excluded from the G4 budget count.

## Risks

Cross-cutting only.

| Risk | Where | Mitigation |
|---|---|---|
| Long-lived red branch — all-at-once means the module split's compile breakage is resolved inside the branch. | S3, S4 | Stage boundaries must compile. Commit per stage so the PR is reviewable commit-by-commit even though it merges once. |
| Session loss mid-migration | any | This directory. Update checkboxes in the same commit as the work; keep the branch name in the header. |
| New component docs regrow `components.md` | any | The file is an index; new component docs go into KDoc on the UI model plus one index line. |
| Reviewer load — a single PR this size is hard to review. | merge | Commit-per-stage discipline; run `softcover-reviewer` per stage, not once at the end. |
