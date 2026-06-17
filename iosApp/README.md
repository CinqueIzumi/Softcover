# iosApp — Softcover iOS shell

This directory holds the **Swift side** of the iOS app. The whole UI and all logic come from the
shared Kotlin/Compose-Multiplatform code, packaged as the **`OrchestrationKit`** framework that
`:orchestration` produces. The Swift files (`iosApp/iOSApp.swift`, `iosApp/ContentView.swift`) are the
thin host.

Layout (flattened — the Xcode project lives directly under `iosApp/`):

```
iosApp/
  iosApp.xcodeproj/        # the Xcode project (framework already wired in)
  iosApp/                  # the app target sources
    iOSApp.swift           # @main; calls InitKoinIosKt.doInitKoinIos()
    ContentView.swift      # hosts MainViewControllerKt.MainViewController()
    Assets.xcassets/
  README.md
```

## What's already wired (Kotlin + Xcode)

- `:orchestration` exports a **static** iOS framework `OrchestrationKit` (`iosArm64` +
  `iosSimulatorArm64`) — see `orchestration/build.gradle.kts`.
- Entry symbols (confirmed in the generated header):
  - `InitKoinIosKt.doInitKoinIos()` — starts Koin (called once in `iOSApp.init()`).
  - `MainViewControllerKt.MainViewController()` — returns the Compose `UIViewController`.
- Cover images load over Ktor's Darwin engine (Coil's iOS network fetcher).
- The Xcode project (`project.pbxproj`) already has, in the `iosApp` target:
  - a **Run Script** build phase (before Compile Sources): `cd "$SRCROOT/.." && ./gradlew
    :orchestration:embedAndSignAppleFrameworkForXcode`;
  - `FRAMEWORK_SEARCH_PATHS = $(SRCROOT)/../orchestration/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`;
  - `OTHER_LDFLAGS = -framework OrchestrationKit` (a static framework isn't auto-linked from the search path);
  - `ENABLE_USER_SCRIPT_SANDBOXING = NO` (the Gradle script writes outside the sandbox);
  - deployment target 15.0, `MARKETING_VERSION` 2.4.0, `CURRENT_PROJECT_VERSION` 29.

## What's left for you (Xcode)

1. **Signing**: open the project → target `iosApp` → *Signing & Capabilities* → pick your Team
   (the bundle id is `nl.rhaydus.softcover.iosApp`; change if you like).
2. **One-time machine setup** — `xcode-select` here points at the Command Line Tools, but Kotlin/
   Native framework *linking* needs full Xcode. Xcode.app itself uses the full toolchain, so opening
   and running from Xcode just works; this only bites command-line Gradle runs. To fix permanently:
   ```bash
   sudo xcode-select -s /Applications/Xcode.app/Contents/Developer
   ```
3. **Run**: select the `iosApp` scheme + a simulator and **⌘R**. The first build runs Gradle to
   produce the framework (slow); subsequent builds are incremental.

## Capabilities (nothing needed today)

- **Notifications** (reading-session reminders): the shared `core:notification` iOS code uses
  `UNUserNotificationCenter` with a runtime authorization prompt. *Local* notifications need **no
  Info.plist key and no entitlement**. Add the **Push Notifications** capability only if remote push
  is introduced later.
- **Camera / Photo library**: the iOS barcode scanner and the share-card "save to gallery" are
  graceful-degradation stubs today (the camera is never opened; sharing uses the system share sheet,
  which needs no key). Add `INFOPLIST_KEY_NSCameraUsageDescription` /
  `INFOPLIST_KEY_NSPhotoLibraryAddUsageDescription` build settings only when a real iOS scanner or
  Photos-save lands. (The project uses `GENERATE_INFOPLIST_FILE = YES` — there is no physical
  Info.plist; add privacy strings as `INFOPLIST_KEY_*` build settings, not a file.)

## Keeping versions in sync

The Xcode build settings `MARKETING_VERSION` (2.4.0) and `CURRENT_PROJECT_VERSION` (29) become
`CFBundleShortVersionString` / `CFBundleVersion`, which `IosAppVersionProvider` reads back into the
shared `AppVersionProvider`. Update both when you bump `app/build.gradle.kts` `versionName` /
`versionCode`.
