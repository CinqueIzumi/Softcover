package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryFilterOptionsBuilderTest {
    private val tagFiction = Tag(
        id = 1,
        name = "Fiction",
        category = TagCategory.GENRE,
    )
    private val tagScifi = Tag(
        id = 2,
        name = "Sci-Fi",
        category = TagCategory.GENRE,
    )

    @Nested
    inner class BuildBookFilterOptions {
        @Test
        fun `returns empty options when book list is empty`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = buildBookFilterOptions(books = emptyList())

            // ----- Assert -----
            result.isEmpty shouldBe true
        }

        @Test
        fun `returns distinct tags sorted by name lowercase`() {
            // ----- Arrange -----
            val book1 = buildBook(
                id = 1,
                tags = listOf(tagScifi, tagFiction),
            )
            val book2 = buildBook(
                id = 2,
                tags = listOf(tagFiction),
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book1, book2))

            // ----- Assert -----
            result.tags.map { it.id } shouldBe listOf(tagFiction.id, tagScifi.id)
        }

        @Test
        fun `returns distinct non-blank formats sorted alphabetically`() {
            // ----- Arrange -----
            val book1 = buildBook(
                id = 1,
                editions = listOf(
                    buildEdition(
                        id = 1,
                        format = "paperback",
                    ),
                    buildEdition(
                        id = 2,
                        format = "ebook",
                    ),
                ),
            )
            val book2 = buildBook(
                id = 2,
                editions = listOf(buildEdition(
                    id = 3,
                    format = "paperback",
                ),),
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book1, book2))

            // ----- Assert -----
            result.formats shouldBe listOf("ebook", "paperback")
        }

        @Test
        fun `excludes blank formats`() {
            // ----- Arrange -----
            val book = buildBook(
                id = 1,
                editions = listOf(
                    buildEdition(
                        id = 1,
                        format = "",
                    ),
                    buildEdition(
                        id = 2,
                        format = "hardcover",
                    ),
                ),
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book))

            // ----- Assert -----
            result.formats shouldBe listOf("hardcover")
        }

        @Test
        fun `returns distinct positive release years sorted descending`() {
            // ----- Arrange -----
            val book1 = buildBook(
                id = 1,
                releaseYear = 2019,
            )
            val book2 = buildBook(
                id = 2,
                releaseYear = 2021,
            )
            val book3 = buildBook(
                id = 3,
                releaseYear = 2019,
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book1, book2, book3))

            // ----- Assert -----
            result.releaseYears shouldBe listOf(2021, 2019)
        }

        @Test
        fun `excludes release years that are zero or negative`() {
            // ----- Arrange -----
            val book1 = buildBook(
                id = 1,
                releaseYear = 2021,
            )
            val book2 = buildBook(
                id = 2,
                releaseYear = -1,
            )
            val book3 = buildBook(
                id = 3,
                releaseYear = 0,
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book1, book2, book3))

            // ----- Assert -----
            result.releaseYears shouldBe listOf(2021)
        }

        @Test
        fun `supportsOwnedFilter is true when a book has an owned edition`() {
            // ----- Arrange -----
            val book = buildBook(
                id = 1,
                editions = listOf(buildEdition(
                    id = 1,
                    owned = true,
                ),),
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book))

            // ----- Assert -----
            result.supportsOwnedFilter shouldBe true
        }

        @Test
        fun `supportsOwnedFilter is false when no book has an owned edition`() {
            // ----- Arrange -----
            val book = buildBook(
                id = 1,
                editions = listOf(buildEdition(
                    id = 1,
                    owned = false,
                ),),
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book))

            // ----- Assert -----
            result.supportsOwnedFilter shouldBe false
        }

        @Test
        fun `ratingBuckets only contains buckets at or below max rating`() {
            // ----- Arrange -----
            val book = buildBook(
                id = 1,
                rating = 4.0,
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book))

            // ----- Assert -----
            result.ratingBuckets shouldBe listOf(4.0, 3.5, 3.0)
        }

        @Test
        fun `ratingBuckets is empty when no book has a positive rating`() {
            // ----- Arrange -----
            val book = buildBook(
                id = 1,
                rating = 0.0,
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book))

            // ----- Assert -----
            result.ratingBuckets shouldBe emptyList()
        }

        @Test
        fun `excludes non-Genre tags from tag facets`() {
            // ----- Arrange -----
            val tagGenre = Tag(
                id = 1,
                name = "Fantasy",
                category = TagCategory.GENRE,
            )
            val tagMood = Tag(
                id = 3,
                name = "Hopeful",
                category = TagCategory.MOOD,
            )
            val tagWarning = Tag(
                id = 4,
                name = "Violence",
                category = TagCategory.CONTENT_WARNING,
            )

            val book = buildBook(
                id = 1,
                tags = listOf(tagGenre, tagMood, tagWarning),
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book))

            // ----- Assert -----
            result.tags.map { it.id } shouldBe listOf(tagGenre.id)
        }
    }

    @Nested
    inner class BuildEditionFilterOptions {
        @Test
        fun `returns empty options when edition list is empty`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = buildEditionFilterOptions(
                editions = emptyList(),
                bookByBookId = emptyMap(),
            )

            // ----- Assert -----
            result.isEmpty shouldBe true
        }

        @Test
        fun `tags sourced from parent books via bookByBookId`() {
            // ----- Arrange -----
            val edition = buildEdition(
                id = 1,
                bookId = 10,
            )
            val book = buildBook(
                id = 10,
                tags = listOf(tagFiction),
            )

            // ----- Act -----
            val result = buildEditionFilterOptions(
                editions = listOf(edition),
                bookByBookId = mapOf(10 to book),
            )

            // ----- Assert -----
            result.tags.map { it.id } shouldBe listOf(tagFiction.id)
        }

        @Test
        fun `excludes non-Genre tags from tag facets`() {
            // ----- Arrange -----
            val tagGenre = Tag(
                id = 1,
                name = "Fantasy",
                category = TagCategory.GENRE,
            )
            val tagMood = Tag(
                id = 3,
                name = "Hopeful",
                category = TagCategory.MOOD,
            )
            val tagWarning = Tag(
                id = 4,
                name = "Violence",
                category = TagCategory.CONTENT_WARNING,
            )

            val book = buildBook(
                id = 10,
                tags = listOf(tagGenre, tagMood, tagWarning),
            )
            val edition = buildEdition(
                id = 1,
                bookId = 10,
            )

            // ----- Act -----
            val result = buildEditionFilterOptions(
                editions = listOf(edition),
                bookByBookId = mapOf(10 to book),
            )

            // ----- Assert -----
            result.tags.map { it.id } shouldBe listOf(tagGenre.id)
        }

        @Test
        fun `formats sourced from editions themselves`() {
            // ----- Arrange -----
            val edition1 = buildEdition(
                id = 1,
                bookId = 10,
                format = "ebook",
            )
            val edition2 = buildEdition(
                id = 2,
                bookId = 10,
                format = "audiobook",
            )

            // ----- Act -----
            val result = buildEditionFilterOptions(
                editions = listOf(edition1, edition2),
                bookByBookId = emptyMap(),
            )

            // ----- Assert -----
            result.formats shouldBe listOf("audiobook", "ebook")
        }

        @Test
        fun `supportsOwnedFilter is true when any edition is owned`() {
            // ----- Arrange -----
            val edition = buildEdition(
                id = 1,
                owned = true,
            )

            // ----- Act -----
            val result = buildEditionFilterOptions(
                editions = listOf(edition),
                bookByBookId = emptyMap(),
            )

            // ----- Assert -----
            result.supportsOwnedFilter shouldBe true
        }

        @Test
        fun `release years sourced from parent books when present`() {
            // ----- Arrange -----
            val edition = buildEdition(
                id = 1,
                bookId = 10,
            )
            val book = buildBook(
                id = 10,
                releaseYear = 2022,
            )

            // ----- Act -----
            val result = buildEditionFilterOptions(
                editions = listOf(edition),
                bookByBookId = mapOf(10 to book),
            )

            // ----- Assert -----
            result.releaseYears shouldBe listOf(2022)
        }
    }

    private fun buildBook(
        id: Int,
        tags: List<Tag> = emptyList(),
        editions: List<BookEdition> = listOf(buildEdition(
            id = id,
            bookId = id,
        ),),
        releaseYear: Int = 2020,
        rating: Double = 0.0,
    ): Book = Book(
        id = id,
        title = "Book $id",
        editions = editions,
        defaultEdition = editions.firstOrNull(),
        rating = rating,
        description = "",
        releaseYear = releaseYear,
        coverUrl = "",
        authors = listOf(Author(
            id = id,
            name = "Author $id",
        ),),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        tags = tags,
        userBook = null,
        userBookRead = null,
    )

    private fun buildEdition(
        id: Int,
        bookId: Int = id,
        format: String = "",
        owned: Boolean = false,
    ): BookEdition = BookEdition(
        id = id,
        canonicalId = null,
        bookId = bookId,
        publisher = null,
        title = "Edition $id",
        url = null,
        localImagePath = null,
        isbn10 = null,
        isbn13 = null,
        pages = null,
        audioSeconds = null,
        authors = emptyList(),
        releaseYear = 2020,
        format = format,
        owned = owned,
    )
}
