package nl.rhaydus.softcover.feature.caching.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "books",
    indices = [
        Index(value = ["userBook_id"], unique = true),
    ]
)
@Serializable
data class BookEntity(
    // region book
    @PrimaryKey val id: Int,
    val title: String,
    val defaultEditionId: Int?,
    val rating: Double,
    val description: String,
    val releaseYear: Int,
    val coverUrl: String,
    // endregion

    @Embedded(prefix = "userBook_")
    val userBook: UserBookEntity?,

    @Embedded(prefix = "userBookRead_")
    val userBookReadEntity: UserBookReadEntity?,
)