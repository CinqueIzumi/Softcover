---
name: set-version-name
description: Set the app's version name to the given value on all platforms — Android versionName in app/build.gradle.kts, iOS MARKETING_VERSION in iosApp/iosApp.xcodeproj/project.pbxproj, and desktop packageVersion in desktopApp/build.gradle.kts — and bump the build number by 1.
---

Set the version name to the value the user provided as an argument on Android, iOS, and desktop, then bump the build number by invoking the `/increment-version-code` skill.

Steps:

1. Read the argument (the new version name, e.g. `1.3.0`). If no argument was given, ask the user for it and stop.
2. Read `app/build.gradle.kts` and locate the `versionName = "<old>"` line inside `defaultConfig`.
3. Use the Edit tool to replace `versionName = "<old>"` with `versionName = "<new>"`.
4. Set the iOS version name to the same `<new>`. In `iosApp/iosApp.xcodeproj/project.pbxproj` the setting `MARKETING_VERSION = <old>;` appears twice (the Debug and Release build configs) and both must stay matched. Use the Edit tool with `replace_all: true` to replace `MARKETING_VERSION = <old>;` with `MARKETING_VERSION = <new>;`. If the iOS value did not already equal the old Android `<old>`, do not silently overwrite it — stop and report the mismatch so the user can decide.
5. Set the desktop version name to the same `<new>`. In `desktopApp/build.gradle.kts` the `packageVersion = "<old>"` line lives inside the `nativeDistributions` block; use the Edit tool to replace it with `packageVersion = "<new>"`. This is the desktop installer's marketing version (the analogue of `versionName` / `MARKETING_VERSION`). If the desktop value did not already equal the old `<old>`, do not silently overwrite it — stop and report the mismatch so the user can decide. Desktop has **no separate build number**, so there is no `versionCode` counterpart to bump here.
6. Invoke the `increment-version-code` skill via the Skill tool to bump the build number by 1 (Android + iOS). Do not edit `versionCode` / `CURRENT_PROJECT_VERSION` yourself — always delegate to that skill so the logic stays in one place.
7. Report the changes back to the user in a single short sentence, noting all platforms (e.g. `versionName / MARKETING_VERSION / packageVersion: 1.2.1 → 1.3.0, versionCode / CURRENT_PROJECT_VERSION: 12 → 13`).

Do not create a commit. Do not modify anything other than the version name (and the build number via the delegated skill) on the three platforms.
