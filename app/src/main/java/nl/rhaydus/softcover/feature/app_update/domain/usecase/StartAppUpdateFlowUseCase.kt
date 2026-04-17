package nl.rhaydus.softcover.feature.app_update.domain.usecase

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import nl.rhaydus.softcover.feature.app_update.domain.repository.AppUpdateRepository

class StartAppUpdateFlowUseCase(private val appUpdateRepository: AppUpdateRepository) {
    operator fun invoke(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        appUpdateRepository.startUpdateFlow(launcher = launcher)
    }
}
