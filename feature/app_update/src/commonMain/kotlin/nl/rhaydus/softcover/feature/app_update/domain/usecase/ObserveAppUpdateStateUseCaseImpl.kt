package nl.rhaydus.softcover.feature.app_update.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.appupdate.ObserveAppUpdateStateUseCase
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.app_update.domain.repository.AppUpdateRepository

class ObserveAppUpdateStateUseCaseImpl(private val appUpdateRepository: AppUpdateRepository) : ObserveAppUpdateStateUseCase {
    override operator fun invoke(): Flow<AppUpdateState> = appUpdateRepository.updateState
}
