# Design system preset and showcase

A `:showcase` Android app that renders every component and the five main phone screens from fixtures, and a
repo-committed `design/` preset that turns the real tokens, those renders and a hand-written brand book into a
Claude Design System artifact any contributor can publish. Nothing here touches `:app` builds or the APK.
No GitHub issue. Branch `design-system-preset` (from `release/3.3.0`, local only), worktree
`../Softcover-design-system-preset`.

## Now

- **State:** steps 0–2 done; reviewer passed step 2.
- **Next:** step 3, the screen showcase entries.
- **Open questions:** none.
- **Carry into step 3:** with Koin in `:showcase`, add `core-common` and inject `AppDispatchers` into
  `ShowcaseScreenModel` (drop `Dispatchers.Main.immediate` from `ShowcaseScreen`).
- **Carry into step 5:** pass the exporter's paths through a `CommandLineArgumentProvider` with declared
  input/output dirs (they are absolute `args` today); list `exportDesignTokens`, `checkDesignPresetContent`
  and `testDesignPreset` in `.claude/rules/build-wiring.md`. On the next foundation bump, add
  `**/jvmDesignPresetTest/**` to the shared detekt test globs and drop the local `FunctionNaming` block.

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
- [ ] **3. Screen showcase entries (5 parallel `softcover-implementer`s).** Library, Reading, Book detail,
  Explore, Profile each expose public entries rendering the phone layout (not the Screen) with fixture states in
  main code, beside `ExploreMockData` / `ProfileScreenPreviewState`. `:showcase` provides real fakes (no relaxed
  mocks) for `ActiveSessionController`, `AppNavigator`, `NavPulse`, `TabReselectRegistry`, in an isolated Koin
  context. The showcase lists the screens.
- [ ] **4. Render runner (`softcover-implementer`).** Spike first: Fraunces and a `SoftcoverIcon` render under
  Robolectric from `:showcase`; stop and report if not. Robolectric + Roborazzi as `:showcase` test deps only;
  runs only when a Gradle property is set (class filter, `roborazzi.test.record=true`), never in `check` or
  `koverXmlReport`. One capture per fixture (no tall sheets), phone size via `@Config(qualifiers=…)`, light,
  dark and dim; each hand-written component name must exist in `GalleryRegistry`. Spacing via tokens (detekt
  runs on test code).
- [ ] **5. Content and bundle (main conversation + `softcover-implementer`).** `design/project/README.md`
  brand book (voice, foundations, layout, decision rules as usage rules naming tokens), `components/bundle.css`,
  12 hand-written components (Cover, BookCard, Chip, PillButton, SectionHeader, PageMasthead, StatNumber,
  StatTile, a chart, Toggle, SheetRow, EmptyState) with `preview.html` + README, the cover. Root
  `designPresetBundle` (never `assemble*` / `publish*`: the slow-Gradle hook blocks them) builds
  `build/design-preset/project/`: committed content, generated tokens and sections, fonts, SVGs, renders,
  generated README + preview for PNG-only components.
- [ ] **6. Publish skill and docs (main conversation).** `.claude/skills/publish-design-system/SKILL.md`: run
  the bundle, create or update the contributor's own artifact (URL in `local.properties` `design.artifactUrl`),
  upload icons and logo as assets, write the index, publish in ≤255-path batches with the index last.
  `design/README.md` for contributors; a CLAUDE.md "Where things live" row.
- [ ] **7. Replace `screenshots/`.** Generate the six README shots (`reading`, `library`, `book-detail`,
  `explore`, `profile-stats`, `profile-taste`; dark, 1080×2300, quantised) from the showcase renders via a
  task, and rewrite `screenshots/README.md` (drop the duplicated lines 58-60 / 62-64). Settle with the user
  first: fixtures draw coverless monogram covers, where the current shots show real cover art.
- [ ] **8. Gates.** `softcover-reviewer`, `styleCheck`, `./gradlew check buildHealth`.

## Decisions

- Themes are the default Softcover palette (light, dark, dim); the other four palettes are a `Palettes.md` section (artifact cap: 8 themes).
- Screens render the phone layouts via Robolectric in `:showcase`; no desktop layouts.
- Hand-written HTML for the 12 core components only; the rest ship as PNG renders.
- Generated tokens and PNGs are built, never committed.
- `:showcase` is a separate app module, not an `:app` flavor.

## Risks

- Compose resources (fonts, drawables) under Robolectric are unverified: step 4's spike.
- Robolectric gives zero status-bar insets, and the tab bar lives in `:orchestration`: screen renders lack both.
- `checkModuleGraph` skips untiered paths (`build.gradle.kts:702`); step 2 tiers `:showcase`.
