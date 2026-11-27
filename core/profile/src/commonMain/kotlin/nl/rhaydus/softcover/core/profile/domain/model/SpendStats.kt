package nl.rhaydus.softcover.core.profile.domain.model

/**
 * What the books read in scope cost. [countedBooks] is every book in scope; [pricedBooks] and every
 * figure in [currencies] leave out the ones without a price.
 */
data class SpendStats(
    val countedBooks: Int = 0,
    val pricedBooks: Int = 0,
    val currencies: List<CurrencySpend> = emptyList(),
)
