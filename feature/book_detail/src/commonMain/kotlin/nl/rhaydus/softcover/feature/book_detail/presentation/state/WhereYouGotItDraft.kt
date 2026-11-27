package nl.rhaydus.softcover.feature.book_detail.presentation.state

internal data class WhereYouGotItDraft(
    val source: WhereYouGotItSource,
    val amountText: String,
    val currencyCode: String,
    val amountError: String?,
    val body: WhereYouGotItBody,
)
