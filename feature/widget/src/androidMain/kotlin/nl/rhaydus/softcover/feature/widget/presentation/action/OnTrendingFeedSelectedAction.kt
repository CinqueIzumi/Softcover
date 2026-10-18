package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.presentation.event.TrendingSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.mapper.trendingFeedChipsFor
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.TrendingSettingsDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsUiState
import nl.rhaydus.softcover.feature.widget.presentation.state.withFeed
import nl.rhaydus.toad.ActionScope

internal class OnTrendingFeedSelectedAction(private val feed: TrendingFeed) : TrendingSettingsAction {
    override suspend fun execute(
        dependencies: TrendingSettingsDependencies,
        scope: ActionScope<TrendingSettingsUiState, TrendingSettingsEvent, TrendingSettingsLocalVariables>,
    ) {
        scope.setLocalVariables { it.withFeed(feed = feed) }

        scope.setState { it.copy(periodChips = trendingFeedChipsFor(selected = feed)) }
    }
}
