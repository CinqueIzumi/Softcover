package nl.rhaydus.softcover.feature.profile.domain.model

data class UserProfileData(
    val profileImageUrl: String,
    val name: String,
    val bio: String,
    val booksRead: Int,
    val totalPagesRead: Int,
    val averageRating: Double,
    val readingStreak: Int,
)
