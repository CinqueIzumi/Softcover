package nl.rhaydus.softcover.core.preferences.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class ObservePlanTodayDismissalsUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<Map<Int, String>> = settingsRepository.dismissedPlanTodayByBook
}
