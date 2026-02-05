package nl.rhaydus.softcover.feature.settings.domain.model

data class UserProfileData(
    val profileImageUrl: String,
    val name: String,
    val bio: String,
    val booksRead: Int,
)