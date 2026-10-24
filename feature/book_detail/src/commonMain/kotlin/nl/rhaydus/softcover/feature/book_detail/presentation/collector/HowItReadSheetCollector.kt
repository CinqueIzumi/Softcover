package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toHowItReadSheet
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the book title and the how-it-read draft to the sheet's UI model off the composition. The only
 * writer of [BookDetailUiState.howItReadSheet]: it opens while a draft exists and asks the sheet to hide
 * otherwise.
 */
internal class HowItReadSheetCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map {
                HowItReadSheetSnapshot(
                    bookTitle = it.book?.title,
                    draft = it.howItReadDraft,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val title = snapshot.bookTitle
                val draft = snapshot.draft

                scope.setState { state ->
                    val liveDraft = state.howItReadDraft

                    state.copy(
                        howItReadSheet = if (title == null || draft == null || liveDraft == null) {
                            state.howItReadSheet.requestClose()
                        } else {
                            state.howItReadSheet.openOrUpdate(
                                model = liveDraft.toHowItReadSheet(bookTitle = title),
                            )
                        },
                    )
                }
            }
    }
}
