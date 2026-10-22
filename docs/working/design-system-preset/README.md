# Design system preset and showcase

A `:showcase` Android app that renders every component from fixtures, live and interactive, and a
repo-committed `design/` preset that turns the real tokens, component renders and a hand-written brand book into a
Claude Design System artifact any contributor can publish. Nothing here touches `:app` builds or the APK.
No GitHub issue. Branch `design-system-preset` (from `release/3.3.0`, local only), worktree
`../Softcover-design-system-preset`.

## Now

- **State:** steps 0–2e committed (`0dccb6922`): 17 live family pages under `showcase/.../live/<family>/`,
  routed from Home via `live/LiveScreens.kt`, sharing `appearance/action/` generic actions and
  `AppearanceSheetHost`. Step 3 was built, then dropped and fully reverted at the user's call (see
  Decisions); steps 3 and 7 are dropped.
- **Committed:** step 4 render runner (`549c8368d`); step 5 brand book, components and `designPresetBundle`
  (`9fcbb8b64`, 740 bundle files).
- **Step 6 written:** `.claude/skills/publish-design-system/SKILL.md` (user-invoked: build, create or read
  the artifact from `local.properties` `design.artifactUrl`, upload `assets/**` into `assetGroups`, publish
  files in ≤255-path / ≤16 MiB calls with the cover last, write the index last, merging into the live one on
  update), `design/README.md`, a CLAUDE.md "Where things live" row. Shapes from the Design System type's
  `SKILL.md` (`https://claude.ai/artifact/5M7UeXXcx16TP3vzVFNDzd`). Not yet run.
