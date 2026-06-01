package nl.rhaydus.softcover.core.preferences.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class GetDateStyleAsFlowUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<DateStyle> = settingsRepository.dateStyle
}