package nl.rhaydus.softcover.feature.books.data.sort

import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection

/**
 * Maps a [LibrarySortMode] + [SortDirection] to the SQL `ORDER BY` fragment used by the
 * library DAO's `@RawQuery` paths. Everything here is fully allowlisted — the mode + direction
 * are typed enums, so no caller input ever reaches the SQL string.
 *
 * Aliases assumed by the caller:
 * - `b`   → `books`
 * - `ub`  → `user_books` (LEFT JOIN for All tab, INNER JOIN for status tabs)
 *
 * Null-handling notes (matching the previous in-memory sort semantics):
 * - Modes whose Kotlin equivalent substituted `MIN` (`DATE_ADDED`, `DATE_FINISHED`) leave SQLite's
 *   default null ordering in place (nulls first on ASC, last on DESC).
 * - Modes whose Kotlin equivalent substituted `MAX` / a far-future sentinel (`RELEASE_DATE`,
 *   `DEADLINE_URGENCY`) `COALESCE` to a large ISO-date literal so nulls land "after" all real
 *   values in ASC order — i.e. unscheduled books sink to the bottom when sorting soonest-first.
 * - Numeric `MIN` sentinels (`RATING` 0, `PROGRESS` 0, `PAGE_COUNT` 0) `COALESCE` to 0 so books
 *   without a value sort with the smallest end.
 */
internal fun LibrarySortMode.toOrderByFragment(direction: SortDirection): String {
    val dir = direction.sqlKeyword()

    return when (this) {
        LibrarySortMode.DATE_ADDED ->
            "ub.dateAdded $dir"

        LibrarySortMode.DATE_FINISHED -> """
            COALESCE(
                (SELECT MAX(finishedAt) FROM user_book_reads
                    WHERE userBookId = ub.id AND finishedAt IS NOT NULL),
                (SELECT MAX(updatedAt) FROM reading_journals
                    WHERE userBookId = ub.id AND event = 'user_book_read_finished')
            ) $dir
        """.trimIndent()

        LibrarySortMode.TITLE ->
            "b.title COLLATE NOCASE $dir"

        LibrarySortMode.AUTHOR -> """
            COALESCE(
                (SELECT MIN(a.name COLLATE NOCASE) FROM authors a
                    INNER JOIN book_author_cross_ref ba ON ba.authorId = a.id
                    WHERE ba.bookId = b.id),
                ''
            ) COLLATE NOCASE $dir
        """.trimIndent()

        LibrarySortMode.RATING ->
            "b.rating $dir"

        LibrarySortMode.PROGRESS -> """
            COALESCE(
                (SELECT MAX(progress) FROM user_book_reads WHERE userBookId = ub.id),
                0
            ) $dir
        """.trimIndent()

        LibrarySortMode.PAGE_COUNT -> """
            COALESCE(
                (SELECT pages FROM book_editions WHERE id = ub.editionId),
                (SELECT pages FROM book_editions WHERE id = b.defaultEditionId),
                0
            ) $dir
        """.trimIndent()

        LibrarySortMode.DEADLINE_URGENCY -> """
            COALESCE(
                (SELECT deadlineDate FROM book_deadlines WHERE bookId = b.id),
                '9999-12-31'
            ) $dir
        """.trimIndent()

        LibrarySortMode.RELEASE_DATE -> """
            COALESCE(
                (SELECT releaseDate FROM book_editions WHERE id = ub.editionId),
                (SELECT releaseDate FROM book_editions WHERE id = b.defaultEditionId),
                b.releaseDate,
                '9999-12-31'
            ) $dir
        """.trimIndent()

        // MANUAL needs an extra JOIN against shelf_manual_order, which the fragment shape can't
        // express. BooksLocalDataSource.getSortedBooksByStatus branches on MANUAL and builds the
        // full statement itself; this fragment path is unreachable for MANUAL.
        LibrarySortMode.MANUAL ->
            error("MANUAL sort cannot be expressed as a stand-alone ORDER BY fragment; the caller must build the full statement.")

        // ORDER is custom-list-only; book-tab SQL never sees it because the All/Status tabs don't
        // offer it in their sort allow-list. Treated as unreachable rather than a silent fallback.
        LibrarySortMode.ORDER ->
            error("ORDER sort is custom-list-only and must not reach the book-tab SQL path.")
    }
}

private fun SortDirection.sqlKeyword(): String = when (this) {
    SortDirection.ASCENDING -> "ASC"
    SortDirection.DESCENDING -> "DESC"
}
