package nl.rhaydus.softcover.feature.profile.domain.model

import java.time.LocalDate

data class UserProfileData(
    val profileImageUrl: String,
    val name: String,
    val username: String,
    val bio: String,
    val booksRead: Int,
    val totalPagesRead: Int,
    val averageRating: Double,
    val readingStreak: Int,
    val activeReadingDates: Set<LocalDate> = emptySet(),
)
