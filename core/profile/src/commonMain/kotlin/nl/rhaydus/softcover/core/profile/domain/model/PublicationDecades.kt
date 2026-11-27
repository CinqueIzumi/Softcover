package nl.rhaydus.softcover.core.profile.domain.model

data class PublicationDecades(
    val booksByDecade: Map<Int, Int> = emptyMap(),
    val unknownYear: Int = 0,
    val oldestYear: Int? = null,
    val booksThisCentury: Int = 0,
)
