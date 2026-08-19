package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The Roadmap screen's offline cache: a single row (`id` is always `0`) holding the raw `ROADMAP.md`
 * markdown, not its parsed form, so a parser improvement re-renders already-cached content correctly.
 */
@Entity(tableName = "roadmap_documents")
data class RoadmapDocumentEntity(
    @PrimaryKey val id: Int = 0,
    val markdown: String,
    val fetchedAtEpochMillis: Long,
)
