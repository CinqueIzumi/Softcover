package nl.rhaydus.softcover.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * No foreign key to books: book rows are a pruned cache and the reader's copy must outlive them.
 *
 * @property customSource the reader's own words for where they got it; non-blank, it wins over [source].
 * @property updatedAt epoch milliseconds of the last save.
 */
@Entity(tableName = "book_copies")
data class BookCopyEntity(
    @PrimaryKey val bookId: Int,
    val source: String? = null,
    val customSource: String? = null,
    val updatedAt: Long,
    val priceThousandths: Long? = null,
    val priceCurrency: String? = null,
)
