package nl.rhaydus.softcover.core.profile.data.model

import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData

@Serializable
internal data class UserProfileDataEntity(
    val profileImageUrl: String,
    val name: String,
    val username: String = "",
    val bio: String,
)

internal fun UserProfileDataEntity.toModel(): UserProfileData = UserProfileData(
    profileImageUrl = profileImageUrl,
    name = name,
    username = username,
    bio = bio,
)

internal fun UserProfileData.toEntity(): UserProfileDataEntity = UserProfileDataEntity(
    profileImageUrl = profileImageUrl,
    name = name,
    username = username,
    bio = bio,
)
