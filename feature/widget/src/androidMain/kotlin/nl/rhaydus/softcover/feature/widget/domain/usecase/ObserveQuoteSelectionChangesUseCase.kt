package nl.rhaydus.softcover.feature.widget.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.widget.domain.repository.QuoteWidgetRepository

internal class ObserveQuoteSelectionChangesUseCase(private val quoteWidgetRepository: QuoteWidgetRepository) {
    operator fun invoke(): Flow<Int> = quoteWidgetRepository.selectionChanges
}
