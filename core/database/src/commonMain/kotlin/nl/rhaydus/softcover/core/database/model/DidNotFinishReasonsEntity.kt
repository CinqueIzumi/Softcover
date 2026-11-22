package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "did_not_finish_reasons")
data class DidNotFinishReasonsEntity(
    @PrimaryKey val bookId: Int,
    val notTheRightTime: Boolean,
    val notMyKindOfWriting: Boolean,
    val tooSlow: Boolean,
    val lostInterest: Boolean,
    val unwantedContent: Boolean,
    val ownWords: String? = null,
    val updatedAt: Long,
)
