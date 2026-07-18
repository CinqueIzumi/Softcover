package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_list_writes")
data class PendingListWriteEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val kind: String,
    val listId: Int?,
    val listName: String?,
    val bookId: Int?,
    val editionId: Int?,
    val listBookId: Int?,
    val startPosition: Int?,
    val orderedListBookIdsCsv: String?,
    val enqueuedAt: String,
    val attempts: Int = 0,
    val privacySettingId: Int? = null,
)
