package nl.rhaydus.softcover.feature.widget.settings.trending

import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.toad.ActionScope

internal class OnTrendingPeriodSelectedAction(private val period: TrendingPeriod) : TrendingSettingsAction {
    override suspend fun execute(
        dependencies: TrendingSettingsDependencies,
        scope: ActionScope<TrendingSettingsUiState, TrendingSettingsEvent, TrendingSettingsLocalVariables>,
    ) {
        scope.setLocalVariables { it.copy(period = period) }

        scope.setState { it.copy(periodChips = trendingPeriodChipsFor(selected = period)) }
    }
}
