package nl.rhaydus.softcover.core.profile.data.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData

@Serializable
internal data class UserProfileDataEntity(
    val profileImageUrl: String,
    val name: String,
    val username: String = "",
    val bio: String,
    val booksRead: Int,
    val totalPagesRead: Int,
    val averageRating: Double,
    val readingStreak: Int,
    val activeReadingDates: List<String> = emptyList(),
)

internal fun UserProfileDataEntity.toModel(): UserProfileData = UserProfileData(
    profileImageUrl = profileImageUrl,
    name = name,
    username = username,
    bio = bio,
    booksRead = booksRead,
    totalPagesRead = totalPagesRead,
    averageRating = averageRating,
    readingStreak = readingStreak,
    activeReadingDates = activeReadingDates
        .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
        .toSet(),
)

internal fun UserProfileData.toEntity(): UserProfileDataEntity = UserProfileDataEntity(
    profileImageUrl = profileImageUrl,
    name = name,
    username = username,
    bio = bio,
    booksRead = booksRead,
    totalPagesRead = totalPagesRead,
    averageRating = averageRating,
    readingStreak = readingStreak,
    activeReadingDates = activeReadingDates
        .sorted()
        .map { it.toString() },
)
