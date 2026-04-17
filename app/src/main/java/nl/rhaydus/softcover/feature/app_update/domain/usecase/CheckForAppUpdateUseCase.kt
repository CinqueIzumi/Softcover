package nl.rhaydus.softcover.feature.app_update.domain.usecase

import nl.rhaydus.softcover.feature.app_update.domain.repository.AppUpdateRepository

class CheckForAppUpdateUseCase(private val appUpdateRepository: AppUpdateRepository) {
    suspend operator fun invoke() {
        appUpdateRepository.checkForUpdate()
    }
}
