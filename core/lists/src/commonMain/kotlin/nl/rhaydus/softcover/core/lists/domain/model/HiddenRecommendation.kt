package nl.rhaydus.softcover.core.lists.domain.model

data class HiddenRecommendation(
    val bookId: Int,
    val title: String?,
    val coverUrl: String?,
    val authorText: String?,
)
