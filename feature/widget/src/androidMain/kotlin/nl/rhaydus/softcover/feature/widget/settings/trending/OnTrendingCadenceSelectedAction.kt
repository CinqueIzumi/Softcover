package nl.rhaydus.softcover.feature.widget.settings.trending

import nl.rhaydus.softcover.feature.widget.trending.TrendingRefreshCadence
import nl.rhaydus.toad.ActionScope

internal class OnTrendingCadenceSelectedAction(private val cadence: TrendingRefreshCadence) : TrendingSettingsAction {
    override suspend fun execute(
        dependencies: TrendingSettingsDependencies,
        scope: ActionScope<TrendingSettingsUiState, TrendingSettingsEvent, TrendingSettingsLocalVariables>,
    ) {
        scope.setLocalVariables { it.copy(cadence = cadence) }

        scope.setState { it.copy(cadenceChips = trendingCadenceChipsFor(selected = cadence)) }
    }
}
