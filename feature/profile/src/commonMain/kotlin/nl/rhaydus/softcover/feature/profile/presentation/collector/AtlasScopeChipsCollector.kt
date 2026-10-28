package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ALL_TIME_SCOPE_KEY
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasScopeChips
import nl.rhaydus.softcover.feature.profile.presentation.mapper.statsScopeKey
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

/**
 * The only writer of the finishing history and the scope row built from it.
 *
 * The first history emission sets the default scope, this year when the reader has finished a book in it
 * and every year otherwise, unless they have already tapped a chip. Any later emission that drops the
 * selected year from the row falls back to every year. The default is never persisted.
 */
internal class AtlasScopeChipsCollector : StatsAtlasCollector {
    override suspend fun onLaunch(
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
        dependencies: StatsAtlasDependencies,
    ) {
        var hasSeenHistory = false

        combine(
            dependencies.observeReadingHistoryUseCase()
                .untilBackfilled(dependencies.observeReadingStatsBackfilledUseCase()),
            scope.state.map { it.selectedScopeKey }.distinctUntilChanged(),
        ) { history, selectedKey ->
            history to selectedKey
        }.collect { (history, selectedKey) ->
            val yearsRead = history?.yearsRead.orEmpty()

            val key = when {
                history == null -> selectedKey

                hasSeenHistory.not() && scope.currentLocalVariables.hasUserPickedScope.not() ->
                    defaultScopeKey(
                        yearsRead = yearsRead,
                        currentYear = dependencies.observeCurrentDateUseCase().firstOrNull()?.year,
                    )

                atlasScopeChips(
                    yearsRead = yearsRead,
                    selectedKey = selectedKey,
                )[selectedKey] == null -> ALL_TIME_SCOPE_KEY

                else -> selectedKey
            }

            if (history != null) hasSeenHistory = true

            scope.setState {
                it.copy(
                    readingHistory = history,
                    selectedScopeKey = key,
                    scopeChips = atlasScopeChips(
                        yearsRead = yearsRead,
                        selectedKey = key,
                    ),
                )
            }
        }
    }

    private fun defaultScopeKey(
        yearsRead: List<Int>,
        currentYear: Int?,
    ): String = if (currentYear != null && currentYear in yearsRead) {
        statsScopeKey(StatsScope.CalendarYear(currentYear))
    } else {
        ALL_TIME_SCOPE_KEY
    }
}
