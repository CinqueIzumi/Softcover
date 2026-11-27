package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.quote.QuoteCardUiModel

internal data class MostQuotedUiModel(
    val title: String,
    val caption: String,
    val card: QuoteCardUiModel.Book,
    val bookId: Int,
)
