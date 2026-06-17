package nl.rhaydus.softcover.orchestration

import platform.Foundation.NSBundle
import nl.rhaydus.softcover.core.domain.app.AppVersionInfo
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider

/**
 * iOS [AppVersionProvider] — reads the app bundle's `CFBundleShortVersionString` (marketing version,
 * e.g. "1.4.0") and `CFBundleVersion` (build number) from `Info.plist`, the iOS analogue of Android's
 * `BuildConfig.VERSION_NAME` / `VERSION_CODE`.
 */
internal class IosAppVersionProvider : AppVersionProvider {
    override val versionInfo: AppVersionInfo = run {
        val info = NSBundle.mainBundle.infoDictionary
        AppVersionInfo(
            name = info?.get("CFBundleShortVersionString") as? String ?: "0.0.0",
            code = (info?.get("CFBundleVersion") as? String)?.toIntOrNull() ?: 0,
        )
    }
}
