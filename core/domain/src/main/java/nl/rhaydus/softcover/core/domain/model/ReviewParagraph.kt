package nl.rhaydus.softcover.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ReviewParagraph(
    val runs: List<ReviewRun>,
)
