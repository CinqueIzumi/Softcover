package nl.rhaydus.softcover.core.domain.model

private const val FULL_PERCENT = 100.0

sealed interface QuotePosition {
    val percent: Double?

    data class Pages(
        val page: Int,
        val totalPages: Int?,
        override val percent: Double?,
    ) : QuotePosition

    data class Percent(
        override val percent: Double,
    ) : QuotePosition

    data class Seconds(
        val seconds: Int,
        val totalSeconds: Int?,
        override val percent: Double?,
    ) : QuotePosition

    companion object {
        fun pages(
            page: Int,
            totalPages: Int,
        ): Pages = Pages(
            page = page,
            totalPages = totalPages,
            percent = page.toDouble() / totalPages * FULL_PERCENT,
        )

        fun seconds(
            seconds: Int,
            totalSeconds: Int,
        ): Seconds = Seconds(
            seconds = seconds,
            totalSeconds = totalSeconds,
            percent = seconds.toDouble() / totalSeconds * FULL_PERCENT,
        )
    }
}
