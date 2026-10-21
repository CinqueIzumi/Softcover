package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.toRecommendedHeader
import nl.rhaydus.toad.ActionScope

internal class RecommendedBooksCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        dependencies.observeRecommendedFeedUseCase(limit = RECOMMENDED_BOOKS_LIMIT)
            .collect { feed ->
                scope.setState {
                    it.copy(
                        recommendedBooks = feed.books.toImmutableList(),
                        loadingRecommendedBooks = feed.isLoading,
                        recommendedHeader = feed.source.toRecommendedHeader(isLoading = feed.isLoading),
                    )
                }
            }
    }

    private companion object {
        const val RECOMMENDED_BOOKS_LIMIT = 10
    }
}
