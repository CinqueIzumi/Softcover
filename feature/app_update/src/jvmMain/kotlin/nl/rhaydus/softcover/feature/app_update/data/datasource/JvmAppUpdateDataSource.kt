package nl.rhaydus.softcover.feature.app_update.data.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.system.exitProcess
import nl.rhaydus.softcover.core.domain.app.AppVersionProvider
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.app_update.data.install.DesktopInstallerLauncher
import nl.rhaydus.softcover.feature.app_update.data.model.AppRelease
import nl.rhaydus.softcover.feature.app_update.data.release.ReleaseSource
import nl.rhaydus.softcover.feature.app_update.data.version.VersionComparator
import nl.rhaydus.ui.common.AppDispatchers

/**
 * Desktop self-updater — the `AppUpdateDataSource` seam Android backs with Play. The desktop
 * distribution ships as jpackage installers, so this drives an *assisted* update: check the latest
 * release, download its installer, and — on the user's confirmation — launch it and exit so it can
 * replace the running app.
 *
 * It maps onto the shared [AppUpdateState] machine so the common repository, use cases, and
 * snackbar/settings UI light up unchanged: [checkForUpdate] → `Available`, [startDownload] →
 * `Downloading` → `Downloaded`, [completeUpdate] launches the installer. Any failure resolves to
 * `Failed` (download/launch) or `Idle` (check). The release provider is reached through
 * [AppUpdateRemoteDataSource], so nothing here is GitHub-specific.
 */
internal class JvmAppUpdateDataSource(
    private val appVersionProvider: AppVersionProvider,
    private val releaseSource: ReleaseSource,
    private val installerLauncher: DesktopInstallerLauncher,
    private val appDispatchers: AppDispatchers,
    private val appDataDirectory: String,
) : AppUpdateDataSource, AutoCloseable {
    private val scope = CoroutineScope(SupervisorJob() + appDispatchers.io)

    private val _updateState = MutableStateFlow<AppUpdateState>(AppUpdateState.Idle)
    override val updateState = _updateState.asStateFlow()

    private var pendingRelease: AppRelease? = null
    private var downloadedInstaller: File? = null

    // Cancels the update state-machine scope so the desktop shutdown path leaves no updater work
    // running. The release provider's HTTP client is left for `exitProcess(0)` to reclaim — its
    // threads are daemon and closing it can block on an in-flight download (see GitHubReleaseSource).
    override fun close() {
        scope.cancel()
    }

    override suspend fun checkForUpdate() {
        val release = releaseSource.fetchLatestRelease()

        val isNewer = release != null &&
            VersionComparator.isNewer(
                candidate = release.version,
                current = appVersionProvider.versionInfo.name,
            )

        if (isNewer) {
            pendingRelease = release
            _updateState.value = AppUpdateState.Available
        } else {
            _updateState.value = AppUpdateState.Idle
        }
    }

    /**
     * Downloads the installer resolved by [checkForUpdate] in the background, moving the state to
     * `Downloading` then `Downloaded` (or `Failed`). Driven by the desktop `AppUpdateFlowLauncher`
     * when the user accepts the update; a no-op if nothing is pending or a download is already
     * underway.
     */
    fun startDownload() {
        val release = pendingRelease ?: return

        if (_updateState.value == AppUpdateState.Downloading) return

        _updateState.value = AppUpdateState.Downloading

        scope.launch {
            val installer = releaseSource.downloadInstaller(
                release = release,
                destinationDirectory = appDataDirectory,
            )

            if (installer != null) {
                downloadedInstaller = installer
                _updateState.value = AppUpdateState.Downloaded
            } else {
                AppLog.e("Failed to download the desktop update installer.")

                _updateState.value = AppUpdateState.Failed
            }
        }
    }

    override fun completeUpdate() {
        val installer = downloadedInstaller ?: return

        if (installerLauncher.launch(installer)) {
            exitProcess(0)
        } else {
            AppLog.e("Failed to launch the downloaded desktop update installer.")

            _updateState.value = AppUpdateState.Failed
        }
    }
}
