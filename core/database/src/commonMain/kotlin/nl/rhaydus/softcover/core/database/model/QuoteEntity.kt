package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "book_quotes",
    indices = [
        Index("bookId"),
        Index(
            value = ["remoteId"],
            unique = true,
        ),
    ],
)
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val remoteId: Long?,
    val bookId: Int,
    val editionId: Int?,
    val text: String,
    val privacySettingId: Int,
    val actionAtEpochMillis: Long,
    val positionType: String?,
    val positionValue: Double?,
    val positionPossible: Double?,
    val positionPercent: Double?,
    val syncState: QuoteSyncState,
    val bookTitle: String? = null,
    val editionTitle: String? = null,
    val editionCoverUrl: String? = null,
) {
    /** Whether [other] is the server copy of this unsynced row: same book, text, instant and edition. */
    fun isSameQuoteAs(other: QuoteEntity): Boolean = bookId == other.bookId &&
        text == other.text &&
        actionAtEpochMillis == other.actionAtEpochMillis &&
        editionId == other.editionId
}
