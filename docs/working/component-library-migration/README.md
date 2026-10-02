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

- **State:** S7-1 done (its box in `steps/s7-bookcard.md` still unticked); SL-1 done (`SkeletonText`). SL is
  app-wide and ends with the `Skeleton` family deleted; its sub-steps interleave with S7 and S9.
- **Next:** SL-2 — seed `title`, `seriesText`, `rating` on `BookInitialCover` and its call sites, per
  [steps/sl-lean-loading.md](steps/sl-lean-loading.md).
- **Open questions:** `BookmarkToggle` → foundation toggle? Issue #275's Stages list has no SL entry yet.
- **Visual checks, for the final UI pass (S12):** confirm on device that the Arrange / Filter sheets no longer
  render empty or stale for a frame on open. Eyeball the accepted shifts: S5-6's 4dp eyebrow gap, 1-line
  titles, reading's mobile masthead line height; S5-C-headers' "Your tabs" tracking (2.2 → 1.2sp) and
  `FeaturedCard`'s eyebrow gap (10 → 8dp); the whole badge family in the gallery and on device (never
  visually checked since S4-5b) — SemiBold everywhere, TrackingTag pill → 6dp, Owned 14 → 12dp icon,
  cover selection mark 26 → 24dp, Expired on `surfaceContainerHigh`; S5-C-dividers' quote-glyph gap
  (14 → 12dp), the "Or type it" label on plain `eyebrowSmall`, and the version footer on `eyebrowSmall`;
  S6-C-rows' settings rows (16dp vertical everywhere, 4dp title gap, top rule and press feedback on
  toggle / choice lists); S6-3's sheet header gaps (12/8/8), 56dp jackets, sheet padding (top 8 / bottom 16,
  LoadingSheet 32 → 16), and the progress sheet's tap-to-clear-focus now covering the body only; S6-4's
  verdict cover as a 56dp header jacket (was above the header); S6-5's Filter footer inset (22 → 24),
  bulk-remove and re-auth as sheets, and the animated close on Apply / Save / Delete / top action; S6-6's
  tag editor and edition jackets (56dp), the edition footer divider, ShareBook's eyebrow bar, and the
  unknown-ISBN footer; S6-7's streak sheet insets, log-out no longer centred, and the genre /
  Continue-series headers; that the Arrange sheet's "Show titles" row does not pop in a frame after open
  (collector-only writer now); S6-C-sheets' header trailing gap (16 → 12), verdict jacket radius 4, the
  TEXT dismiss label's right edge, ShareBook's TEXT Cancel, animated state-driven closes.
- **Verification:** SL-1: `:core:component:compileKotlinJvm` and `styleCheck` (JDK 21) passed; reviewed clean.
- **Uncommitted:** none.

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
| S7 | [steps/s7-bookcard.md](steps/s7-bookcard.md) | S7-1–S7-7 | [ ] |
| SL | [steps/sl-lean-loading.md](steps/sl-lean-loading.md) | SL-1–SL-11, app-wide; SL-1–SL-5 before S7-2 | [ ] |
| S8 | [steps/s8-screen-states.md](steps/s8-screen-states.md) | S8-1–S8-3 | [ ] |
| S8 | [steps/s8-controls.md](steps/s8-controls.md) | S8-4–S8-5 | [ ] |
| S9 | [steps/s9-statistics.md](steps/s9-statistics.md) | S9-1–S9-4 | [ ] |
| S10 | [steps/s10-shelf-teardown.md](steps/s10-shelf-teardown.md) | one per `*Shelf.kt` | [ ] |
| S11 | [steps/s11-contract-retrofit.md](steps/s11-contract-retrofit.md) | S11-1–S11-5 | [ ] |
| S12 | [steps/s12-close-out.md](steps/s12-close-out.md) | S12-1–S12-5 | [ ] |

## Decisions

- Test posture: no Compose UI tests; unit tests follow `.claude/rules/tests.md`; the Component Gallery is the visual-acceptance surface.
- A `:core:uibinding` → `:core:<data>` edge is pre-approved when a mapper needs one; write it when the edge exists.
- Loading: each component takes `isLoading`, masks its own leaves (`skeletonMask`, `SkeletonText`).
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

Cross-cutting only; S7's own risks live in `steps/s7-bookcard.md`.

| Risk | Where | Mitigation |
|---|---|---|
| Long-lived red branch — all-at-once means the module split's compile breakage is resolved inside the branch. | S3, S4 | Stage boundaries must compile. Commit per stage so the PR is reviewable commit-by-commit even though it merges once. |
| Session loss mid-migration | any | This directory. Update checkboxes in the same commit as the work; keep the branch name in the header. |
| New component docs regrow `components.md` | any | The file is an index; new component docs go into KDoc on the UI model plus one index line. |
| Reviewer load — a single PR of this size is not reviewable in the normal way. | merge | Commit-per-stage discipline; run `softcover-reviewer` per stage, not once at the end. |
