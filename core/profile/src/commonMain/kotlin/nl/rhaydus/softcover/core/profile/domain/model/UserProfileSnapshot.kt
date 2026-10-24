package nl.rhaydus.softcover.core.profile.domain.model

/** Who the account belongs to, as the server reports it. */
data class UserProfileSnapshot(
    val profileImageUrl: String,
    val name: String,
    val username: String,
    val bio: String,
)
