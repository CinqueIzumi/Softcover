---
name: increment-version-code
description: Increment the app's build number by 1 on both platforms — Android versionCode in app/build.gradle.kts and iOS CURRENT_PROJECT_VERSION in iosApp/iosApp.xcodeproj/project.pbxproj.
---

Increment the build number by exactly 1 on both platforms, keeping Android and iOS in sync.

Steps:

1. Read `app/build.gradle.kts` and locate the `versionCode = <N>` line inside `defaultConfig`.
2. Use the Edit tool to replace `versionCode = <N>` with `versionCode = <N + 1>`.
3. Update the iOS build number to the same `<N + 1>`. In `iosApp/iosApp.xcodeproj/project.pbxproj` the setting `CURRENT_PROJECT_VERSION = <N>;` appears twice (the Debug and Release build configs) and both must stay matched. Use the Edit tool with `replace_all: true` to replace `CURRENT_PROJECT_VERSION = <N>;` with `CURRENT_PROJECT_VERSION = <N + 1>;`. If the iOS value did not already equal the old Android `<N>`, do not silently overwrite it — stop and report the mismatch so the user can decide.
4. Report the old and new value back to the user in a single short sentence, noting both platforms were bumped (e.g. `versionCode / CURRENT_PROJECT_VERSION: 12 → 13`).

Do not modify anything else. Do not create a commit. Do not touch `versionName` / `MARKETING_VERSION`.
