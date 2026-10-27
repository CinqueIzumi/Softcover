package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasYear
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearAtAGlanceHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearGrid
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

/**
 * The only writer of everything keyed by the atlas year: the chapter header and the day grid. Only a change
 * of year re-observes the days, so a scope change within the same year leaves the grid alone.
 */
internal class YearGridCollector : StatsAtlasCollector {
    override suspend fun onLaunch(
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
        dependencies: StatsAtlasDependencies,
    ) {
        combine(
            scope.state.map { it.selectedScope },
            dependencies.observeCurrentDateUseCase(),
        ) { selectedScope, today ->
            atlasYear(
                scope = selectedScope,
                today = today,
            )
        }
            .distinctUntilChanged()
            .flatMapLatest { year ->
                dependencies.observeDailyPagesReadUseCase(year)
            }
            .collect { daily ->
                scope.setState {
                    it.copy(
                        yearGrid = yearGrid(daily),
                        yearAtAGlanceHeader = yearAtAGlanceHeader(year = daily.year),
                    )
                }
            }
    }
}
