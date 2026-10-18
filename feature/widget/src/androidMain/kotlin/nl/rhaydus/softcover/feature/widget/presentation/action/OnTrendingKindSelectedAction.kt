package nl.rhaydus.softcover.feature.widget.presentation.action

import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeedKind
import nl.rhaydus.softcover.feature.widget.presentation.event.TrendingSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.mapper.trendingFeedChipsFor
import nl.rhaydus.softcover.feature.widget.presentation.mapper.trendingKindChipsFor
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.TrendingSettingsDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsUiState
import nl.rhaydus.toad.ActionScope

internal class OnTrendingKindSelectedAction(private val kind: TrendingFeedKind) : TrendingSettingsAction {
    override suspend fun execute(
        dependencies: TrendingSettingsDependencies,
        scope: ActionScope<TrendingSettingsUiState, TrendingSettingsEvent, TrendingSettingsLocalVariables>,
    ) {
        val feed = scope.currentLocalVariables.copy(kind = kind).feed

        scope.setLocalVariables { it.copy(kind = kind) }

        scope.setState {
            it.copy(
                kindChips = trendingKindChipsFor(selected = kind),
                periodChips = trendingFeedChipsFor(selected = feed),
            )
        }
    }
}
