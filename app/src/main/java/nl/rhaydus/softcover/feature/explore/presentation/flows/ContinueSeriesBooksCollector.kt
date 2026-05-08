package nl.rhaydus.softcover.feature.explore.presentation.flows

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import timber.log.Timber

class ContinueSeriesBooksCollector : ExploreInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        dependencies.getContinueSeriesBooksUseCase()
            .catch { error ->
                Timber.e(error, "Failed to fetch continue-series books")

                emit(emptyList())
            }
            .collectLatest { books ->
                scope.setState {
                    it.copy(
                        continueSeriesBooks = books,
                        loadingContinueSeriesBooks = false,
                    )
                }
            }
    }
}
