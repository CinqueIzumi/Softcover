package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * No foreign key to books: book rows are a pruned cache and traits must outlive them.
 *
 * @property updatedAt epoch milliseconds of the last save.
 */
@Entity(tableName = "book_traits")
data class BookTraitsEntity(
    @PrimaryKey val bookId: Int,
    val pace: String? = null,
    val plotCharacterBalance: Int? = null,
    val likeable: String? = null,
    val believable: String? = null,
    val wellDeveloped: String? = null,
    val multiLayered: String? = null,
    val definedByFlaws: String? = null,
    val bipoc: String? = null,
    val lgbtq: String? = null,
    val writingStyle: String? = null,
    val vivid: Boolean = false,
    val poetic: Boolean = false,
    val unconventional: Boolean = false,
    val updatedAt: Long,
)