- **Next:** in a session started from this worktree (skills load from the launch directory, so the main
  checkout's session never listed it), the user runs `/publish-design-system` to prove it end to end; fix the
  skill from what breaks. Then step 8 (reviewer, `styleCheck`, `check buildHealth`).
- **Open questions:** none.
- **Verification:** `checkDocBudgets` passed; `styleCheck` passed after the last Kotlin edit (step 5).
- **Uncommitted:** the publish skill, `design/README.md`, the CLAUDE.md row and this tracker.
- **Carry:** on the next foundation bump, add `**/jvmDesignPresetTest/**` to the shared detekt test globs and
  drop the local `FunctionNaming` block.

## Steps

One step per session. Commits only when the user asks.

- [x] **0. Rule amendments (main conversation).** `.claude/rules/tests.md`: Robolectric and Roborazzi allowed in
  `:showcase` only; the render runner is a generator that asserts nothing and is written by
  `softcover-implementer`. `docs/reference/design-system/component-contract.md:281`: the showcase app and its
  renders join the in-app gallery as the visual check.
- [x] **1. Token export (`softcover-implementer`).**
  - Refactor `appTypography()` (`core/designsystem/.../theme/Type.kt:135-157`) into a non-composable
    `buildAppTypography(display, body)`; refresh the stability baseline if it moves.
  - A `jvm().compilations.create("designPreset")` in `:core:designsystem` with `associateWith(main)`, run by a
    `JavaExec` task, with its detekt task wired by hand. Output into `build/`, never committed:
    `tokens.json` in the Design System shape (themes Softcover light, dark, dim; every `ColorScheme` role
    enumerated by reflection, fixed inks, `RatingGold`, spoiler roles; Material and editorial type roles with
    families and tabular figures; `Spacing`; `CornerRadius` plus `pill`), `Palettes.md` (Vellum, Ink, Foxed,
    Sea role tables, light and dark), icons, illustrations (with dark variants) and the launcher foreground as
    SVG (`?attr/colorControlNormal` → `currentColor`).
  - Deterministic: `Locale.ROOT` formatting, sorted file walks, `.gitattributes` `design/** text eol=lf`.
  - A content check in root `check`: every token the README and `bundle.css` cite exists, `bundle.css` has no
    raw hex.
  - Tests (`softcover-test-writer`): colour formatting with alpha, role enumeration, the content check.
- [x] **2. `:showcase` skeleton (`softcover-implementer`).** Android application module, own applicationId,
  `app` tier in `tierOf` (`build.gradle.kts:367`), excluded from kover aggregation. Move the gallery render loop
  (`feature/settings/.../ComponentGalleryContent.kt:166-286`) into a public composable in `:core:component`,
  used by Settings and the showcase. Opens on a palette × brightness switcher and the component gallery.
- [x] **2b. Remove the Settings gallery (`softcover-implementer`).** The `ComponentGallery*` screen, state,
  actions, collectors, mapper, layouts, `SecretTapCounter` and their tests; the About / Settings / version
  footer entry points and `SettingsModule` wiring; stability baseline; `MinifiedContractTest.kt:230`; docs and
  rules naming it. Rewrite `component-contract.md` §7.5: the showcase is the visual check; a new component
  still adds its `GalleryRegistry` entry, and one with behaviour also adds a live demo in `:showcase`.
- [x] **2c. Showcase shell (`softcover-implementer`).** Voyager + Koin; a showcase-wide theme store (palette,
  brightness) applied app-wide, changed from a top-bar sheet; home list of the 32 families, live ones marked;
  a family page of registry fixtures; a root `SoftcoverSnackbarHost`. Remove the framed-gallery screen and
  `GalleryThemedRegion`.
- [x] **2d. Overlay demos (`softcover-implementer`, parallel with 2e).** Sheets via `SheetSession` on the
  UiState (scaffolds, progress, verdict, quote, choose-lists, share-card; short, long, loading, error cases),
  the date picker dialog, snackbars via `SnackBarManager` (plain, with action, over a sheet), the
  mark-as-read burst, dismissable banners.
- [x] **2e. In-page demos (`softcover-implementer`, parallel with 2d).** Top bars on a scrolling page with
  `enterAlwaysScrollBehavior`, search and editorial bars, a live sidebar, stateful chips / controls / fields
  / selection, chrome buttons over a cover. Each family page is a small ScreenModel; tests per round.
- ~~**3. Screen showcase entries.**~~ Dropped (see Decisions).
- [x] **4. Render runner (`softcover-implementer`).** Spike first: Fraunces and a `SoftcoverIcon` render under
  Robolectric from `:showcase`; stop and report if not. Robolectric + Roborazzi as `:showcase` test deps only;
  runs only when a Gradle property is set (class filter, `roborazzi.test.record=true`), never in `check` or
  `koverXmlReport`. One capture per fixture (no tall sheets), phone size via `@Config(qualifiers=…)`, light,
  dark and dim; each hand-written component name must exist in `GalleryRegistry`. Spacing via tokens (detekt
  runs on test code).
- [x] **5. Content and bundle (main conversation + `softcover-implementer`).** `design/project/README.md`
  brand book (voice, foundations, layout, decision rules as usage rules naming tokens), `components/bundle.css`,
  12 hand-written components (Cover, BookCard, Chip, PillButton, SectionHeader, PageMasthead, StatNumber,
  StatTile, a chart, Toggle, SheetRow, EmptyState) with `preview.html` + README, the cover. Root
  `designPresetBundle` (never `assemble*` / `publish*`: the slow-Gradle hook blocks them) builds
  `build/design-preset/project/`: committed content, generated tokens and sections, fonts, SVGs, renders,
  generated README + preview for PNG-only components.
- [x] **6. Publish skill and docs (main conversation).** `.claude/skills/publish-design-system/SKILL.md`: run
  the bundle, create or update the contributor's own artifact (URL in `local.properties` `design.artifactUrl`),
  upload icons and logo as assets, write the index, publish in ≤255-path batches with the index last.
  `design/README.md` for contributors; a CLAUDE.md "Where things live" row.
- ~~**7. Replace `screenshots/`.**~~ Dropped (see Decisions).
- [ ] **8. Gates.** `softcover-reviewer`, `styleCheck`, `./gradlew check buildHealth`.

## Decisions

- Themes are the default Softcover palette (light, dark, dim); the other four palettes are a `Palettes.md` section (artifact cap: 8 themes).
- No screen renders: outside the app shell the phone layouts lack the tab bar, real covers and live state, so the preset ships components and foundations only and the README screenshots stay hand-captured.
- Hand-written HTML for the 12 core components only; the rest ship as PNG renders.
- Generated tokens and PNGs are built, never committed.
- `:showcase` is a separate app module, not an `:app` flavor.
- The showcase is interactive (real sheets, dialogs, snackbars, scrolling bars) and replaces the Settings
  gallery. `GalleryRegistry` stays in `:core:component` (its entries use internal sheet bodies) as the
  still-render source for step 4.

## Risks

- Compose resources (fonts, drawables) under Robolectric are unverified: step 4's spike.
- `checkModuleGraph` skips untiered paths (`build.gradle.kts:702`); step 2 tiers `:showcase`.
