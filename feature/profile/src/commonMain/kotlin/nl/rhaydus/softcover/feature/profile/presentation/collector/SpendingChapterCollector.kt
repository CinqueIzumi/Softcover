package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.Currency
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.resolve
import nl.rhaydus.softcover.core.profile.domain.model.SpendStats
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.spendingChapterUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

/**
 * The only writer of the Recommenders and spending chapter. It stays idle until the history has landed,
 * like `AtlasScopedSectionsCollector`, and holds the stats back until the library is backfilled so an
 * unfilled one never reads as a reader who paid nothing.
 */
internal class SpendingChapterCollector : StatsAtlasCollector {
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
                    dependencies.observeSpending(snapshot.scope)
                } else {
                    emptyFlow()
                }
            }
            .collect { (stats, usual) ->
                scope.setState {
                    it.copy(
                        spendingChapter = spendingChapterUiModel(
                            stats = stats,
                            currencies = dependencies.getCurrenciesUseCase(),
                            usual = usual,
                        ),
                    )
                }
            }
    }

    private fun StatsAtlasDependencies.observeSpending(scope: StatsScope): Flow<Pair<SpendStats?, Currency>> =
        observeCurrentDateUseCase().flatMapLatest { today ->
            combine(
                observeSpendStatsUseCase(scope.resolve(today))
                    .untilBackfilled(
                        combine(
                            observeReadingStatsBackfilledUseCase(),
                            observeReadDatesBackfilledUseCase(),
                        ) { statsBackfilled, readDatesBackfilled -> statsBackfilled && readDatesBackfilled },
                    ),
                getUsualCurrencyAsFlowUseCase(),
            ) { stats, usual -> stats to usual }
        }

    private data class ScopeSnapshot(
        val scope: StatsScope,
        val isHistoryLoaded: Boolean,
    )
}
