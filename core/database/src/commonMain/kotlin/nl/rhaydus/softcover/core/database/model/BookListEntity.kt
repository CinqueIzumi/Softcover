package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import nl.rhaydus.softcover.core.domain.model.PrivacySetting

@Entity(tableName = "book_lists")
data class BookListEntity(
    @PrimaryKey
    val id: Int,

    val name: String,
    val slug: String,
    val ranked: Boolean = false,
    val privacySettingId: Int = PrivacySetting.PUBLIC.code,

    /**
     * The freshness signature captured the last time this list's `list_books` were deep-fetched.
     * Compared against the server's signature to decide whether the contents must be re-fetched;
     * `null` for rows created before the column existed or written by an optimistic mutation, which
     * forces one re-fetch on the next full refresh.
     */
    val signature: String? = null,
)
