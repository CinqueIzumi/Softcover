package nl.rhaydus.softcover.core.database.model

import androidx.room.DatabaseView
import androidx.room.Embedded

@DatabaseView(
    """
    SELECT
        edition.*,
        EXISTS(
            SELECT 1
            FROM list_books lb
            JOIN book_lists bl ON bl.id = lb.listId
            WHERE bl.slug = 'owned'
            AND (
                lb.editionId = edition.id
                OR lb.editionId = edition.canonicalId
                OR lb.editionId IN (
                    SELECT sub.id FROM book_editions sub
                    WHERE sub.canonicalId = edition.id
                )
            )
        ) AS isOwned
    FROM book_editions edition
    """,
    viewName = "book_edition_view",
)
data class BookEditionView(
    @Embedded
    val edition: BookEditionEntity,

    val isOwned: Boolean,
)
