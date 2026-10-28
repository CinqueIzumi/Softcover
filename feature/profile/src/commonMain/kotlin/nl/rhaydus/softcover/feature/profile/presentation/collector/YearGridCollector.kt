package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasYear
import nl.rhaydus.softcover.feature.profile.presentation.mapper.currentStreakTile
import nl.rhaydus.softcover.feature.profile.presentation.mapper.longestStreakTile
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearAtAGlanceHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearGrid
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasStreaks
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

/**
 * The only writer of everything keyed by the atlas year: the chapter header, the day grid, the streak tiles and
 * the streaks the day sheet reads. Only a change of year re-keys the grid, so a scope change within the same
 * year leaves the grid alone.
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
            ) to today
        }
            .distinctUntilChanged()
            .flatMapLatest { (year, today) ->
                dependencies.observeReadingActivityUseCase().map { activity ->
                    Triple(
                        year,
                        today,
                        activity,
                    )
                }
            }
            .collect { (year, today, activity) ->
                val longest = activity.longestStreakIn(year)
                val current = activity.currentStreak(today)

                scope.setState {
                    it.copy(
                        yearGrid = yearGrid(
                            activity = activity,
                            year = year,
                        ),
                        yearAtAGlanceHeader = yearAtAGlanceHeader(year = year),
                        currentStreakTile = currentStreakTile(
                            streak = current,
                            today = today,
                        ),
                        longestStreakTile = longestStreakTile(
                            streak = longest,
                            year = year,
                        ),
                        atlasStreaks = AtlasStreaks(
                            activity = activity,
                            longestInAtlasYear = longest,
                            current = current,
                        ),
                    )
                }
            }
    }
}
