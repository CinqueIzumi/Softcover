package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toWhereYouGotItSheet
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the book title and the where-you-got-it draft to the sheet's UI model off the composition. The only
 * writer of [BookDetailUiState.whereYouGotItSheet]: it opens while a draft exists and asks the sheet to hide
 * otherwise.
 */
internal class WhereYouGotItSheetCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map {
                WhereYouGotItSheetSnapshot(
                    bookTitle = it.book?.title,
                    draft = it.whereYouGotItDraft,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val title = snapshot.bookTitle
                val draft = snapshot.draft

                scope.setState { state ->
                    val liveDraft = state.whereYouGotItDraft

                    state.copy(
                        whereYouGotItSheet = if (title == null || draft == null || liveDraft == null) {
                            state.whereYouGotItSheet.requestClose()
                        } else {
                            state.whereYouGotItSheet.openOrUpdate(
                                model = liveDraft.toWhereYouGotItSheet(bookTitle = title),
                            )
                        },
                    )
                }
            }
    }
}
