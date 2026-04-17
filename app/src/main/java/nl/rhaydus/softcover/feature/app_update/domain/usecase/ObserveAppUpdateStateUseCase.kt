package nl.rhaydus.softcover.feature.app_update.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.app_update.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.app_update.domain.repository.AppUpdateRepository

class ObserveAppUpdateStateUseCase(private val appUpdateRepository: AppUpdateRepository) {
    operator fun invoke(): Flow<AppUpdateState> = appUpdateRepository.updateState
}
