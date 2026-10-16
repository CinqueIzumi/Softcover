package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.sanitizedQuoteNumber
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

private const val HOUR_DIGITS = 3
private const val CLOCK_DIGITS = 2
private const val MAX_CLOCK_VALUE = 59

internal class OnQuoteTimeChangedAction(
    private val unit: ProgressTimeUnit,
    private val text: String,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.setState { state ->
            val draft = state.quoteDraft ?: return@setState state

            state.copy(
                quoteDraft = when (unit) {
                    ProgressTimeUnit.HOURS -> draft.copy(
                        hours = sanitizedQuoteNumber(
                            text = text,
                            maximum = Int.MAX_VALUE,
                            maxLength = HOUR_DIGITS,
                        ),
                    )

                    ProgressTimeUnit.MINUTES -> draft.copy(minutes = clockPart())

                    ProgressTimeUnit.SECONDS -> draft.copy(seconds = clockPart())
                },
            )
        }
    }

    private fun clockPart(): String = sanitizedQuoteNumber(
        text = text,
        maximum = MAX_CLOCK_VALUE,
        maxLength = CLOCK_DIGITS,
    )
}
