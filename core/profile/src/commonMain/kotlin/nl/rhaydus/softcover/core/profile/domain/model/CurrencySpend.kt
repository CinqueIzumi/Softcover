package nl.rhaydus.softcover.core.profile.domain.model

/** One currency's spend, never converted; [sources] are ordered by spend, largest first. */
data class CurrencySpend(
    val currencyCode: String,
    val totalThousandths: Long,
    val pricedBooks: Int,
    val medianThousandths: Long,
    val sources: List<SourceSpend>,
)
