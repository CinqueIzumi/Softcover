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
- Apply the smallest set of `softcover.*` convention plugins and do not re-declare what they provide; the
  roster and the rules are in `docs/reference/module-structure.md` § Build wiring conventions.
- `ktlintCheck`, `ktlintFormat` and `checkDocBudgets` are root-only tasks, so `:<module>:ktlintCheck` does not
  exist. Run the bare task, which always scans the whole repo (`-Pktlint.root=<dir>` narrows ktlint).
  `detekt*` and compile tasks are per-module.
