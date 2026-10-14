package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.presentation.error.onApiFailure
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import kotlin.time.Duration.Companion.hours

internal class TrendingBooksLoader : ReadingCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
        dependencies: ReadingScreenDependencies,
    ) {
        coroutineScope {
            launch {
                dependencies.refreshTrendingBooksUseCase(maxAge = TRENDING_MAX_AGE).onApiFailure()
            }

            dependencies.observeTrendingBooksUseCase().collect { books ->
                scope.setState { it.copy(trendingBooks = books.toImmutableList()) }
            }
        }
    }

    private companion object {
        val TRENDING_MAX_AGE = 24.hours
    }
}
