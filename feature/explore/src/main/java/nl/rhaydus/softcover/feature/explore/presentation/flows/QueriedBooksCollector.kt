package nl.rhaydus.softcover.feature.explore.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

internal class QueriedBooksCollector : ExploreInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        combine(
            dependencies.getAllUserBooksUseCase(),
            dependencies.getQueriedBooksUseCase(),
        ) { allUserBooks: List<Book>, fetchedBooks: List<Book> ->
            fetchedBooks.map {
                allUserBooks.find { userBook -> userBook.id == it.id } ?: it
            }
        }.collectLatest { queriedBooks: List<Book> ->
            scope.setState { it.copy(queriedBooks = queriedBooks) }
        }
    }
}
