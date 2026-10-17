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
- A module that ships Compose Multiplatform resources sets `androidResources.enable = true` in its own
  `androidLibrary { }` block (see `core/designsystem/build.gradle.kts`). The convention plugins deliberately
  leave it to the module build file. Gated by `./gradlew checkResourcePackaging` (wired into `check`).
- Presentation and `:core:component` main sources stay at or under 600 lines. Gated by
  `./gradlew checkPresentationFileSize` (wired into `check`), with no baseline.
- Apply the smallest set of `softcover.*` convention plugins and do not re-declare what they provide; the
  roster and the rules are in `docs/reference/module-structure.md` § Build wiring conventions.
- `ktlintCheck`, `ktlintFormat` and `checkDocBudgets` are root-only tasks, so `:<module>:ktlintCheck` does not
  exist. Run the bare task, which always scans the whole repo (`-Pktlint.root=<dir>` narrows ktlint).
  `detekt*` and compile tasks are per-module.
- After changing a `:detekt-rules` rule, run `./gradlew --stop` and then the detekt tasks with
  `--rerun-tasks`. Otherwise detekt stays UP-TO-DATE and runs the old rule jar. With the build cache on, a
  stale-daemon result is also stored in the local build cache; `--rerun-tasks` bypasses cache reads, so the
  recipe holds.
- A `SpinePalette` entry added without its `ColorPalette` compiles clean and stays unselectable; only
  `SpinePaletteMapperTest`'s bijection test (`:core:uibinding`) catches it.
- `stabilityCheck` (part of `check` and `styleCheck`) compares every Compose module with its baseline in
  `<module>/stability/`, for the `androidMain` target only, so composables in `jvmMain` or `iosMain` are
  never checked. Any change to a composable fails it, even a new one; refresh the baselines with
  `./gradlew stabilityDump` and commit them with the change.
