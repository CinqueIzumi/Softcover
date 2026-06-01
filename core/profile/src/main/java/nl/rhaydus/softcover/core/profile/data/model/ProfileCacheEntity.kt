package nl.rhaydus.softcover.core.profile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProfileCacheEntity(
    val profile: UserProfileDataEntity? = null,
)
