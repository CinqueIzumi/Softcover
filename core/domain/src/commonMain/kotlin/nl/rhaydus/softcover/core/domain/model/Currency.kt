package nl.rhaydus.softcover.core.domain.model

data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val fractionDigits: Int,
)
