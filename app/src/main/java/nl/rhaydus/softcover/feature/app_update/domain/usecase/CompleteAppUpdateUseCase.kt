package nl.rhaydus.softcover.feature.app_update.domain.usecase

import nl.rhaydus.softcover.feature.app_update.domain.repository.AppUpdateRepository

class CompleteAppUpdateUseCase(private val appUpdateRepository: AppUpdateRepository) {
    operator fun invoke() {
        appUpdateRepository.completeUpdate()
    }
}
