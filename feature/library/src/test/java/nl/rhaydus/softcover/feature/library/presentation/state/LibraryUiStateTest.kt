package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.designsystem.presentation.model.LibraryTab
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.JournalEventType
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.ReadingJournal
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryUiStateTest {
    // region Fixtures
    private val readTabId = LibraryTab.Status.of(UserBookStatus.READ).id
    private val otherTabId = "list-99"

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

    private fun buildUserBook(
        dateAdded: String = "2024-01-01",
        journals: List<ReadingJournal> = emptyList(),
    ) = UserBook(
        id = 1,
        status = BookStatus.Read,
        dateAdded = dateAdded,
        createdAt = null,
        privacySettingId = 1,
        reviewHasSpoilers = false,
        editionId = null,
        lastReadDate = null,
        rating = null,
        referrerUserId = null,
        reviewedAt = null,
        updatedAt = null,
        journals = journals,
    )

    private fun buildFinishedJournal(updatedAt: String) = ReadingJournal(
        updatedAt = updatedAt,
        event = JournalEventType.StatusFinished.eventName,
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

    private fun buildEditionFull(
        id: Int = 1,
        bookId: Int = 1,
        format: String = "paperback",
        owned: Boolean = false,
        releaseYear: Int = 2020,
    ) = BookEdition(
        id = id,
        canonicalId = null,
        bookId = bookId,
        publisher = null,
        title = null,
        url = null,
        localImagePath = null,
        isbn10 = null,
        isbn13 = null,
        pages = null,
        audioSeconds = null,
        authors = emptyList(),
        releaseYear = releaseYear,
        releaseDate = null,
        format = format,
        owned = owned,
    )

    private fun buildState(
        booksByTab: Map<String, List<Book>> = emptyMap(),
        editionsByTab: Map<String, List<BookEdition>> = emptyMap(),
        bookByBookId: Map<Int, Book> = emptyMap(),
        searchQuery: String = "",
        selectedReadYear: Int? = null,
        sortModeByTab: Map<String, LibrarySortMode> = emptyMap(),
        sortDirectionByTab: Map<String, SortDirection> = emptyMap(),
        filtersByTab: Map<String, LibraryFilters> = emptyMap(),
        filterOptionsByTab: Map<String, LibraryFilterOptions> = emptyMap(),
    ) = LibraryUiState(
        booksByTab = booksByTab,
        editionsByTab = editionsByTab,
        bookByBookId = bookByBookId,
        searchQuery = searchQuery,
        selectedReadYear = selectedReadYear,
        sortModeByTab = sortModeByTab,
        sortDirectionByTab = sortDirectionByTab,
        filtersByTab = filtersByTab,
        filterOptionsByTab = filterOptionsByTab,
    )
    // endregion
    @Nested
    inner class Defaults {
        @Test
        fun `tabsLoaded is false in the initial state before any flows emit`() {
            // ----- Arrange & Act -----
            val state = LibraryUiState()

            // ----- Assert -----
            state.tabsLoaded shouldBe false
        }
    }

    @Nested
    inner class SortDirectionFor {
        @Test
        fun `falls back to the mode's defaultDirection when not set explicitly`() {
            // ----- Arrange -----
            val state = buildState(
                sortModeByTab = mapOf(otherTabId to LibrarySortMode.DATE_ADDED),
                sortDirectionByTab = emptyMap(),
            )

            // ----- Act -----
            val result = state.sortDirectionFor(otherTabId)

            // ----- Assert -----
            result shouldBe SortDirection.DESCENDING
        }

        @Test
        fun `returns the persisted direction when set explicitly`() {
            // ----- Arrange -----
            val state = buildState(
                sortModeByTab = mapOf(otherTabId to LibrarySortMode.DATE_ADDED),
                sortDirectionByTab = mapOf(otherTabId to SortDirection.ASCENDING),
            )

            // ----- Act -----
            val result = state.sortDirectionFor(otherTabId)

            // ----- Assert -----
            result shouldBe SortDirection.ASCENDING
        }
    }

    @Nested
    inner class SortModeFor {
        @Test
        fun `returns DATE_FINISHED for the Read tab when sortModeByTab is empty`() {
            // ----- Arrange -----
            val state = buildState(sortModeByTab = emptyMap())

            // ----- Act -----
            val result = state.sortModeFor(readTabId)

            // ----- Assert -----
            result shouldBe LibrarySortMode.DATE_FINISHED
        }

        @Test
        fun `returns DATE_ADDED for a non-Read tab when sortModeByTab is empty`() {
            // ----- Arrange -----
            val state = buildState(sortModeByTab = emptyMap())

            // ----- Act -----
            val result = state.sortModeFor(otherTabId)

            // ----- Assert -----
            result shouldBe LibrarySortMode.Default
        }
    }

    @Nested
    inner class DisplayBooksFor {
        @Test
        fun `returns null when tabId is absent from booksByTab`() {
            // ----- Arrange -----
            val state = buildState(booksByTab = emptyMap())

            // ----- Act -----
            val result = state.displayBooksFor(otherTabId)

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns booksByTab list in DAO-provided order when search query is blank`() {
            // Book sort moved to SQL ORDER BY at the DAO layer, so displayBooksFor no longer
            // re-sorts — it just passes the pre-sorted list through (after applying search +
            // year filters). The list arrives in whatever order the DAO emitted it.
            // ----- Arrange -----
            val newer = buildBook(
                id = 1,
                userBook = buildUserBook(dateAdded = "2024-06-01"),
            )
            val older = buildBook(
                id = 2,
                userBook = buildUserBook(dateAdded = "2023-01-01"),
            )

            val state = buildState(
                booksByTab = mapOf(otherTabId to listOf(newer, older)),
                sortModeByTab = mapOf(otherTabId to LibrarySortMode.DATE_ADDED),
            )

            // ----- Act -----
            val result = state.displayBooksFor(otherTabId)

            // ----- Assert -----
            result!!.map { it.id } shouldBe listOf(1, 2)
        }

        @Test
        fun `filters by title case-insensitively when query is non-blank`() {
            // ----- Arrange -----
            val match = buildBook(
                id = 1,
                title = "Foundation",
            )
            val noMatch1 = buildBook(
                id = 2,
                title = "Dune",
            )
            val noMatch2 = buildBook(
                id = 3,
                title = "Neuromancer",
            )

            val state = buildState(
                booksByTab = mapOf(otherTabId to listOf(match, noMatch1, noMatch2)),
                searchQuery = "foundation",
            )

            // ----- Act -----
            val result = state.displayBooksFor(otherTabId)

            // ----- Assert -----
            result!!.map { it.id } shouldBe listOf(1)
        }

        @Test
        fun `filters by author name case-insensitively when no title matches`() {
            // ----- Arrange -----
            val matchByAuthor = buildBook(
                id = 1,
                title = "Foundation",
                authors = listOf(Author(
                    id = 1,
                    name = "Isaac Asimov",
                ),),
            )
            val noMatch = buildBook(
                id = 2,
                title = "Dune",
                authors = listOf(Author(
                    id = 2,
                    name = "Frank Herbert",
                ),),
            )

            val state = buildState(
                booksByTab = mapOf(otherTabId to listOf(matchByAuthor, noMatch)),
                searchQuery = "asimov",
            )

            // ----- Act -----
            val result = state.displayBooksFor(otherTabId)

            // ----- Assert -----
            result!!.map { it.id } shouldBe listOf(1)
        }

        @Test
        fun `Read tab filters by selectedReadYear when non-null`() {
            // ----- Arrange -----
            val book2023 = buildBook(
                id = 1,
                userBook = buildUserBook(
                    journals = listOf(buildFinishedJournal("2023-05-01T00:00:00")),
                ),
            )
            val book2022 = buildBook(
                id = 2,
                userBook = buildUserBook(
                    journals = listOf(buildFinishedJournal("2022-05-01T00:00:00")),
                ),
            )

            val state = buildState(
                booksByTab = mapOf(readTabId to listOf(book2023, book2022)),
                selectedReadYear = 2023,
            )

            // ----- Act -----
            val result = state.displayBooksFor(readTabId)

            // ----- Assert -----
            result!!.map { it.id } shouldBe listOf(1)
        }

        @Test
        fun `Read tab year filter is NOT applied when selectedReadYear is null`() {
            // ----- Arrange -----
            val book2023 = buildBook(
                id = 1,
                userBook = buildUserBook(
                    journals = listOf(buildFinishedJournal("2023-05-01T00:00:00")),
                ),
            )
            val book2022 = buildBook(
                id = 2,
                userBook = buildUserBook(
                    journals = listOf(buildFinishedJournal("2022-05-01T00:00:00")),
                ),
            )

            val state = buildState(
                booksByTab = mapOf(readTabId to listOf(book2023, book2022)),
                selectedReadYear = null,
            )

            // ----- Act -----
            val result = state.displayBooksFor(readTabId)

            // ----- Assert -----
            result!!.map { it.id }.toSet() shouldBe setOf(1, 2)
        }

        @Test
        fun `Read tab applies both search query and year filter together`() {
            // ----- Arrange -----
            val matchBoth = buildBook(
                id = 1,
                title = "Foundation",
                userBook = buildUserBook(
                    journals = listOf(buildFinishedJournal("2023-05-01T00:00:00")),
                ),
            )
            val wrongYear = buildBook(
                id = 2,
                title = "Foundation's Edge",
                userBook = buildUserBook(
                    journals = listOf(buildFinishedJournal("2022-05-01T00:00:00")),
                ),
            )
            val wrongTitle = buildBook(
                id = 3,
                title = "Dune",
                userBook = buildUserBook(
                    journals = listOf(buildFinishedJournal("2023-08-01T00:00:00")),
                ),
            )

            val state = buildState(
                booksByTab = mapOf(readTabId to listOf(matchBoth, wrongYear, wrongTitle)),
                searchQuery = "foundation",
                selectedReadYear = 2023,
            )

            // ----- Act -----
            val result = state.displayBooksFor(readTabId)

            // ----- Assert -----
            result!!.map { it.id } shouldBe listOf(1)
        }

        @Test
        fun `sortModeByTab does not affect displayBooksFor output order`() {
            // Sort is applied by the DAO (SQL ORDER BY); the in-memory state's sortModeByTab is
            // only used by the screen to look up the persisted sort to flow back into the DAO
            // subscription. The render-time pass is filter-only.
            // ----- Arrange -----
            val bookZ = buildBook(
                id = 1,
                title = "Zebra",
            )
            val bookA = buildBook(
                id = 2,
                title = "Apple",
            )
            val bookM = buildBook(
                id = 3,
                title = "Mango",
            )

            val state = buildState(
                booksByTab = mapOf(otherTabId to listOf(bookZ, bookA, bookM)),
                sortModeByTab = mapOf(otherTabId to LibrarySortMode.TITLE),
                sortDirectionByTab = mapOf(otherTabId to SortDirection.DESCENDING),
            )

            // ----- Act -----
            val result = state.displayBooksFor(otherTabId)

            // ----- Assert -----
            // Output mirrors booksByTab input order regardless of sort settings.
            result!!.map { it.title } shouldBe listOf("Zebra", "Apple", "Mango")
        }
    }

    @Nested
    inner class DisplayEditionsFor {
        @Test
        fun `returns null when tabId is absent from editionsByTab`() {
            // ----- Arrange -----
            val state = buildState(editionsByTab = emptyMap())

            // ----- Act -----
            val result = state.displayEditionsFor(otherTabId)

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `filters editions by title case-insensitively`() {
            // ----- Arrange -----
            val match = buildEdition(
                id = 1,
                title = "Foundation",
            )
            val noMatch = buildEdition(
                id = 2,
                title = "Dune",
            )

            val state = buildState(
                editionsByTab = mapOf(otherTabId to listOf(match, noMatch)),
                searchQuery = "FOUNDATION",
            )

            // ----- Act -----
            val result = state.displayEditionsFor(otherTabId)

            // ----- Assert -----
            result!!.map { it.id } shouldBe listOf(1)
        }

        @Test
        fun `sorts filtered editions by TITLE mode alphabetically`() {
            // ----- Arrange -----
            val editionZ = buildEdition(
                id = 1,
                title = "Zebra Edition",
            )
            val editionA = buildEdition(
                id = 2,
                title = "Alpha Edition",
            )

            val state = buildState(
                editionsByTab = mapOf(otherTabId to listOf(editionZ, editionA)),
                sortModeByTab = mapOf(otherTabId to LibrarySortMode.TITLE),
            )

            // ----- Act -----
            val result = state.displayEditionsFor(otherTabId)

            // ----- Assert -----
            result!!.map { it.id } shouldBe listOf(2, 1)
        }

        @Test
        fun `displayEditionsFor reverses ordering when direction is the opposite of default`() {
            // ----- Arrange -----
            val editionA = buildEdition(
                id = 1,
                title = "Alpha Edition",
            )
            val editionZ = buildEdition(
                id = 2,
                title = "Zebra Edition",
            )

            val state = buildState(
                editionsByTab = mapOf(otherTabId to listOf(editionA, editionZ)),
                sortModeByTab = mapOf(otherTabId to LibrarySortMode.TITLE),
                sortDirectionByTab = mapOf(otherTabId to SortDirection.DESCENDING),
            )

            // ----- Act -----
            val result = state.displayEditionsFor(otherTabId)

            // ----- Assert -----
            result!!.map { it.id } shouldBe listOf(2, 1)
        }
    }

    @Nested
    inner class AvailableReadYears {
        @Test
        fun `returns empty list when booksByTab has no Read-tab entry`() {
            // ----- Arrange -----
            val state = buildState(booksByTab = emptyMap())

            // ----- Act -----
            val result = state.availableReadYears

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `returns descending distinct finished years from Read-tab books`() {
            // ----- Arrange -----
            val book2021a = buildBook(
                id = 1,
                userBook = buildUserBook(
                    journals = listOf(buildFinishedJournal("2021-06-01T00:00:00")),
                ),
            )
            val book2023 = buildBook(
                id = 2,
                userBook = buildUserBook(
                    journals = listOf(buildFinishedJournal("2023-01-01T00:00:00")),
                ),
            )
            val book2021b = buildBook(
                id = 3,
                userBook = buildUserBook(
                    journals = listOf(buildFinishedJournal("2021-12-31T00:00:00")),
                ),
            )

            val state = buildState(
                booksByTab = mapOf(readTabId to listOf(book2021a, book2023, book2021b)),
            )

            // ----- Act -----
            val result = state.availableReadYears

            // ----- Assert -----
            result shouldBe listOf(2023, 2021)
        }
    }

    @Nested
    inner class DisplayBooksForFilters {
        private val tagFiction = Tag(
            id = 1,
            name = "Fiction",
        )

        @Test
        fun `applies filter chips after search and year filters`() {
            // ----- Arrange -----
            val matchAll = buildBook(
                id = 1,
                title = "Foundation",
                editions = listOf(buildEditionFull(format = "ebook")),
                tags = listOf(tagFiction),
                rating = 4.5,
                releaseYear = 2021,
            )
            val matchSearchNotFilter = buildBook(
                id = 2,
                title = "Foundation's Edge",
                editions = listOf(buildEditionFull(format = "paperback")),
                tags = listOf(tagFiction),
                rating = 3.0,
                releaseYear = 2021,
            )

            val filters = LibraryFilters(ratingMin = 4.0)

            val state = buildState(
                booksByTab = mapOf(otherTabId to listOf(matchAll, matchSearchNotFilter)),
                searchQuery = "foundation",
                filtersByTab = mapOf(otherTabId to filters),
            )

            // ----- Act -----
            val result = state.displayBooksFor(otherTabId)

            // ----- Assert -----
            result!!.map { it.id } shouldBe listOf(1)
        }

        @Test
        fun `returns empty list when all books are excluded by the active filter`() {
            // ----- Arrange -----
            val book = buildBook(
                id = 1,
                editions = listOf(buildEditionFull(format = "paperback")),
            )

            val filters = LibraryFilters(formats = setOf("ebook"))

            val state = buildState(
                booksByTab = mapOf(otherTabId to listOf(book)),
                filtersByTab = mapOf(otherTabId to filters),
            )

            // ----- Act -----
            val result = state.displayBooksFor(otherTabId)

            // ----- Assert -----
            result shouldBe emptyList()
        }

        @Test
        fun `skips filter pass entirely when filters for tab are empty`() {
            // ----- Arrange -----
            val book1 = buildBook(id = 1)
            val book2 = buildBook(id = 2)

            val state = buildState(
                booksByTab = mapOf(otherTabId to listOf(book1, book2)),
                filtersByTab = emptyMap(),
            )

            // ----- Act -----
            val result = state.displayBooksFor(otherTabId)

            // ----- Assert -----
            result!!.size shouldBe 2
        }
    }

    @Nested
    inner class DisplayEditionsForFilters {
        private val tagFiction = Tag(
            id = 1,
            name = "Fiction",
        )

        @Test
        fun `applies filter chips after sort`() {
            // ----- Arrange -----
            val ownedEdition = buildEditionFull(
                id = 1,
                bookId = 10,
                format = "ebook",
                owned = true,
            )
            val unownedEdition = buildEditionFull(
                id = 2,
                bookId = 10,
                format = "ebook",
                owned = false,
            )

            val filters = LibraryFilters(owned = true)

            val state = buildState(
                editionsByTab = mapOf(otherTabId to listOf(ownedEdition, unownedEdition)),
                filtersByTab = mapOf(otherTabId to filters),
            )

            // ----- Act -----
            val result = state.displayEditionsFor(otherTabId)

            // ----- Assert -----
            result!!.map { it.id } shouldBe listOf(1)
        }

        @Test
        fun `resolves parent book via bookByBookId for tag facet`() {
            // ----- Arrange -----
            val edition = buildEditionFull(
                id = 1,
                bookId = 10,
            )
            val book = buildBook(
                id = 10,
                tags = listOf(tagFiction),
            )

            val filters = LibraryFilters(tags = setOf(tagFiction))

            val state = buildState(
                editionsByTab = mapOf(otherTabId to listOf(edition)),
                bookByBookId = mapOf(10 to book),
                filtersByTab = mapOf(otherTabId to filters),
            )

            // ----- Act -----
            val result = state.displayEditionsFor(otherTabId)

            // ----- Assert -----
            result!!.map { it.id } shouldBe listOf(1)
        }

        @Test
        fun `rejects edition when parent book is missing and tag filter is active`() {
            // ----- Arrange -----
            val edition = buildEditionFull(
                id = 1,
                bookId = 99,
            )

            val filters = LibraryFilters(tags = setOf(tagFiction))

            val state = buildState(
                editionsByTab = mapOf(otherTabId to listOf(edition)),
                bookByBookId = emptyMap(),
                filtersByTab = mapOf(otherTabId to filters),
            )

            // ----- Act -----
            val result = state.displayEditionsFor(otherTabId)

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class AvailableFilterOptionsFor {
        private val sampleOptions = LibraryFilterOptions(
            tags = listOf(Tag(
                id = 1,
                name = "Fiction",
            ),),
            formats = listOf("ebook"),
            releaseYears = listOf(2024),
            supportsOwnedFilter = true,
            ratingBuckets = listOf(4.0),
        )

        @Test
        fun `returns empty options when the tab has no precomputed entry`() {
            // ----- Arrange -----
            val state = buildState(filterOptionsByTab = emptyMap())

            // ----- Act -----
            val result = state.availableFilterOptionsFor(otherTabId)

            // ----- Assert -----
            result.isEmpty shouldBe true
        }

        @Test
        fun `returns the precomputed options for the tab when present`() {
            // ----- Arrange -----
            val state = buildState(filterOptionsByTab = mapOf(otherTabId to sampleOptions))

            // ----- Act -----
            val result = state.availableFilterOptionsFor(otherTabId)

            // ----- Assert -----
            result shouldBe sampleOptions
        }

        @Test
        fun `returns empty options for a tab that is not in the precomputed map`() {
            // ----- Arrange -----
            val state = buildState(
                filterOptionsByTab = mapOf("some-other-tab" to sampleOptions),
            )

            // ----- Act -----
            val result = state.availableFilterOptionsFor(otherTabId)

            // ----- Assert -----
            result.isEmpty shouldBe true
        }
    }
}
