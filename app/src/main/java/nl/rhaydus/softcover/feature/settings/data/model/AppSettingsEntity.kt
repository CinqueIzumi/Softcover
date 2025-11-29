package nl.rhaydus.softcover.feature.settings.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettingsEntity(
    val apiKey: String = "",
    val userId: Int = -1,
)