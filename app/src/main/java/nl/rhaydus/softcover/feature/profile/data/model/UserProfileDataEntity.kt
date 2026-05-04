package nl.rhaydus.softcover.feature.profile.data.model

import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.feature.profile.domain.model.UserProfileData

@Serializable
data class UserProfileDataEntity(
    val profileImageUrl: String,
    val name: String,
    val bio: String,
    val booksRead: Int,
    val totalPagesRead: Int,
    val averageRating: Double,
    val readingStreak: Int,
)

fun UserProfileDataEntity.toModel(): UserProfileData = UserProfileData(
    profileImageUrl = profileImageUrl,
    name = name,
    bio = bio,
    booksRead = booksRead,
    totalPagesRead = totalPagesRead,
    averageRating = averageRating,
    readingStreak = readingStreak,
)

fun UserProfileData.toEntity(): UserProfileDataEntity = UserProfileDataEntity(
    profileImageUrl = profileImageUrl,
    name = name,
    bio = bio,
    booksRead = booksRead,
    totalPagesRead = totalPagesRead,
    averageRating = averageRating,
    readingStreak = readingStreak,
)
