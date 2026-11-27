package nl.rhaydus.softcover.core.profile.domain.model

data class WrittenStats(
    val reviewCount: Int,
    val reviewWordCount: Int,
    val quoteCount: Int,
    val quotedBookCount: Int,
    val mostQuoted: MostQuotedBook?,
)
