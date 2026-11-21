package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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
 * Maps the book title, the where-you-got-it draft and the usual currency to the sheet's UI model off the
 * composition. The only writer of [BookDetailUiState.whereYouGotItSheet]: it opens while a draft exists and
 * asks the sheet to hide otherwise.
 */
internal class WhereYouGotItSheetCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        val currencies = dependencies.getCurrenciesUseCase()

        combine(
            scope.state.map { it.book?.title to it.whereYouGotItDraft },
            dependencies.getUsualCurrencyAsFlowUseCase().map { it.code },
        ) { (title, draft), usualCode ->
            WhereYouGotItSheetSnapshot(
                bookTitle = title,
                draft = draft,
                usualCurrencyCode = usualCode,
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
                                model = liveDraft.toWhereYouGotItSheet(
                                    bookTitle = title,
                                    currencies = currencies,
                                    usualCurrencyCode = snapshot.usualCurrencyCode,
                                ),
                            )
                        },
                    )
                }
            }
    }
}
