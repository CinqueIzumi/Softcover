package nl.rhaydus.softcover.feature.books.data.model

import androidx.room.DatabaseView
import androidx.room.Embedded

@DatabaseView(
    """
    SELECT 
        edition.*,
        EXISTS(
            SELECT 1
            FROM book_list_edition_cross_ref bler
            JOIN book_lists bl ON bl.id = bler.bookListId
            WHERE bler.editionId = edition.id
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