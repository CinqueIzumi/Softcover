package nl.rhaydus.softcover.feature.widget

import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase

internal class SoftcoverWidgetDependencies(
    val getThemeConfiguration: GetThemeConfigurationUseCase,
)
