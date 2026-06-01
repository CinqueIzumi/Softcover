package nl.rhaydus.softcover.core.data.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_user_book_writes",
    indices = [
        Index(value = ["userBookId", "kind"], unique = true),
    ],
)
data class PendingUserBookWriteEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val kind: String,
    val userBookId: Int,
    val userBookReadId: Int,
    val bookId: Int,
    val editionId: Int?,
    val progressPages: Int?,
    val progressSeconds: Int?,
    val startedAt: String?,
    val finishedAt: String?,
    val rating: Double? = null,
    val reviewSlateJson: String? = null,
    val reviewHasSpoilers: Boolean? = null,
    val enqueuedAt: String,
    val attempts: Int = 0,
)
