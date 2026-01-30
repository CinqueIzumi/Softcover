package nl.rhaydus.softcover.feature.caching.data.model

import androidx.room.Entity

@Entity(
    tableName = "edition_author_cross_ref",
    primaryKeys = ["editionId", "authorId"]
)
data class EditionAuthorCrossRef(
    val editionId: Int,
    val authorId: Int,
)