package nl.rhaydus.softcover.core.data.database.model

import androidx.room.Entity
import androidx.room.Index

/**
 * Per-shelf user-defined ordering for books on built-in status shelves (Want to Read, Currently
 * Reading, Read). One row per (statusCode, bookId) — a book that lives on multiple shelves across
 * its lifetime keeps a separate manual position per shelf so re-shelving doesn't clobber the order
 * the user set on the other shelf. Hardcover does not model server-side per-shelf positions, so
 * this lives only locally.
 */
@Entity(
    tableName = "shelf_manual_order",
    primaryKeys = ["statusCode", "bookId"],
    indices = [
        Index("statusCode"),
        Index("bookId"),
    ],
)
data class ShelfManualOrderEntity(
    val statusCode: Int,
    val bookId: Int,
    val position: Int,
)
