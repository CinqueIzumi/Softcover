package nl.rhaydus.softcover.core.domain.appupdate

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.AppUpdateState

interface ObserveAppUpdateStateUseCase {
    operator fun invoke(): Flow<AppUpdateState>
}
