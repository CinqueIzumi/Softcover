package nl.rhaydus.softcover.orchestration

import nl.rhaydus.softcover.core.domain.app.AppVersionInfo
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider

/**
 * Desktop [AppVersionProvider] — the desktop analogue of Android's `BuildConfig.VERSION_NAME`. The
 * marketing version is passed in as the `softcover.appVersion` system property, which
 * `desktopApp/build.gradle.kts` derives from `packageVersion` and injects both into the packaged
 * launcher and into the `:run` JavaExec — so a dev run reports the same version as a release build.
 * Falls back to the running jar's `Implementation-Version` manifest attribute, then `0.0.0` (a raw
 * classpath with neither). The build number is not tracked on desktop.
 */
internal class JvmAppVersionProvider : AppVersionProvider {
    override val versionInfo: AppVersionInfo = AppVersionInfo(
        name = resolveVersionName(),
        code = 0,
    )

    private fun resolveVersionName(): String {
        return System.getProperty(VERSION_PROPERTY)
            ?: JvmAppVersionProvider::class.java.`package`?.implementationVersion
            ?: FALLBACK_VERSION
    }

    private companion object {
        const val VERSION_PROPERTY = "softcover.appVersion"
        const val FALLBACK_VERSION = "0.0.0"
    }
}
