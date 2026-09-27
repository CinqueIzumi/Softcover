---
name: architecture_gradle_main_suffix_case_sensitive
description: A root Gradle gate that scans src/*Main directories by name (checkPresentationFileSize, and any future sibling) must use an exact-case "Main" suffix match, which silently exempts classic `src/main` (lowercase) modules like :app/:desktopApp.
metadata:
  type: architecture
---

`checkPresentationFileSize` (root `build.gradle.kts`) discovers source sets with
`dir.name.endsWith("Main")` (capital M). That correctly matches every KMP source set
(`commonMain`, `androidMain`, `jvmMain`, `iosMain`, `mobileMain`, …) and, just as importantly, is
case-sensitive enough that a classic Android/JVM module's `src/main` (lowercase) is invisible to the
scan — verified live: `:app` and `:desktopApp` use plain `src/main` and are skipped entirely, gate or no
gate. This is currently harmless only because neither module has a `presentation` package. Confirm the
same before trusting a case-sensitive `*Main` filter on any future root-level scan task modelled on this
one: check whether every module the gate is meant to cover actually uses KMP-style source-set naming, not
just that the regex/suffix "looks right" for the modules you spot-checked. If a future PR adds a
`presentation` package under a plain `src/main` module, this class of gate will not catch it — that would
be the real 🔴/🟡 finding, not the filter logic itself.
