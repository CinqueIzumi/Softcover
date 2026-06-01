package nl.rhaydus.softcover.core.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import nl.rhaydus.softcover.core.domain.model.TagCategory

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val category: String = TagCategory.OTHER.name,
)
