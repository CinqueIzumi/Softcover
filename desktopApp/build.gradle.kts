import org.jetbrains.compose.desktop.application.dsl.TargetFormat

// Desktop (JVM) application shell — the desktop sibling of the Android `:app` and the iOS Xcode
// project. It owns nothing but the window + `main`; the whole app (UI, DI, data) comes from
// `:orchestration`, exactly as `:app` and `iosApp` do. A plain Kotlin/JVM module (not KMP): it is a
// single-platform executable, so it applies the JetBrains Compose plugin for `compose.desktop`.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

dependencies {
    // Orchestration tier (composes every feature + core module and hosts DesktopApp()).
    implementation(project(":orchestration"))

    // The logging facade the entry point installs (AppLog.install, mirrors Android's SoftCoverApp)
    // now lives in the foundation core-common module.
    implementation(libs.rhaydus.coreCommon)

    implementation(compose.desktop.currentOs)

    // Provides Dispatchers.Main on desktop (the Swing/AWT event thread) — the desktop counterpart of
    // kotlinx-coroutines-android. Compose's runtime and the lifecycle ViewModel scope dispatch onto it.
    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "nl.rhaydus.softcover.desktop.MainKt"

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Deb,
            )
            packageName = "Softcover"
            // Desktop release version — the marketing version (mirrors Android versionName /
            // iOS MARKETING_VERSION). Kept in sync by the set-version-name / release skills. Desktop
            // has no separate build-number field, so versionCode has no desktop counterpart.
            packageVersion = "3.0.2"
            description = "Softcover — a Hardcover.app book tracking client"
            // Maintainer/vendor for the package metadata (jpackage Deb/Msi); without it the Linux
            // package falls back to "Unknown". This is package metadata, not the runtime window name
            // (that fix lives in Main.kt's awt.application.name).
            vendor = "Rhaydus"

            // Required for KSafe's OS-backed key custody in a trimmed release distributable: the
            // native secret-store access needs sun.misc.Unsafe (jdk.unsupported); without these the
            // packaged app silently drops to KSafe's software-key fallback tier.
            // jlink strips every module not listed here from the bundled runtime, so a missing entry
            // surfaces as a launch-time NoClassDefFoundError in the packaged app only — a `:run`
            // launch uses the full local JDK and never reproduces it. Keep this reconciled with
            // `./gradlew :desktopApp:suggestRuntimeModules` (jdeps) whenever a dependency changes.
            modules(
                "jdk.unsupported",
                "java.management",
                // Flagged as required by jdeps (`suggestRuntimeModules`); a transitive dependency in
                // the runtime classpath references the JVM instrumentation API.
                "java.instrument",
                // The desktop self-updater (GitHubReleaseSource) talks to GitHub over the JDK's
                // java.net.http client; without this the packaged app died at launch with a
                // NoClassDefFoundError for java/net/http/HttpClient.
                "java.net.http",
            )

            // App icons. macOS gets the rounded/padded native variant; Windows + Linux use the flat
            // square. All three are generated from the iOS 1024 source by icons/generate-icons.py.
            macOS {
                iconFile.set(project.file("icons/softcover.icns"))
                bundleID = "nl.rhaydus.softcover"
                dockName = "Softcover"
            }

            windows {
                iconFile.set(project.file("icons/softcover.ico"))
                menuGroup = "Softcover"
                // Stable across releases so the Msi upgrades in place instead of installing
                // side-by-side. Generated once via uuidgen; never change it.
                upgradeUuid = "F690243B-E4DF-41A0-9027-8D2CD0E8B619"
            }

            linux {
                iconFile.set(project.file("src/main/resources/softcover.png"))
            }
        }

        // Bake the marketing version in as a system property so the app reports it at runtime
        // (JvmAppVersionProvider reads "softcover.appVersion"). application.jvmArgs reach BOTH the
        // packaged launcher and a `./gradlew :run` launch (which has no jar manifest to read), so a
        // dev run reports the real version and the auto-updater's version check behaves like a release
        // build. Single-sourced from packageVersion above, so set-version-name only touches that literal.
        // (No -Xdock:name here: a bare-JVM `:run` fork shows "java" in the macOS Dock and does not honor
        // -Xdock:name; the packaged app takes its name from macOS.dockName below, which is the only
        // reliable source. The `:run` Dock label is a dev-only cosmetic.)
        jvmArgs += "-Dsoftcover.appVersion=${nativeDistributions.packageVersion}"
    }
}
