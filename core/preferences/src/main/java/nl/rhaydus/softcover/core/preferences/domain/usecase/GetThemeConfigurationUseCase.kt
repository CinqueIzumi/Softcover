package nl.rhaydus.softcover.core.preferences.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class GetThemeConfigurationUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<ThemeConfiguration> {
        return settingsRepository.getThemeConfig()
    }
}