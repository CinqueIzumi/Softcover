package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.core.uibinding.lists.toChooseListsUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class ChooseListsCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map {
                ChooseListsSnapshot(
                    book = it.book,
                    userLists = it.userLists,
                    listsBeingMutated = it.listsBeingMutated,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val book = snapshot.book

                val model = if (book != null) {
                    snapshot.userLists.toChooseListsUiModel(
                        bookId = book.id,
                        bookTitle = book.title,
                        cover = book.toCoverUiModel(variant = CoverVariant.Jacket),
                        listsBeingMutated = snapshot.listsBeingMutated,
                        previous = scope.currentState.chooseListsSheet,
                    )
                } else {
                    null
                }

                scope.setState { it.copy(chooseListsSheet = model) }
            }
    }
}
