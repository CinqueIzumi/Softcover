---
paths:
  - "**/build.gradle.kts"
  - "build-logic/**"
  - "gradle/**"
  - "settings.gradle.kts"
---

# Build wiring

- All versions live in `gradle/libs.versions.toml`; build files reference them as `libs.<alias>`, never a
  literal coordinate or version.
- A module shipping Compose Multiplatform resources sets `androidResources.enable = true` in its own
  `androidLibrary { }` block; gated by `checkResourcePackaging` (wired into `check`).
- Presentation and `:core:component` main sources stay at or under 600 lines. Gated by
  `./gradlew checkFileSize` (wired into `check`), with no baseline.
- Apply the smallest set of `rhaydus.*` / `softcover.*` convention plugins and do not re-declare what they
  provide; the roster and the rules are in `docs/reference/module-structure.md` § Build wiring conventions.
- `ktlintCheck`, `ktlintFormat` and `checkDocBudgets` are root-only tasks, so `:<module>:ktlintCheck` does not
  exist. Run the bare task, which always scans the whole repo (`-Pktlint.root=<dir>` narrows ktlint).
  `detekt*` and compile tasks are per-module.
- After changing a foundation `detekt-rules` rule, run `./gradlew --stop`, then detekt with `--rerun-tasks`;
  otherwise detekt runs the old rule jar, from the daemon or the build cache.
- A `SpinePalette` entry added without its `ColorPalette` compiles clean and stays unselectable; only
  `SpinePaletteMapperTest`'s bijection test (`:core:uibinding`) catches it.
- `stabilityCheck` (in `check` and `styleCheck`) compares each Compose module with `<module>/stability/`, for
  `androidMain` only. Any composable change fails it; refresh with `./gradlew stabilityDump` and commit both.
- Only `:showcase:renderShowcase` runs the render runner (class filter elsewhere). Robolectric needs JUnit 4
  (vintage engine), `@GraphicsMode(NATIVE)` (else debug boxes), `sdk = [35]` (36 needs Java 21), plain
  `Application` (the Koin one throws on the second test) and `ComponentActivity` registered by hand.
- Design preset tasks: `:core:designsystem:exportDesignTokens` / `checkDesignPresetContent` / `testDesignPreset`
  and root `designPresetBundle` (fails above 1,008 files). Never name one `assemble*` / `publish*`.
- The `nl.rhaydus:*` catalog entries are version-less substitution coordinates for the foundation `includeBuild`;
  every CI checkout that runs Gradle sets `submodules: true`.
- `build-logic/` is named `softcover-build-logic`: sharing the foundation's `build-logic` name sends Softcover's
  plugin ids to the foundation build, which fails with "does not provide a plugin".
- The foundation and Softcover must build on the same AGP and Kotlin versions, because Gradle refuses two AGP
  versions in one build; bump both together.
- dependency-analysis cannot credit a `commonMain` declaration of an included-build KMP library, which is why
  the `nl.rhaydus` coordinates are excluded from unused-dependency advice in the root `build.gradle.kts`.
