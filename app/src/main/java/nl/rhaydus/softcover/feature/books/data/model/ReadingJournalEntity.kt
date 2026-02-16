package nl.rhaydus.softcover.feature.books.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "reading_journals")
@Serializable
data class ReadingJournalEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val userBookId: Int,
    val event: String,
    val updatedAt: String,
)