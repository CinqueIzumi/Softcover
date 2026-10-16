package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_quote_writes")
data class PendingQuoteWriteEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val kind: String,
    val quoteLocalId: Long,
    val enqueuedAt: String,
    val attempts: Int = 0,
)
