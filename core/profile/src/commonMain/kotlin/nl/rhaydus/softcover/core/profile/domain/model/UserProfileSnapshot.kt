package nl.rhaydus.softcover.core.profile.domain.model

import kotlinx.datetime.LocalDate

data class UserProfileSnapshot(
    val profileImageUrl: String,
    val name: String,
    val username: String,
    val bio: String,
    val booksRead: Int,
    val totalPagesRead: Int,
    val averageRating: Double,
    val activeReadingDates: Set<LocalDate>,
)
