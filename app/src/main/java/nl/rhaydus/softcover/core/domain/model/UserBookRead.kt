package nl.rhaydus.softcover.core.domain.model

data class UserBookRead(
    val id: Int,
    val currentPage: Int?,
    val progress: Float?,
    val startedAt: String?,
    val finishedAt: String?,
)