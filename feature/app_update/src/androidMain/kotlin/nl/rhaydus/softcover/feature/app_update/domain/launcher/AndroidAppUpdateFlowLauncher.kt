package nl.rhaydus.softcover.feature.app_update.domain.launcher

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import nl.rhaydus.common.AppLog

/**
 * Android [AppUpdateFlowLauncher] — re-requests the current [com.google.android.play.core.appupdate.AppUpdateInfo]
 * and hands it to Play's `startUpdateFlowForResult` together with the activity's
 * [ActivityResultLauncher]. Re-fetching (rather than reusing a cached info) is Play's documented
 * pattern and keeps the launcher independent of the data source's lifecycle, so the Compose-scoped
 * [activityResultLauncher] never has to reach a DI-scoped singleton.
 */
internal class AndroidAppUpdateFlowLauncher(
    private val appUpdateManager: AppUpdateManager,
    private val activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
) : AppUpdateFlowLauncher {
    override fun launch() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                runCatching {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        activityResultLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                    )
                }.onFailure {
                    AppLog.e(
                        it,
                        "Failed to start app update flow",
                    )
                }
            }
            .addOnFailureListener {
                AppLog.e(
                    it,
                    "Failed to fetch app update info for flow",
                )
            }
    }
}
