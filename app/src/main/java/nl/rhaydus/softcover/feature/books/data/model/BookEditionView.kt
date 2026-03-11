package nl.rhaydus.softcover.feature.books.data.model

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
            WHERE lb.editionId = edition.id
            AND bl.slug = 'owned'
        ) AS isOwned
    FROM book_editions edition
    """,
    viewName = "book_edition_view"
)
data class BookEditionView(
    @Embedded
    val edition: BookEditionEntity,

    val isOwned: Boolean
)