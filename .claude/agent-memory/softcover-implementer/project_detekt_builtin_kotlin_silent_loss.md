---
name: detekt-builtin-kotlin-silent-loss
description: detekt 1.23.8 registers no :app per-variant tasks under AGP built-in Kotlin; we register them by hand in root build.gradle.kts
metadata:
  type: project
---

detekt's Android integration keys off the legacy `kotlin-android` plugin id and BaseExtension, so dropping that plugin makes `detektMain/Debug/Release` for `:app` vanish silently (the `check` gate just matches nothing). Root `build.gradle.kts` registers `detekt<Variant>` itself (classpath from `compile<Variant>Kotlin` libraries + sdk bootClasspath) and a lifecycle `detektMain`. detekt's `registerDetektTask` is Kotlin-internal, not callable from scripts.

**How to apply:** after any AGP/Kotlin/detekt change, diff `:app:tasks --all` detekt names and run a canary violation (unguarded `Flow.first()` trips UnguardedFlowTerminalRead).
