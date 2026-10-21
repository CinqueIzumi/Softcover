package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hidden_recommendations")
data class HiddenRecommendationEntity(
    @PrimaryKey val bookId: Int,
    val bookTitle: String? = null,
    val coverUrl: String? = null,
    val authorText: String? = null,
)
