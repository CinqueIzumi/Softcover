package nl.rhaydus.softcover.feature.explore.domain.model

data class MoodTag(
    val id: Int,
    val label: String,
    val slug: String,
    val bookCount: Int,
)
