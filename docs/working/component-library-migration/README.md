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

- **State:** S0–S4 done. S5-1 done: `Chip` is the D11 + D12 family (`ChipVariant`, `ChipInteraction`,
  `ChipDimensions`), the ChooseLists pills and the `WhenReadRow` date pill render through it, and
  `ChooseListsRowUiModel.membershipChip` carries the row's chip. Reviewer: no findings.
- **Next:** [`steps/s5-chips.md`](steps/s5-chips.md) § S5-2, Phase 1 per
  [`family-procedure.md`](family-procedure.md): the `feature:library` chips, including the
  `ActiveFilterChip` dismiss question (D12's `ChipEvent.Dismissed` likely answers it).
- **Countdown:** Step 09 in `docs/working/token-hygiene/README.md` — 3 of 5 sessions.
- **Open questions:** none.
- **Verification:** compile for the five touched modules, `checkModuleGraph`, `ktlintCheck`,
  `styleCheck` and `checkDocBudgets` passed under JBR 21, and so did the targeted tests.
- **Uncommitted (all staged):** the S5-1 chip family under `core/component/.../chip/` (plus the new
  `ChipDimensions`, `ChipVariant`, `ChipInteraction` and `ChipFace`); its call sites in core:component
  lists/progress/share/gallery/topbar, `ChooseListsMapper`, and feature book_detail, library and
  settings; tests in core:component, core:uibinding, book_detail and library; `components.md` and the
  tracker docs; agent-memory notes under `.claude/agent-memory/`.

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
| S5 | [steps/s5-chips.md](steps/s5-chips.md) | S5-1–S5-3 | [ ] |
| S5 | [steps/s5-headers.md](steps/s5-headers.md) | S5-4–S5-6 | [ ] |
| S5 | [steps/s5-badges.md](steps/s5-badges.md) | S5-7 | [ ] |
| S5 | [steps/s5-skeletons.md](steps/s5-skeletons.md) | S5-8 | [ ] |
| S5 | [steps/s5-dividers.md](steps/s5-dividers.md) | S5-9 | [ ] |
| S6 | [steps/s6-list-rows.md](steps/s6-list-rows.md) | S6-1–S6-2 | [ ] |
| S6 | [steps/s6-sheet-chrome.md](steps/s6-sheet-chrome.md) | S6-3–S6-7 | [ ] |
| S7 | [steps/s7-bookcard.md](steps/s7-bookcard.md) | S7-1–S7-7 | [ ] |
| S8 | [steps/s8-screen-states.md](steps/s8-screen-states.md) | S8-1–S8-3 | [ ] |
| S8 | [steps/s8-controls.md](steps/s8-controls.md) | S8-4–S8-5 | [ ] |
| S9 | [steps/s9-statistics.md](steps/s9-statistics.md) | S9-1–S9-4 | [ ] |
| S10 | [steps/s10-shelf-teardown.md](steps/s10-shelf-teardown.md) | one per `*Shelf.kt` | [ ] |
| S11 | [steps/s11-contract-retrofit.md](steps/s11-contract-retrofit.md) | S11-1–S11-3 | [ ] |
| S12 | [steps/s12-close-out.md](steps/s12-close-out.md) | S12-1–S12-4 | [ ] |

## Decisions

- **D1** Gallery reachability: shipped easter egg, not debug-only. Seven taps on `VersionFooter`, each
  within two seconds of the last, `milestone` haptic on the seventh, `noRippleClickable` so the footer
  looks untouched. Counting logic in `SecretTapCounter`, unit-tested. Registry (`GalleryRegistry` /
  `GalleryEntry` / `GalleryFamily`) lives in `:core:component/gallery/`; the screen
  (`ComponentGalleryScreen`) lives in `feature:settings`, since G1 bans `:core:component` from Voyager.
  Anatomy: `component-contract.md` § 7.5.
- **D2** `:core:uibinding` dependency visibility: `api`. Every domain model lives in `:core:domain`, a
  dependency-free contract module, so no allowlist row is required today. A
  `:core:uibinding` → `:core:<data>` row is pre-approved when a mapper needs one — write it when the
  edge exists, not speculatively. Policy detail: `module-structure.md`.
- **D3** `ShareCard.kt` (1,161 lines) split per body while renaming, in S4 — not deferred to S12
  (`component-contract.md` § 7.3, § 7.0).
- **D4** `SoftcoverDatePickerDialog` lands in `:core:component/dialog/`, moved as-is at the merge —
  `:core:designsystem` is tokens-only (G2), so it cannot land where it started.
- **D5** Easter-egg gesture spec: seven taps, a two-second window between consecutive taps, `milestone`
  haptic on unlock, `noRippleClickable` so the footer looks untouched.
- **D6** Fixture size in the release binary: not measured; measure at S12.
- **D7** Test posture: no Compose UI tests in this repo, none planned — coverage is unit tests on
  every UI model and mapper, plus the Component Gallery as the visual-acceptance surface.
- **D8** (2026-09-28) The target module shape and the `:core:component` package layout live in
  `module-structure.md`, not here.
- **D9** (2026-09-28) Each family step's Phase 1 proposes the UI model / variant / event shape to the
  user; the approved shape is recorded here as a new D-number before Phase 2 starts.
- **D10** (2026-09-28) A step large enough to span several sessions splits into sub-steps, one session
  each, tracked in the step file's `## Sub-steps` table.
- **D11** (2026-09-28) Chips and pills are one family on `Chip` + `ChipUiModel(key, label, variant,
  interaction)`. `ChipVariant` is sealed: `Tonal(selected)`, `Spoiler` (replaces `concealed`), `Add`,
  `AddOutlined`, `Remove`, `Format(active, face: ChipFace)`. `ChipInteraction` is `Clickable` /
  `Disabled` / `Inert` (replaces `clickable`). Metrics come from a per-variant table, so the migration is
  visually neutral. `ChipEvent` stays `Clicked(key)`. `MembershipPill` goes: `ChooseListsMapper` builds
  the pill as `ChooseListsRowUiModel.membershipChip`. `FormatChip` moves in S8-4 with the toolbar's own model.
  `SearchChromePill` was a search field, not a chip, and is renamed `SearchChromeField`.
- **D12** (2026-09-28) The `WhenReadRow` date pill is `Tonal(selected)`. To carry it, `ChipUiModel` gains
  two variant-independent fields: `leadingIcon: SoftcoverIcon?` and `dismissLabel: String?`. A non-null
  `dismissLabel` renders a trailing ✕ as its own tap target, uses the label as the ✕'s content
  description, and reports `ChipEvent.Dismissed(key)`. S5-2's `ActiveFilterChip` reuses it. `Remove`'s
  ✕ stays display-only. The date pill takes Tonal's `onSurface` idle ink, an accepted visual change.

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

**Open:** `:app:projectHealth` is unreachable on this machine (the pre-existing `JdkImageTransform`
failure); CI on a working toolchain covers it. `checkComponentBudget` (G4) is not written yet — the
count is still falling.

## Out of scope

Not components; they stay where they are.

- Screen composition roots — `*Screen`, `*ScreenLayout`, `*Shelf`, `Content`, `*Overlays`. S10 shrinks
  them; it does not move them.
- Navigation shells — `orchestration/presentation/`: `App`, `DesktopApp`, `CompactNavShell`,
  `WideNavShell`, `TabRootHost`, `BottomFloatingBar`, `DockedBottomNavigationBar`, `NavigationRailBar`,
  `EditorialSidebar`, `SidebarItem`, `BookDetailPaneHost`, `ReAuthDialog`.
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
