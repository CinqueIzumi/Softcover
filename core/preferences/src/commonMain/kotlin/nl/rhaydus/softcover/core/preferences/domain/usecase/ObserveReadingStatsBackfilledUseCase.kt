package nl.rhaydus.softcover.core.preferences.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

/**
 * Emits `false` until a refresh covering every Read book has refilled author demographics, page counts and
 * each contributor's role.
 */
class ObserveReadingStatsBackfilledUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<Boolean> = combine(
        settingsRepository.authorDemographicsBackfilled,
        settingsRepository.authorRolesBackfilled,
    ) { demographicsBackfilled, rolesBackfilled ->
        demographicsBackfilled && rolesBackfilled
    }
}
