package nl.rhaydus.softcover.feature.connectivity.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_progress_updates",
    indices = [
        Index(value = ["userBookId", "kind"], unique = true),
    ],
)
data class PendingProgressUpdateEntity(
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
    val enqueuedAt: String,
    val attempts: Int = 0,
)
