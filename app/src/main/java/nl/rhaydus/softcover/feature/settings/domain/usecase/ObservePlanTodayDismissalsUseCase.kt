package nl.rhaydus.softcover.feature.settings.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository

class ObservePlanTodayDismissalsUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<Map<Int, String>> = settingsRepository.dismissedPlanTodayByBook
}
