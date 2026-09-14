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

/**
 * Maps the reader's custom lists to the choose-lists sheet off the composition (R9).
 *
 * The "Owned" list is filtered out here rather than in the mapper: which lists to offer is this
 * screen's decision, and the mapper renders exactly what it is handed.
 */
internal class ChooseListsCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map {
                ChooseListsSnapshot(
                    bookId = it.book?.id,
                    bookTitle = it.book?.title,
                    currentEdition = it.book?.currentEdition,
                    userLists = it.userLists,
                    listsBeingMutated = it.listsBeingMutated,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val bookId = snapshot.bookId
                val bookTitle = snapshot.bookTitle

                val model = if (bookId != null && bookTitle != null) {
                    snapshot.userLists
                        .filter { it.isOwned.not() }
                        .toChooseListsUiModel(
                            bookId = bookId,
                            bookTitle = bookTitle,
                            cover = snapshot.currentEdition.toCoverUiModel(
                                defaultEdition = snapshot.currentEdition,
                                coverlessTitle = bookTitle,
                                variant = CoverVariant.ChooseListsSingleJacket,
                            ),
                            listsBeingMutated = snapshot.listsBeingMutated,
                        )
                } else {
                    null
                }

                scope.setState { it.copy(chooseListsSheet = model) }
            }
    }
}
