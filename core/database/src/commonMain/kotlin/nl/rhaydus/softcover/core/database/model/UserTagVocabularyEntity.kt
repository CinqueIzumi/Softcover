package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity

@Entity(tableName = "user_tag_vocabulary", primaryKeys = ["userId", "category", "name"])
data class UserTagVocabularyEntity(
    val userId: Int,
    val category: String,
    val name: String,
    val usageCount: Int = 0,
)
