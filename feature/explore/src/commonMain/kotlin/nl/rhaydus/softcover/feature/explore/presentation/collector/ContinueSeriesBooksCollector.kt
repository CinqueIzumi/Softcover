package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

internal class ContinueSeriesBooksCollector : ExploreCollector {
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        val continueSeriesFlow = dependencies.continueSeriesRefreshTrigger
            .flatMapLatest {
                dependencies.getContinueSeriesBooksUseCase()
            }
            .catch { error ->
                AppLog.e(
                    error,
                    "Failed to fetch continue-series books",
                )

                emit(emptyList())
            }

        combine(
            continueSeriesFlow,
            dependencies.getAllUserBooksUseCase(),
        ) { books, allUserBooks ->
            books.map { book -> allUserBooks.find { it.id == book.id } ?: book }
        }.collectLatest { books ->
            scope.setState {
                it.copy(
                    continueSeriesBooks = books,
                    loadingContinueSeriesBooks = false,
                )
            }
        }
    }
}
