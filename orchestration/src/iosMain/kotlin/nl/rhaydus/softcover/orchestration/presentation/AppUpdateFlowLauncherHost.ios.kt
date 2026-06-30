package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.feature.app_update.domain.launcher.AppUpdateFlowLauncher

// iOS updates are an App Store concern — there is no in-app update flow. The observed update state is
// always Idle on iOS, so App() never actually invokes this; bind a logged no-op so the shared wiring
// still has a launcher to hold.
@Composable
internal actual fun rememberAppUpdateFlowLauncher(): AppUpdateFlowLauncher =
    remember {
        AppUpdateFlowLauncher {
            AppLog.i("In-app update flow is not available on this platform.")
        }
    }
