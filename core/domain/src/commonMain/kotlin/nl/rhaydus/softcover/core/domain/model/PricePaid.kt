package nl.rhaydus.softcover.core.domain.model

/**
 * @property amountThousandths thousandths of the currency's main unit for every currency, whatever its own
 * fraction digits (14.50 EUR is 14500, 1450 JPY is 1450000).
 * @property currencyCode an ISO 4217 code.
 */
data class PricePaid(
    val amountThousandths: Long,
    val currencyCode: String,
)
