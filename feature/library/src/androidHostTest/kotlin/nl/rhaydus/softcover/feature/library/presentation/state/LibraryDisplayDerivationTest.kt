package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.UserBook
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryDisplayDerivationTest {
    private fun buildEdition(
        id: Int = 1,
        bookId: Int = 1,
        title: String? = null,
        format: String = "paperback",
        owned: Boolean = false,
        pages: Int? = null,
        releaseYear: Int = 2020,
        authors: List<Author> = emptyList(),
    ) = BookEdition(
        id = id,
        canonicalId = null,
        bookId = bookId,
        publisher = null,
        title = title,
        url = null,
        localImagePath = null,
        isbn10 = null,
        isbn13 = null,
        pages = pages,
        audioSeconds = null,
        authors = authors,
        releaseYear = releaseYear,
        releaseDate = null,
        format = format,
        owned = owned,
    )

    private fun buildUserBook(lastReadDate: String? = null) = UserBook(
        id = 1,
        status = BookStatus.Read,
        dateAdded = "2024-01-01",
        createdAt = null,
        privacySettingId = 1,
        reviewHasSpoilers = false,
        editionId = null,
        lastReadDate = lastReadDate,
        rating = null,
        referrerUserId = null,
        reviewedAt = null,
        updatedAt = null,
        journals = emptyList(),
    )

    private fun buildBook(
        id: Int = 1,
        title: String = "A Book",
        authors: List<Author> = emptyList(),
        editions: List<BookEdition> = emptyList(),
        tags: List<Tag> = emptyList(),
        rating: Double = 0.0,
        releaseYear: Int = 2020,
        userBook: UserBook? = null,
    ) = Book(
        id = id,
        canonicalId = null,
        title = title,
        editions = editions,
        defaultEdition = null,
        rating = rating,
        description = "",
        releaseYear = releaseYear,
        releaseDate = null,
        coverUrl = "",
        authors = authors,
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        tags = tags,
        userBook = userBook,
        userBookRead = null,
    )

    @Nested
    inner class ComputeDisplayBooks {
        @Test
        fun `empty query returns raw list unfiltered`() {
            // ----- Arrange -----
            val books = listOf(
                buildBook(
                    id = 1,
                    title = "Kotlin in Action",
                ),
                buildBook(
                    id = 2,
                    title = "Clean Code",
                ),
            )

            // ----- Act -----
            val result = computeDisplayBooks(
                raw = books,
                query = "",
                filters = LibraryFilters(),
            )

            // ----- Assert -----
            result shouldBe books
        }

        @Test
        fun `whitespace-only query is treated as empty and returns raw list unfiltered`() {
            // ----- Arrange -----
            val books = listOf(
                buildBook(
                    id = 1,
                    title = "Kotlin in Action",
                ),
                buildBook(
                    id = 2,
                    title = "Clean Code",
                ),
            )

            // ----- Act -----
            val result = computeDisplayBooks(
                raw = books,
                query = "   ",
                filters = LibraryFilters(),
            )

            // ----- Assert -----
            result shouldBe books
        }

        @Test
        fun `title match is case-insensitive`() {
            // ----- Arrange -----
            val matching = buildBook(
                id = 1,
                title = "Kotlin in Action",
            )
            val nonMatching = buildBook(
                id = 2,
                title = "Clean Code",
            )

            // ----- Act -----
            val result = computeDisplayBooks(
                raw = listOf(matching, nonMatching),
                query = "kotlin",
                filters = LibraryFilters(),
            )

            // ----- Assert -----
            result shouldBe listOf(matching)
        }

        @Test
        fun `author name match is case-insensitive`() {
            // ----- Arrange -----
            val author = Author(
                id = 1,
                name = "Robert C. Martin",
            )
            val matching = buildBook(
                id = 1,
                title = "Clean Code",
                authors = listOf(author),
            )
            val nonMatching = buildBook(
                id = 2,
                title = "Other Book",
                authors = emptyList(),
            )

            // ----- Act -----
            val result = computeDisplayBooks(
                raw = listOf(matching, nonMatching),
                query = "ROBERT",
                filters = LibraryFilters(),
            )

            // ----- Assert -----
            result shouldBe listOf(matching)
        }

        @Test
        fun `non-matching query returns empty list`() {
            // ----- Arrange -----
            val books = listOf(
                buildBook(
                    id = 1,
                    title = "Kotlin in Action",
                ),
                buildBook(
                    id = 2,
                    title = "Clean Code",
                ),
            )

            // ----- Act -----
            val result = computeDisplayBooks(
                raw = books,
                query = "xyzzy",
                filters = LibraryFilters(),
            )

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `null readYear on filters skips year narrowing`() {
            // ----- Arrange -----
            val book2023 = buildBook(
                id = 1,
                userBook = buildUserBook(lastReadDate = "2023-06-15"),
            )
            val book2022 = buildBook(
                id = 2,
                userBook = buildUserBook(lastReadDate = "2022-06-15"),
            )

            // ----- Act -----
            val result = computeDisplayBooks(
                raw = listOf(book2023, book2022),
                query = "",
                filters = LibraryFilters(readYear = null),
            )

            // ----- Assert -----
            result shouldBe listOf(book2023, book2022)
        }

        @Test
        fun `readYear on filters keeps only books whose finishedYear matches`() {
            // ----- Arrange -----
            val book2023 = buildBook(
                id = 1,
                userBook = buildUserBook(lastReadDate = "2023-06-15"),
            )
            val book2022 = buildBook(
                id = 2,
                userBook = buildUserBook(lastReadDate = "2022-06-15"),
            )
            val bookNoDate = buildBook(
                id = 3,
                userBook = null,
            )

            // ----- Act -----
            val result = computeDisplayBooks(
                raw = listOf(book2023, book2022, bookNoDate),
                query = "",
                filters = LibraryFilters(readYear = 2023),
            )

            // ----- Assert -----
            result shouldBe listOf(book2023)
        }

        @Test
        fun `non-empty filters keep only books matching all active facets`() {
            // ----- Arrange -----
            val ebookEdition = buildEdition(
                id = 10,
                format = "ebook",
            )
            val paperbackEdition = buildEdition(
                id = 11,
                format = "paperback",
            )
            val ebookBook = buildBook(
                id = 1,
                editions = listOf(ebookEdition),
            )
            val paperbackBook = buildBook(
                id = 2,
                editions = listOf(paperbackEdition),
            )

            // ----- Act -----
            val result = computeDisplayBooks(
                raw = listOf(ebookBook, paperbackBook),
                query = "",
                filters = LibraryFilters(formats = setOf("ebook")),
            )

            // ----- Assert -----
            result shouldBe listOf(ebookBook)
        }

        @Test
        fun `search, readYear and tag filters are all applied together`() {
            // ----- Arrange -----
            val tagFiction = Tag(
                id = 1,
                name = "Fiction",
            )
            val matchingBook = buildBook(
                id = 1,
                title = "Kotlin Novel",
                tags = listOf(tagFiction),
                userBook = buildUserBook(lastReadDate = "2023-03-01"),
            )
            val wrongYearBook = buildBook(
                id = 2,
                title = "Kotlin Guide",
                tags = listOf(tagFiction),
                userBook = buildUserBook(lastReadDate = "2022-03-01"),
            )
            val wrongTagBook = buildBook(
                id = 3,
                title = "Kotlin Tips",
                tags = emptyList(),
                userBook = buildUserBook(lastReadDate = "2023-04-01"),
            )

            // ----- Act -----
            val result = computeDisplayBooks(
                raw = listOf(matchingBook, wrongYearBook, wrongTagBook),
                query = "kotlin",
                filters = LibraryFilters(
                    tags = setOf(tagFiction),
                    readYear = 2023,
                ),
            )

            // ----- Assert -----
            result shouldBe listOf(matchingBook)
        }
    }

    @Nested
    inner class ComputeDisplayEditions {
        @Test
        fun `empty query returns editions sorted by mode`() {
            // ----- Arrange -----
            val editionB = buildEdition(
                id = 1,
                title = "Banana",
            )
            val editionA = buildEdition(
                id = 2,
                title = "Apple",
            )

            // ----- Act -----
            val result = computeDisplayEditions(
                raw = listOf(editionB, editionA),
                query = "",
                mode = LibrarySortMode.TITLE,
                direction = SortDirection.ASCENDING,
                addedAtByEditionId = emptyMap(),
                filters = LibraryFilters(),
                bookByBookId = emptyMap(),
            )

            // ----- Assert -----
            result.map { it.title } shouldBe listOf("Apple", "Banana")
        }

        @Test
        fun `title match is case-insensitive`() {
            // ----- Arrange -----
            val matching = buildEdition(
                id = 1,
                title = "Kotlin in Action",
            )
            val nonMatching = buildEdition(
                id = 2,
                title = "Clean Code",
            )

            // ----- Act -----
            val result = computeDisplayEditions(
                raw = listOf(matching, nonMatching),
                query = "kotlin",
                mode = LibrarySortMode.TITLE,
                direction = SortDirection.ASCENDING,
                addedAtByEditionId = emptyMap(),
                filters = LibraryFilters(),
                bookByBookId = emptyMap(),
            )

            // ----- Assert -----
            result shouldBe listOf(matching)
        }

        @Test
        fun `author name match is case-insensitive`() {
            // ----- Arrange -----
            val author = Author(
                id = 1,
                name = "Martin Fowler",
            )
            val matching = buildEdition(
                id = 1,
                title = "Refactoring",
                authors = listOf(author),
            )
            val nonMatching = buildEdition(
                id = 2,
                title = "Other Book",
            )

            // ----- Act -----
            val result = computeDisplayEditions(
                raw = listOf(matching, nonMatching),
                query = "MARTIN",
                mode = LibrarySortMode.TITLE,
                direction = SortDirection.ASCENDING,
                addedAtByEditionId = emptyMap(),
                filters = LibraryFilters(),
                bookByBookId = emptyMap(),
            )

            // ----- Assert -----
            result shouldBe listOf(matching)
        }

        @Test
        fun `null title does not match a non-empty query`() {
            // ----- Arrange -----
            val nullTitleEdition = buildEdition(
                id = 1,
                title = null,
            )

            // ----- Act -----
            val result = computeDisplayEditions(
                raw = listOf(nullTitleEdition),
                query = "kotlin",
                mode = LibrarySortMode.TITLE,
                direction = SortDirection.ASCENDING,
                addedAtByEditionId = emptyMap(),
                filters = LibraryFilters(),
                bookByBookId = emptyMap(),
            )

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `TITLE DESCENDING sort orders editions in reverse alphabetical order`() {
            // ----- Arrange -----
            val editionC = buildEdition(
                id = 1,
                title = "Cherry",
            )
            val editionA = buildEdition(
                id = 2,
                title = "Apple",
            )
            val editionB = buildEdition(
                id = 3,
                title = "Banana",
            )

            // ----- Act -----
            val result = computeDisplayEditions(
                raw = listOf(editionC, editionA, editionB),
                query = "",
                mode = LibrarySortMode.TITLE,
                direction = SortDirection.DESCENDING,
                addedAtByEditionId = emptyMap(),
                filters = LibraryFilters(),
                bookByBookId = emptyMap(),
            )

            // ----- Assert -----
            result.map { it.title } shouldBe listOf("Cherry", "Banana", "Apple")
        }

        @Test
        fun `non-empty filters keep only editions matching active facets`() {
            // ----- Arrange -----
            val ownedEdition = buildEdition(
                id = 1,
                title = "Owned",
                owned = true,
            )
            val unownedEdition = buildEdition(
                id = 2,
                title = "Unowned",
                owned = false,
            )

            // ----- Act -----
            val result = computeDisplayEditions(
                raw = listOf(ownedEdition, unownedEdition),
                query = "",
                mode = LibrarySortMode.TITLE,
                direction = SortDirection.ASCENDING,
                addedAtByEditionId = emptyMap(),
                filters = LibraryFilters(owned = true),
                bookByBookId = emptyMap(),
            )

            // ----- Assert -----
            result shouldBe listOf(ownedEdition)
        }

        @Test
        fun `matchesEdition resolves book-level facets via bookByBookId lookup`() {
            // ----- Arrange -----
            val tagScifi = Tag(
                id = 1,
                name = "Sci-Fi",
            )
            val scifiBook = buildBook(
                id = 100,
                tags = listOf(tagScifi),
            )
            val nonScifiBook = buildBook(
                id = 200,
                tags = emptyList(),
            )

            val scifiEdition = buildEdition(
                id = 1,
                bookId = 100,
                title = "Sci-Fi Ed",
            )
            val nonScifiEdition = buildEdition(
                id = 2,
                bookId = 200,
                title = "Other Ed",
            )

            // ----- Act -----
            val result = computeDisplayEditions(
                raw = listOf(scifiEdition, nonScifiEdition),
                query = "",
                mode = LibrarySortMode.TITLE,
                direction = SortDirection.ASCENDING,
                addedAtByEditionId = emptyMap(),
                filters = LibraryFilters(tags = setOf(tagScifi)),
                bookByBookId = mapOf(100 to scifiBook, 200 to nonScifiBook),
            )

            // ----- Assert -----
            result shouldBe listOf(scifiEdition)
        }

        @Test
        fun `editions with unknown bookId are treated as having no book for filter purposes`() {
            // ----- Arrange -----
            val tagScifi = Tag(
                id = 1,
                name = "Sci-Fi",
            )
            val orphanEdition = buildEdition(
                id = 1,
                bookId = 999,
                title = "Orphan",
            )

            // ----- Act -----
            val result = computeDisplayEditions(
                raw = listOf(orphanEdition),
                query = "",
                mode = LibrarySortMode.TITLE,
                direction = SortDirection.ASCENDING,
                addedAtByEditionId = emptyMap(),
                filters = LibraryFilters(tags = setOf(tagScifi)),
                bookByBookId = emptyMap(),
            )

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class LibraryPreviewCount {
        @Test
        fun `book-tab path counts items matching the draft filters`() {
            // ----- Arrange -----
            val tabId = "status-read"
            val ebookBook = buildBook(
                id = 1,
                editions = listOf(
                    buildEdition(
                        id = 10,
                        format = "ebook",
                    ),
                ),
            )
            val paperbackBook = buildBook(
                id = 2,
                editions = listOf(
                    buildEdition(
                        id = 11,
                        format = "paperback",
                    ),
                ),
            )

            val state = LibraryUiState(
                booksByTab = mapOf(tabId to listOf(ebookBook, paperbackBook)),
            )

            // ----- Act -----
            val result = libraryPreviewCount(
                state = state,
                tabId = tabId,
                draftFilters = LibraryFilters(formats = setOf("ebook")),
            )

            // ----- Assert -----
            result shouldBe 1
        }

        @Test
        fun `book-tab path combines the committed search query with the draft filters`() {
            // ----- Arrange -----
            val tabId = "status-read"
            val matching = buildBook(
                id = 1,
                title = "Kotlin Novel",
                editions = listOf(
                    buildEdition(
                        id = 10,
                        format = "ebook",
                    ),
                ),
            )
            val wrongFormat = buildBook(
                id = 2,
                title = "Kotlin Guide",
                editions = listOf(
                    buildEdition(
                        id = 11,
                        format = "paperback",
                    ),
                ),
            )
            val wrongTitle = buildBook(
                id = 3,
                title = "Other Book",
                editions = listOf(
                    buildEdition(
                        id = 12,
                        format = "ebook",
                    ),
                ),
            )

            val state = LibraryUiState(
                booksByTab = mapOf(tabId to listOf(matching, wrongFormat, wrongTitle)),
                searchQuery = "kotlin",
            )

            // ----- Act -----
            val result = libraryPreviewCount(
                state = state,
                tabId = tabId,
                draftFilters = LibraryFilters(formats = setOf("ebook")),
            )

            // ----- Assert -----
            result shouldBe 1
        }

        @Test
        fun `custom-list edition-tab path counts editions matching the draft filters`() {
            // ----- Arrange -----
            val tabId = "list-5"
            val ownedEdition = buildEdition(
                id = 1,
                title = "Owned Ed",
                owned = true,
            )
            val unownedEdition = buildEdition(
                id = 2,
                title = "Unowned Ed",
                owned = false,
            )

            val state = LibraryUiState(
                editionsByTab = mapOf(tabId to listOf(ownedEdition, unownedEdition)),
            )

            // ----- Act -----
            val result = libraryPreviewCount(
                state = state,
                tabId = tabId,
                draftFilters = LibraryFilters(owned = true),
            )

            // ----- Assert -----
            result shouldBe 1
        }

        @Test
        fun `unknown tabId absent from both books and editions returns 0`() {
            // ----- Arrange -----
            val state = LibraryUiState(
                booksByTab = emptyMap(),
                editionsByTab = emptyMap(),
            )

            // ----- Act -----
            val result = libraryPreviewCount(
                state = state,
                tabId = "unknown-tab",
                draftFilters = LibraryFilters(),
            )

            // ----- Assert -----
            result shouldBe 0
        }

        @Test
        fun `book-tab with an empty collected list returns 0 without falling through to editions`() {
            // ----- Arrange -----
            val tabId = "status-read"
            val state = LibraryUiState(
                booksByTab = mapOf(tabId to emptyList()),
                editionsByTab = mapOf(tabId to listOf(buildEdition(id = 1))),
            )

            // ----- Act -----
            val result = libraryPreviewCount(
                state = state,
                tabId = tabId,
                draftFilters = LibraryFilters(),
            )

            // ----- Assert -----
            result shouldBe 0
        }

        @Test
        fun `reads the draft filters rather than the tab's committed filtersByTab entry`() {
            // ----- Arrange -----
            val tabId = "status-read"
            val ebookBook = buildBook(
                id = 1,
                editions = listOf(
                    buildEdition(
                        id = 10,
                        format = "ebook",
                    ),
                ),
            )
            val paperbackBook = buildBook(
                id = 2,
                editions = listOf(
                    buildEdition(
                        id = 11,
                        format = "paperback",
                    ),
                ),
            )
            val audiobookBook = buildBook(
                id = 3,
                editions = listOf(
                    buildEdition(
                        id = 12,
                        format = "audiobook",
                    ),
                ),
            )

            // Committed filters would narrow to just the paperback book (count 1); the draft below
            // is empty, so the correct result is all 3 books. Asserting 3 proves committed filters
            // were not consulted.
            val committedFilters = LibraryFilters(formats = setOf("paperback"))

            val state = LibraryUiState(
                booksByTab = mapOf(tabId to listOf(ebookBook, paperbackBook, audiobookBook)),
                filtersByTab = mapOf(tabId to committedFilters),
            )

            // ----- Act -----
            val result = libraryPreviewCount(
                state = state,
                tabId = tabId,
                draftFilters = LibraryFilters(),
            )

            // ----- Assert -----
            result shouldBe 3
        }
    }
}
