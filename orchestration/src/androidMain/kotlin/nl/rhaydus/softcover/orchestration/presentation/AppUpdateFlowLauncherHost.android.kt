package nl.rhaydus.softcover.orchestration.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.compose.getKoin
import org.koin.core.parameter.parametersOf
import nl.rhaydus.softcover.feature.app_update.domain.launcher.AppUpdateFlowLauncher

@Composable
internal actual fun rememberAppUpdateFlowLauncher(): AppUpdateFlowLauncher {
    val koin = getKoin()

    val updateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { }

    return remember(updateLauncher) {
        koin.get<AppUpdateFlowLauncher> { parametersOf(updateLauncher) }
    }
}
