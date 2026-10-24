package nl.rhaydus.softcover.core.preferences.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

/** Emits `false` until a refresh covering every Read book has refilled author demographics and page counts. */
class ObserveAuthorDemographicsBackfilledUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<Boolean> = settingsRepository.authorDemographicsBackfilled
}
