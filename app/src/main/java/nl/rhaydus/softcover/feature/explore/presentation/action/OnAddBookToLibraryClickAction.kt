package nl.rhaydus.softcover.feature.explore.presentation.action

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import timber.log.Timber

class OnAddBookToLibraryClickAction(val book: Book) : ExploreAction {
    override suspend fun execute(
        dependencies: ExploreDependencies,
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
    ) {
        dependencies.markBookAsWantToReadUseCase(bookId = book.id).onFailure {
            Timber.e("-=-= $it")
        }
    }
}