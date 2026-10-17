package nl.rhaydus.softcover.feature.widget.domain.usecase

import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.feature.widget.domain.model.QuoteSelection
import nl.rhaydus.softcover.feature.widget.domain.repository.QuoteWidgetRepository
import nl.rhaydus.softcover.feature.widget.domain.util.quoteId
import nl.rhaydus.softcover.feature.widget.domain.util.rankForDay
import nl.rhaydus.softcover.feature.widget.domain.util.resolveShown
import nl.rhaydus.softcover.feature.widget.domain.util.stepQuote

internal class CycleQuoteUseCase(private val quoteWidgetRepository: QuoteWidgetRepository) {
    suspend operator fun invoke(
        appWidgetId: Int,
        quotes: List<Quote>,
        today: Long,
        step: Int,
    ): Result<Unit> = runCatchingCancellable {
        val ranking = rankForDay(
            quotes = quotes,
            epochDay = today,
        )

        val selection = quoteWidgetRepository.getSelection(appWidgetId = appWidgetId)

        val shown = resolveShown(
            ranking = ranking,
            selectedQuoteId = selection?.quoteId,
            selectedDay = selection?.epochDay,
            today = today,
        )

        val next = stepQuote(
            ranking = ranking,
            currentQuoteId = shown?.let(::quoteId),
            step = step,
        )

        if (next != null) {
            quoteWidgetRepository.saveSelection(
                appWidgetId = appWidgetId,
                selection = QuoteSelection(
                    quoteId = quoteId(next),
                    epochDay = today,
                ),
            )
        }
    }
}
