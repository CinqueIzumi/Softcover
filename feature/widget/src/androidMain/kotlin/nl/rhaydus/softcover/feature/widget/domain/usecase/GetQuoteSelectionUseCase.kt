package nl.rhaydus.softcover.feature.widget.domain.usecase

import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.widget.domain.model.QuoteSelection
import nl.rhaydus.softcover.feature.widget.domain.repository.QuoteWidgetRepository

internal class GetQuoteSelectionUseCase(private val quoteWidgetRepository: QuoteWidgetRepository) {
    suspend operator fun invoke(appWidgetId: Int): Result<QuoteSelection?> = runCatchingCancellable {
        quoteWidgetRepository.getSelection(appWidgetId = appWidgetId)
    }
}
