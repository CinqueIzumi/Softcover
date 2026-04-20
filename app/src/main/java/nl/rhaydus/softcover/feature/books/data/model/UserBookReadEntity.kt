package nl.rhaydus.softcover.feature.books.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "user_book_reads",
    foreignKeys = [
        ForeignKey(
            entity = UserBookEntity::class,
            parentColumns = ["id"],
            childColumns = ["userBookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userBookId")]
)
@Serializable
data class UserBookReadEntity(
    @PrimaryKey
    val id: Int,

    val userBookId: Int,

    val currentPage: Int?,
    val currentSeconds: Int?,
    val progress: Float?,
    val startedAt: String?,
    val finishedAt: String?,
)