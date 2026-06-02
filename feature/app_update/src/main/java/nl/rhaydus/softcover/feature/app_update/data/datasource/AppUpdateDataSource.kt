package nl.rhaydus.softcover.feature.app_update.data.datasource

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import timber.log.Timber

interface AppUpdateDataSource {
    val updateState: Flow<AppUpdateState>

    suspend fun checkForUpdate()

    fun startUpdateFlow(launcher: ActivityResultLauncher<IntentSenderRequest>)

    fun completeUpdate()
}

internal class AppUpdateDataSourceImpl(private val appUpdateManager: AppUpdateManager) : AppUpdateDataSource {
    private val _updateState = MutableStateFlow<AppUpdateState>(AppUpdateState.Idle)
    override val updateState = _updateState.asStateFlow()

    private var cachedUpdateInfo: AppUpdateInfo? = null

    private val installListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> _updateState.value = AppUpdateState.Downloading
            InstallStatus.DOWNLOADED -> _updateState.value = AppUpdateState.Downloaded
            InstallStatus.FAILED -> _updateState.value = AppUpdateState.Failed
            InstallStatus.CANCELED -> _updateState.value = AppUpdateState.Idle
        }
    }

    init {
        appUpdateManager.registerListener(installListener)
    }

    override suspend fun checkForUpdate() {
        runCatching {
            appUpdateManager.requestAppUpdateInfo()
        }.onSuccess { info ->
            cachedUpdateInfo = info

            when {
                info.installStatus() == InstallStatus.DOWNLOADED -> {
                    _updateState.value = AppUpdateState.Downloaded
                }

                info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                    _updateState.value = AppUpdateState.Available
                }

                else -> {
                    _updateState.value = AppUpdateState.Idle
                }
            }
        }.onFailure {
            Timber.e(
                it,
                "Failed to check for app update",
            )

            _updateState.value = AppUpdateState.Idle
        }
    }

    override fun startUpdateFlow(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        val info = cachedUpdateInfo ?: return

        runCatching {
            appUpdateManager.startUpdateFlowForResult(
                info,
                launcher,
                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
            )
        }.onFailure {
            Timber.e(
                it,
                "Failed to start app update flow",
            )

            _updateState.value = AppUpdateState.Failed
        }
    }

    override fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    fun forceState(state: AppUpdateState) {
        _updateState.value = state
    }
}
