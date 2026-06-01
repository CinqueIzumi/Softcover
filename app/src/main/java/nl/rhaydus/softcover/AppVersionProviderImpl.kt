package nl.rhaydus.softcover

import nl.rhaydus.softcover.core.domain.app.AppVersionInfo
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider

class AppVersionProviderImpl : AppVersionProvider {
    override val versionInfo: AppVersionInfo = AppVersionInfo(
        name = BuildConfig.VERSION_NAME,
        code = BuildConfig.VERSION_CODE,
    )
}
