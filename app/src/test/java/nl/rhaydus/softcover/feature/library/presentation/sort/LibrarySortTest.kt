package nl.rhaydus.softcover.feature.library.presentation.sort

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

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
                val editions = listOf(
                    buildEdition(id = 1, title = "Zebra Edition"),
                    buildEdition(id = 2, title = "alpha Edition"),
                    buildEdition(id = 3, title = "Middle Edition"),
                )

                val result = editions.applyEditionSort(mode = LibrarySortMode.TITLE)

                result[0].title shouldBe "alpha Edition"
                result[1].title shouldBe "Middle Edition"
                result[2].title shouldBe "Zebra Edition"
            }

            @Test
            fun `null title sorts before non-null`() {
                val editions = listOf(
                    buildEdition(id = 1, title = "Some Title"),
                    buildEdition(id = 2, title = null),
                )

                val result = editions.applyEditionSort(mode = LibrarySortMode.TITLE)

                result[0].id shouldBe 2
                result[1].id shouldBe 1
            }

            @Test
            fun `sorts editions by title descending when direction is DESCENDING`() {
                val editions = listOf(
                    buildEdition(id = 1, title = "alpha Edition"),
                    buildEdition(id = 2, title = "Middle Edition"),
                    buildEdition(id = 3, title = "Zebra Edition"),
                )

                val result = editions.applyEditionSort(
                    mode = LibrarySortMode.TITLE,
                    direction = SortDirection.DESCENDING,
                )

                result[0].title shouldBe "Zebra Edition"
                result[1].title shouldBe "Middle Edition"
                result[2].title shouldBe "alpha Edition"
            }
        }

        @Nested
        inner class Author {

            @Test
            fun `sorts editions by first author name ascending`() {
                val editions = listOf(
                    buildEdition(id = 1, authors = listOf(Author(id = 1, name = "Tolkien"))),
                    buildEdition(id = 2, authors = listOf(Author(id = 2, name = "Adams"))),
                )

                val result = editions.applyEditionSort(mode = LibrarySortMode.AUTHOR)

                result[0].id shouldBe 2
                result[1].id shouldBe 1
            }

            @Test
            fun `sorts editions by first author name descending when direction is DESCENDING`() {
                val editions = listOf(
                    buildEdition(id = 1, authors = listOf(Author(id = 1, name = "Adams"))),
                    buildEdition(id = 2, authors = listOf(Author(id = 2, name = "Tolkien"))),
                )

                val result = editions.applyEditionSort(
                    mode = LibrarySortMode.AUTHOR,
                    direction = SortDirection.DESCENDING,
                )

                result[0].id shouldBe 2
                result[1].id shouldBe 1
            }
        }

        @Nested
        inner class PageCount {

            @Test
            fun `sorts editions by page count descending`() {
                val editions = listOf(
                    buildEdition(id = 1, pages = 200),
                    buildEdition(id = 2, pages = 600),
                    buildEdition(id = 3, pages = null),
                )

                val result = editions.applyEditionSort(mode = LibrarySortMode.PAGE_COUNT)

                result[0].id shouldBe 2
                result[1].id shouldBe 1
                result[2].id shouldBe 3
            }

            @Test
            fun `sorts editions by fewest pages first when direction is ASCENDING`() {
                val editions = listOf(
                    buildEdition(id = 1, pages = 600),
                    buildEdition(id = 2, pages = 200),
                )

                val result = editions.applyEditionSort(
                    mode = LibrarySortMode.PAGE_COUNT,
                    direction = SortDirection.ASCENDING,
                )

                result[0].id shouldBe 2
                result[1].id shouldBe 1
            }
        }

        @Nested
        inner class ReleaseDate {

            @Test
            fun `sorts editions by release date ascending`() {
                val editions = listOf(
                    buildEdition(id = 1).copy(releaseDate = LocalDate.of(2023, 5, 1)),
                    buildEdition(id = 2).copy(releaseDate = LocalDate.of(2021, 1, 15)),
                    buildEdition(id = 3).copy(releaseDate = LocalDate.of(2022, 9, 30)),
                )

                val result = editions.applyEditionSort(mode = LibrarySortMode.RELEASE_DATE)

                result[0].id shouldBe 2
                result[1].id shouldBe 3
                result[2].id shouldBe 1
            }

            @Test
            fun `edition with null release date sorts last`() {
                val withDate = buildEdition(id = 1).copy(releaseDate = LocalDate.of(2020, 1, 1))
                val nullDate = buildEdition(id = 2).copy(releaseDate = null)

                val editions = listOf(nullDate, withDate)

                val result = editions.applyEditionSort(mode = LibrarySortMode.RELEASE_DATE)

                result[0].id shouldBe 1
                result[1].id shouldBe 2
            }

            @Test
            fun `sorts editions by newest release date first when direction is DESCENDING`() {
                val editions = listOf(
                    buildEdition(id = 1).copy(releaseDate = LocalDate.of(2021, 1, 15)),
                    buildEdition(id = 2).copy(releaseDate = LocalDate.of(2023, 5, 1)),
                    buildEdition(id = 3).copy(releaseDate = LocalDate.of(2022, 9, 30)),
                )

                val result = editions.applyEditionSort(
                    mode = LibrarySortMode.RELEASE_DATE,
                    direction = SortDirection.DESCENDING,
                )

                result[0].id shouldBe 2
                result[1].id shouldBe 3
                result[2].id shouldBe 1
            }
        }

        @Nested
        inner class DateAdded {

            @Test
            fun `default direction DESCENDING sorts newest-added first`() {
                val editions = listOf(
                    buildEdition(id = 1),
                    buildEdition(id = 2),
                    buildEdition(id = 3),
                )

                val addedAt = mapOf(
                    1 to "2025-01-15T10:00:00Z",
                    2 to "2025-03-20T08:30:00Z",
                    3 to "2024-11-05T14:00:00Z",
                )

                val result = editions.applyEditionSort(
                    mode = LibrarySortMode.DATE_ADDED,
                    addedAtByEditionId = addedAt,
                )

                result[0].id shouldBe 2
                result[1].id shouldBe 1
                result[2].id shouldBe 3
            }

            @Test
            fun `ASCENDING direction sorts oldest-added first`() {
                val editions = listOf(
                    buildEdition(id = 1),
                    buildEdition(id = 2),
                    buildEdition(id = 3),
                )

                val addedAt = mapOf(
                    1 to "2025-01-15T10:00:00Z",
                    2 to "2025-03-20T08:30:00Z",
                    3 to "2024-11-05T14:00:00Z",
                )

                val result = editions.applyEditionSort(
                    mode = LibrarySortMode.DATE_ADDED,
                    direction = SortDirection.ASCENDING,
                    addedAtByEditionId = addedAt,
                )

                result[0].id shouldBe 3
                result[1].id shouldBe 1
                result[2].id shouldBe 2
            }

            @Test
            fun `edition absent from map sorts last in ASCENDING`() {
                val editions = listOf(
                    buildEdition(id = 1),
                    buildEdition(id = 2),
                    buildEdition(id = 3),
                )

                val addedAt = mapOf(
                    1 to "2025-01-15T10:00:00Z",
                    2 to "2025-03-20T08:30:00Z",
                )

                val result = editions.applyEditionSort(
                    mode = LibrarySortMode.DATE_ADDED,
                    direction = SortDirection.ASCENDING,
                    addedAtByEditionId = addedAt,
                )

                result[0].id shouldBe 1
                result[1].id shouldBe 2
                result[2].id shouldBe 3
            }

            @Test
            fun `edition absent from map sorts first in DESCENDING`() {
                val editions = listOf(
                    buildEdition(id = 1),
                    buildEdition(id = 2),
                    buildEdition(id = 3),
                )

                val addedAt = mapOf(
                    1 to "2025-01-15T10:00:00Z",
                    2 to "2025-03-20T08:30:00Z",
                )

                val result = editions.applyEditionSort(
                    mode = LibrarySortMode.DATE_ADDED,
                    direction = SortDirection.DESCENDING,
                    addedAtByEditionId = addedAt,
                )

                result[0].id shouldBe 3
            }

            @Test
            fun `null addedAt value collapses to the missing-entry sentinel`() {
                val editions = listOf(
                    buildEdition(id = 1),
                    buildEdition(id = 2),
                )

                val addedAt = mapOf<Int, String?>(
                    1 to "2025-01-15T10:00:00Z",
                    2 to null,
                )

                val result = editions.applyEditionSort(
                    mode = LibrarySortMode.DATE_ADDED,
                    direction = SortDirection.ASCENDING,
                    addedAtByEditionId = addedAt,
                )

                result[0].id shouldBe 1
                result[1].id shouldBe 2
            }

            @Test
            fun `empty addedAtByEditionId map with ASCENDING preserves input order`() {
                val editions = listOf(buildEdition(id = 1), buildEdition(id = 2))

                val result = editions.applyEditionSort(
                    mode = LibrarySortMode.DATE_ADDED,
                    direction = SortDirection.ASCENDING,
                    addedAtByEditionId = emptyMap(),
                )

                result[0].id shouldBe 1
                result[1].id shouldBe 2
            }
        }

        @Nested
        inner class UnchangedModes {

            @Test
            fun `DATE_FINISHED returns list unchanged`() {
                val editions = listOf(buildEdition(id = 1), buildEdition(id = 2))

                val result = editions.applyEditionSort(mode = LibrarySortMode.DATE_FINISHED)

                result shouldBe editions
            }

            @Test
            fun `RATING returns list unchanged`() {
                val editions = listOf(buildEdition(id = 1), buildEdition(id = 2))

                val result = editions.applyEditionSort(mode = LibrarySortMode.RATING)

                result shouldBe editions
            }

            @Test
            fun `PROGRESS returns list unchanged`() {
                val editions = listOf(buildEdition(id = 1), buildEdition(id = 2))

                val result = editions.applyEditionSort(mode = LibrarySortMode.PROGRESS)

                result shouldBe editions
            }

            @Test
            fun `DEADLINE_URGENCY returns list unchanged`() {
                val editions = listOf(buildEdition(id = 1), buildEdition(id = 2))

                val result = editions.applyEditionSort(mode = LibrarySortMode.DEADLINE_URGENCY)

                result shouldBe editions
            }
        }
    }
}
