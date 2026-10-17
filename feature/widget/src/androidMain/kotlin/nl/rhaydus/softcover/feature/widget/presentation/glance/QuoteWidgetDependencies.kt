package nl.rhaydus.softcover.feature.widget.presentation.glance

import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveAllQuotesUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.RefreshAllQuotesUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.CycleQuoteUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetQuoteSelectionUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveQuoteSelectionChangesUseCase

internal class QuoteWidgetDependencies(
    val environment: WidgetEnvironment,
    val repainter: WidgetRepainter,
    val observeAllQuotes: ObserveAllQuotesUseCase,
    val refreshAllQuotes: RefreshAllQuotesUseCase,
    val getQuoteSelection: GetQuoteSelectionUseCase,
    val cycleQuote: CycleQuoteUseCase,
    val observeQuoteSelectionChanges: ObserveQuoteSelectionChangesUseCase,
)
