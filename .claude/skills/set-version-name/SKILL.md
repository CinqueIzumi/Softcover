---
name: set-version-name
description: Set the Android app's versionName in app/build.gradle.kts to the given value and bump versionCode by 1.
---

Set `versionName` in `app/build.gradle.kts` to the value the user provided as an argument, then bump `versionCode` by invoking the `/increment-version-code` skill.

Steps:

1. Read the argument (the new version name, e.g. `1.3.0`). If no argument was given, ask the user for it and stop.
2. Read `app/build.gradle.kts` and locate the `versionName = "<old>"` line inside `defaultConfig`.
3. Use the Edit tool to replace `versionName = "<old>"` with `versionName = "<new>"`.
4. Invoke the `increment-version-code` skill via the Skill tool to bump `versionCode` by 1. Do not edit `versionCode` yourself — always delegate to that skill so the logic stays in one place.
5. Report both changes back to the user in a single short sentence (e.g. `versionName: 1.2.1 → 1.3.0, versionCode: 12 → 13`).

Do not create a commit. Do not modify anything other than `versionName` (and `versionCode` via the delegated skill).
