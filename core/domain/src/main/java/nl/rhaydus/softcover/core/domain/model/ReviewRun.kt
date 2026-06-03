package nl.rhaydus.softcover.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ReviewRun(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val spoiler: Boolean = false,
)
