package nl.rhaydus.softcover.feature.personal.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personal_reviews")
data class PersonalReviewEntity(
    @PrimaryKey
    val bookId: Int,
    val body: String,
    val hasSpoilers: Boolean,
    val isDraft: Boolean,
    val updatedAt: String,
)
