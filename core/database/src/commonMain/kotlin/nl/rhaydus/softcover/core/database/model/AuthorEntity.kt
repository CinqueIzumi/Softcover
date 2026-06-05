package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "authors")
data class AuthorEntity(
    @PrimaryKey val id: Int,
    val name: String,
)
