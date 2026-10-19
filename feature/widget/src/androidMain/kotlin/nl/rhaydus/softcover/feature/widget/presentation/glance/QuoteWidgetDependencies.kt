package nl.rhaydus.softcover.feature.widget.presentation.glance

import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveAllQuotesUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.RefreshAllQuotesUseCase
import nl.rhaydus.softcover.feature.widget.domain.model.QuoteFetchTracker
import nl.rhaydus.softcover.feature.widget.domain.usecase.CycleQuoteUseCase
import nl.rhaydus.softcover.feature.widget.presentation.refresh.QuoteFetcher

internal class QuoteWidgetDependencies(
    val environment: WidgetEnvironment,
    val repainter: WidgetRepainter,
    val observeAllQuotes: ObserveAllQuotesUseCase,
    val refreshAllQuotes: RefreshAllQuotesUseCase,
    val cycleQuote: CycleQuoteUseCase,
    val fetchTracker: QuoteFetchTracker,
    val fetcher: QuoteFetcher,
)
