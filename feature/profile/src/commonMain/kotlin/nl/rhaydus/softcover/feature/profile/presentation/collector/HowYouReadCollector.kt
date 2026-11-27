package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.resolve
import nl.rhaydus.softcover.core.profile.domain.model.HowYouReadStats
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.howYouReadUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

internal class HowYouReadCollector : StatsAtlasCollector {
    override suspend fun onLaunch(
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
        dependencies: StatsAtlasDependencies,
    ) {
        scope.state
            .map { state ->
                ScopeSnapshot(
                    scope = state.selectedScope,
                    isHistoryLoaded = state.readingHistory != null,
                )
            }
            .distinctUntilChanged()
            .flatMapLatest { snapshot ->
                if (snapshot.isHistoryLoaded) {
                    dependencies.observeHowYouRead(snapshot.scope).map { snapshot.scope to it }
                } else {
                    emptyFlow()
                }
            }
            .collect { (statsScope, stats) ->
                scope.setState {
                    it.copy(
                        howYouRead = howYouReadUiModel(
                            stats = stats,
                            scope = statsScope,
                        ),
                    )
                }
            }
    }

    private fun StatsAtlasDependencies.observeHowYouRead(scope: StatsScope): Flow<HowYouReadStats?> =
        observeCurrentDateUseCase().flatMapLatest { today ->
            observeHowYouReadStatsUseCase(scope.resolve(today))
                .untilBackfilled(
                    combine(
                        observeReadingStatsBackfilledUseCase(),
                        observeReadDatesBackfilledUseCase(),
                    ) { statsBackfilled, readDatesBackfilled -> statsBackfilled && readDatesBackfilled },
                )
        }

    private data class ScopeSnapshot(
        val scope: StatsScope,
        val isHistoryLoaded: Boolean,
    )
}
