---
name: project_designsystem_palette_contrast_tests
description: How SoftcoverColorSchemeTest data-drives WCAG contrast checks across ColorPalette entries in core/designsystem
metadata:
  type: project
---

`core/designsystem/src/androidHostTest/kotlin/.../presentation/theme/SoftcoverColorSchemeTest.kt` guards
`softcoverColorScheme(darkTheme, colorPalette)` (Theme.kt): fixed hex assertions for the SOFTCOVER
default (regression net against future refactors), a WCAG contrast-floor sweep, and a distinctness
check across `ColorPalette.entries`.

- No existing luminance/contrast helper in the repo — wrote a private one in the test file (sRGB
  gamma-decode per WCAG, `(hi + 0.05) / (lo + 0.05)`), reading channels via `Color.red/green/blue`
  (0f..1f floats).
- Used JUnit 5 `@TestFactory` + `DynamicTest.dynamicTest(name) { }` to iterate `ColorPalette.entries x
  listOf(false, true)` so a newly added palette/brightness is covered automatically without editing the
  test. `io.kotest.assertions.withClue { }` wraps each ratio assertion so a failure names the palette,
  brightness, and the specific foreground/background pair.
- Confirmed this project's import convention is flat single-block alphabetical (not grouped by
  java/kotlin/nl.rhaydus) — see `BooksRepositoryImplTest.kt` for a multi-group example: `io.kotest.*`,
  `io.mockk.*`, `kotlin.*`, `kotlinx.*`, `nl.rhaydus.*`, `org.junit.*` all interleaved into one
  alphabetical run, no blank-line grouping.
- `io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual` exists in kotest-assertions-core 5.9.1 (jvm),
  confirmed via the sources jar in the Gradle cache.
