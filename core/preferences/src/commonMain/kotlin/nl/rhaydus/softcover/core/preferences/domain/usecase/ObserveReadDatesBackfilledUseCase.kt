package nl.rhaydus.softcover.core.preferences.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class ObserveReadDatesBackfilledUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<Boolean> = combine(
        settingsRepository.readDatesBackfilled,
        settingsRepository.authorDemographicsBackfilled,
    ) { datesBackfilled, demographicsBackfilled ->
        datesBackfilled && demographicsBackfilled
    }
}
