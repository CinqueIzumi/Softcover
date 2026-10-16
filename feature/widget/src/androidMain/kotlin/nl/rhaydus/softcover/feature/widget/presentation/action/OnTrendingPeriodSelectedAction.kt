package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.feature.widget.presentation.event.TrendingSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.mapper.trendingPeriodChipsFor
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.TrendingSettingsDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsUiState
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
