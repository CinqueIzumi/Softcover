package nl.rhaydus.softcover.core.profile.domain.model

data class LovedBook(
    val coverUrl: String,
    val title: String,
    val author: String,
    val ratingStars: Double,
    val monthLabel: String,
)
