package nl.rhaydus.softcover.feature.app_update.domain.usecase

import nl.rhaydus.softcover.feature.app_update.domain.launcher.AppUpdateFlowLauncher

class StartAppUpdateFlowUseCase {
    operator fun invoke(launcher: AppUpdateFlowLauncher) {
        launcher.launch()
    }
}
