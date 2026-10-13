package nl.rhaydus.softcover.feature.library.presentation.sort

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Book sort moved into SQL `ORDER BY` at the DAO layer (see `BooksLocalDataSource` +
 * `feature/books/data/sort/SortSql.kt`), so the in-memory book-sort tests are gone.
 * Edition sort still happens in memory — that path is exercised here.
 */
class LibrarySortTest {
    private fun buildEdition(
        id: Int = 1,
        pages: Int? = null,
        authors: List<Author> = emptyList(),
        title: String? = null,
        owned: Boolean = false,
    ) = BookEdition(
        id = id,
        canonicalId = null,
        bookId = 1,
        publisher = null,
        title = title,
        url = null,
        localImagePath = null,
        isbn10 = null,
        isbn13 = null,
        pages = pages,
        audioSeconds = null,
        authors = authors,
        releaseYear = 2020,
        releaseDate = null,
        format = "paperback",
        owned = owned,
    )

    @Nested
    inner class ApplyEditionSort {
        @Nested
        inner class Title {
            @Test
            fun `sorts editions by title ascending case-insensitively`() {
                // ----- Arrange -----
                val editions = listOf(
                    buildEdition(
                        id = 1,
                        title = "Zebra Edition",
                    ),
                    buildEdition(
                        id = 2,
                        title = "alpha Edition",
                    ),
                    buildEdition(
                        id = 3,
                        title = "Middle Edition",
                    ),
                )

                // ----- Act -----
                val result = editions.applyEditionSort(mode = LibrarySortMode.TITLE)

                // ----- Assert -----
                result.map { it.title } shouldBe listOf("alpha Edition", "Middle Edition", "Zebra Edition")
            }

            @Test
            fun `null title sorts before non-null`() {
                // ----- Arrange -----
                val editions = listOf(
                    buildEdition(
                        id = 1,
                        title = "Some Title",
                    ),
                    buildEdition(
                        id = 2,
                        title = null,
                    ),
                )

                // ----- Act -----
                val result = editions.applyEditionSort(mode = LibrarySortMode.TITLE)

                // ----- Assert -----
                result.map { it.id } shouldBe listOf(2, 1)
            }

            @Test
            fun `sorts editions by title descending when direction is DESCENDING`() {
                // ----- Arrange -----
                val editions = listOf(
                    buildEdition(
                        id = 1,
                        title = "alpha Edition",
                    ),
                    buildEdition(
                        id = 2,
                        title = "Middle Edition",
                    ),
                    buildEdition(
                        id = 3,
                        title = "Zebra Edition",
                    ),
                )

                // ----- Act -----
                val result = editions.applyEditionSort(
                    mode = LibrarySortMode.TITLE,
                    direction = SortDirection.DESCENDING,
                )

                // ----- Assert -----
                result.map { it.title } shouldBe listOf("Zebra Edition", "Middle Edition", "alpha Edition")
            }
        }

        @Nested
        inner class Author {
            @Test
            fun `sorts editions by first author name ascending`() {
                // ----- Arrange -----
                val editions = listOf(
                    buildEdition(
                        id = 1,
                        authors = listOf(
                            Author(
                                id = 1,
                                name = "Tolkien",
                            ),
                        ),
                    ),
                    buildEdition(
                        id = 2,
                        authors = listOf(
                            Author(
                                id = 2,
                                name = "Adams",
                            ),
                        ),
                    ),
                )

                // ----- Act -----
                val result = editions.applyEditionSort(mode = LibrarySortMode.AUTHOR)

                // ----- Assert -----
                result.map { it.id } shouldBe listOf(2, 1)
            }
        }

        @Nested
        inner class PageCount {
            @Test
            fun `sorts editions by page count descending`() {
                // ----- Arrange -----
                val editions = listOf(
                    buildEdition(
                        id = 1,
                        pages = 200,
                    ),
                    buildEdition(
                        id = 2,
                        pages = 600,
                    ),
                    buildEdition(
                        id = 3,
                        pages = null,
                    ),
                )

                // ----- Act -----
                val result = editions.applyEditionSort(mode = LibrarySortMode.PAGE_COUNT)

                // ----- Assert -----
                result.map { it.id } shouldBe listOf(2, 1, 3)
            }

            @Test
            fun `sorts editions by fewest pages first when direction is ASCENDING`() {
                // ----- Arrange -----
                val editions = listOf(
                    buildEdition(
                        id = 1,
                        pages = 600,
                    ),
                    buildEdition(
                        id = 2,
                        pages = 200,
                    ),
                )

                // ----- Act -----
                val result = editions.applyEditionSort(
                    mode = LibrarySortMode.PAGE_COUNT,
                    direction = SortDirection.ASCENDING,
                )

                // ----- Assert -----
                result.map { it.id } shouldBe listOf(2, 1)
            }
        }

        @Nested
        inner class ReleaseDate {
            @Test
            fun `sorts editions by release date ascending`() {
                // ----- Arrange -----
                val editions = listOf(
                    buildEdition(id = 1).copy(
                        releaseDate = LocalDate(
                            2023,
                            5,
                            1,
                        ),
                    ),
                    buildEdition(id = 2).copy(
                        releaseDate = LocalDate(
                            2021,
                            1,
                            15,
                        ),
                    ),
                    buildEdition(id = 3).copy(
                        releaseDate = LocalDate(
                            2022,
                            9,
                            30,
                        ),
                    ),
                )

                // ----- Act -----
                val result = editions.applyEditionSort(mode = LibrarySortMode.RELEASE_DATE)

                // ----- Assert -----
                result.map { it.id } shouldBe listOf(2, 3, 1)
            }

            @Test
            fun `edition with null release date sorts last`() {
                // ----- Arrange -----
                val withDate = buildEdition(id = 1).copy(
                    releaseDate = LocalDate(
                        2020,
                        1,
                        1,
                    ),
                )
                val nullDate = buildEdition(id = 2).copy(releaseDate = null)

                // ----- Act -----
                val result = listOf(nullDate, withDate).applyEditionSort(mode = LibrarySortMode.RELEASE_DATE)

                // ----- Assert -----
                result.map { it.id } shouldBe listOf(1, 2)
            }
        }

        @Nested
        inner class DateAdded {
            private val editions = listOf(
                buildEdition(id = 1),
                buildEdition(id = 2),
                buildEdition(id = 3),
            )

            @Test
            fun `default direction DESCENDING sorts newest-added first`() {
                // ----- Arrange -----
                val addedAt = mapOf(
                    1 to "2025-01-15T10:00:00Z",
                    2 to "2025-03-20T08:30:00Z",
                    3 to "2024-11-05T14:00:00Z",
                )

                // ----- Act -----
                val result = editions.applyEditionSort(
                    mode = LibrarySortMode.DATE_ADDED,
                    addedAtByEditionId = addedAt,
                )

                // ----- Assert -----
                result.map { it.id } shouldBe listOf(2, 1, 3)
            }

            @Test
            fun `ASCENDING direction sorts oldest-added first`() {
                // ----- Arrange -----
                val addedAt = mapOf(
                    1 to "2025-01-15T10:00:00Z",
                    2 to "2025-03-20T08:30:00Z",
                    3 to "2024-11-05T14:00:00Z",
                )

                // ----- Act -----
                val result = editions.applyEditionSort(
                    mode = LibrarySortMode.DATE_ADDED,
                    direction = SortDirection.ASCENDING,
                    addedAtByEditionId = addedAt,
                )

                // ----- Assert -----
                result.map { it.id } shouldBe listOf(3, 1, 2)
            }

            @Test
            fun `edition absent from map sorts last in ASCENDING`() {
                // ----- Arrange -----
                val addedAt = mapOf(
                    1 to "2025-01-15T10:00:00Z",
                    2 to "2025-03-20T08:30:00Z",
                )

                // ----- Act -----
                val result = editions.applyEditionSort(
                    mode = LibrarySortMode.DATE_ADDED,
                    direction = SortDirection.ASCENDING,
                    addedAtByEditionId = addedAt,
                )

                // ----- Assert -----
                result.map { it.id } shouldBe listOf(1, 2, 3)
            }

            @Test
            fun `edition absent from map sorts first in DESCENDING`() {
                // ----- Arrange -----
                val addedAt = mapOf(
                    1 to "2025-01-15T10:00:00Z",
                    2 to "2025-03-20T08:30:00Z",
                )

                // ----- Act -----
                val result = editions.applyEditionSort(
                    mode = LibrarySortMode.DATE_ADDED,
                    direction = SortDirection.DESCENDING,
                    addedAtByEditionId = addedAt,
                )

                // ----- Assert -----
                result.map { it.id } shouldBe listOf(3, 2, 1)
            }

            @Test
            fun `null addedAt value collapses to the missing-entry sentinel`() {
                // ----- Arrange -----
                val addedAt = mapOf<Int, String?>(
                    1 to "2025-01-15T10:00:00Z",
                    2 to null,
                    3 to "2025-02-01T10:00:00Z",
                )

                // ----- Act -----
                val result = editions.applyEditionSort(
                    mode = LibrarySortMode.DATE_ADDED,
                    direction = SortDirection.ASCENDING,
                    addedAtByEditionId = addedAt,
                )

                // ----- Assert -----
                result.map { it.id } shouldBe listOf(1, 3, 2)
            }
        }

        @Nested
        inner class UnchangedModes {
            @Test
            fun `modes without an in-memory edition sort return the list unchanged`() {
                // ----- Arrange -----
                val editions = listOf(buildEdition(id = 2), buildEdition(id = 1))
                val modes = listOf(
                    LibrarySortMode.DATE_FINISHED,
                    LibrarySortMode.RATING,
                    LibrarySortMode.PROGRESS,
                    LibrarySortMode.DEADLINE_URGENCY,
                )

                // ----- Act -----
                val results = modes.map { editions.applyEditionSort(mode = it) }

                // ----- Assert -----
                results shouldBe modes.map { editions }
            }
        }
    }
}
