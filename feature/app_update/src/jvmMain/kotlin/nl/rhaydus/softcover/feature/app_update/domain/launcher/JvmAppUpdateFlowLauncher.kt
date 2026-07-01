package nl.rhaydus.softcover.feature.app_update.domain.launcher

import nl.rhaydus.softcover.feature.app_update.data.datasource.JvmAppUpdateDataSource

/**
 * Desktop [AppUpdateFlowLauncher] — starts the download of the pending installer. Mirrors the
 * Android launcher as a behavioural seam: it closes over the concrete [JvmAppUpdateDataSource] and
 * kicks off its background download, so the shared `StartAppUpdateFlowUseCase` stays platform-neutral
 * and the common `AppUpdateDataSource` interface never grows a desktop-only method.
 */
internal class JvmAppUpdateFlowLauncher(
    private val appUpdateDataSource: JvmAppUpdateDataSource,
) : AppUpdateFlowLauncher {
    override fun launch() {
        appUpdateDataSource.startDownload()
    }
}
