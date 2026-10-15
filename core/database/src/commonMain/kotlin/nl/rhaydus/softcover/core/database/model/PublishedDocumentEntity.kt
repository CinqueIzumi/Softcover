package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The offline cache of one document published from the repo (the roadmap, the changelog): one row per
 * [key], holding the raw markdown rather than its parsed form so a parser improvement re-renders
 * already-cached content correctly.
 */
@Entity(tableName = "published_documents")
data class PublishedDocumentEntity(
    @PrimaryKey val key: String,
    val markdown: String,
    val fetchedAtEpochMillis: Long,
)
