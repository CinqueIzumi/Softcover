---
name: increment-version-code
description: Increment the Android app's versionCode in app/build.gradle.kts by 1.
---

Increment the `versionCode` in `app/build.gradle.kts` by exactly 1.

Steps:

1. Read `app/build.gradle.kts` and locate the `versionCode = <N>` line inside `defaultConfig`.
2. Use the Edit tool to replace `versionCode = <N>` with `versionCode = <N + 1>`.
3. Report the old and new value back to the user in a single short sentence (e.g. `versionCode: 12 → 13`).

Do not modify anything else. Do not create a commit. Do not touch `versionName`.
