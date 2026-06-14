package nl.rhaydus.softcover.orchestration

import nl.rhaydus.softcover.core.domain.app.AppVersionInfo
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider

/**
 * Desktop [AppVersionProvider] — reads the marketing version from the running jar's manifest
 * (`Implementation-Version`, set by the packaging task), the desktop analogue of Android's
 * `BuildConfig.VERSION_NAME`. Falls back to `0.0.0` when run from a raw classpath (e.g. `:run` in
 * development, where there is no jar manifest). The build number is not tracked on desktop.
 */
internal class JvmAppVersionProvider : AppVersionProvider {
    override val versionInfo: AppVersionInfo = AppVersionInfo(
        name = JvmAppVersionProvider::class.java.`package`?.implementationVersion ?: "0.0.0",
        code = 0,
    )
}
