package nl.rhaydus.softcover.feature.personal.domain.model

import java.time.Instant

data class PersonalReview(
    val bookId: Int,
    val body: String,
    val hasSpoilers: Boolean,
    val isDraft: Boolean,
    val updatedAt: Instant,
)
