package nl.rhaydus.softcover.core.profile.domain.model

import kotlinx.datetime.LocalDate

data class UserProfileData(
    val profileImageUrl: String,
    val name: String,
    val username: String,
    val bio: String,
    val readingStreak: Int,
    val recentReadingDays: Set<LocalDate> = emptySet(),
)
