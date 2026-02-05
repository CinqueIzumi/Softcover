package nl.rhaydus.softcover.feature.caching.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserBookReadEntity(
    val id: Int,
    val currentPage: Int?,
    val progress: Float?,
    val startedAt: String?,
    val finishedAt: String?,
)