package nl.rhaydus.softcover.feature.profile.domain.model

import java.time.LocalDate

data class UserProfileSnapshot(
    val profileImageUrl: String,
    val name: String,
    val bio: String,
    val booksRead: Int,
    val totalPagesRead: Int,
    val averageRating: Double,
    val activeReadingDates: Set<LocalDate>,
)
