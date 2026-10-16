package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.component.quote.QuotePositionTab
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.quotePageTotal
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.resolvedQuoteTab
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.sanitizedQuoteNumber
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

private const val PAGE_DIGITS = 4
private const val PERCENT_DIGITS = 3
private const val FULL_PERCENT = 100

/** Keeps digits only, a page within the book's page count and a percentage within 100. */
internal class OnQuotePositionChangedAction(
    private val text: String,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val book = scope.currentState.book ?: return

        scope.setState { state ->
            val draft = state.quoteDraft ?: return@setState state
            val sanitized = when (book.resolvedQuoteTab(draft)) {
                QuotePositionTab.PAGE -> sanitizedQuoteNumber(
                    text = text,
                    maximum = book.quotePageTotal ?: Int.MAX_VALUE,
                    maxLength = PAGE_DIGITS,
                )

                QuotePositionTab.PERCENTAGE,
                QuotePositionTab.TIME -> sanitizedQuoteNumber(
                    text = text,
                    maximum = FULL_PERCENT,
                    maxLength = PERCENT_DIGITS,
                )
            }

            state.copy(quoteDraft = draft.copy(positionText = sanitized))
        }
    }
}
