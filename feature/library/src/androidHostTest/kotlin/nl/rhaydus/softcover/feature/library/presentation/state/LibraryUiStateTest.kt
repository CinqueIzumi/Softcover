package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryUiStateTest {
    // region Fixtures
    private val readTabId = LibraryTab.Status.of(UserBookStatus.READ).id
    private val otherTabId = "list-99"

    private fun buildBook(
        id: Int = 1,
        title: String = "A Book",
    ) = Book(
        id = id,
        canonicalId = null,
        title = title,
        editions = emptyList(),
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        releaseDate = null,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        tags = emptyList(),
        userBook = null,
    )

    private fun buildState(
        booksByTab: Map<String, List<Book>> = emptyMap(),
        editionsByTab: Map<String, List<BookEdition>> = emptyMap(),
        bookByBookId: Map<Int, Book> = emptyMap(),
        searchQuery: String = "",
        sortModeByTab: Map<String, LibrarySortMode> = emptyMap(),
        sortDirectionByTab: Map<String, SortDirection> = emptyMap(),
        filtersByTab: Map<String, LibraryFilters> = emptyMap(),
        filterOptionsByTab: Map<String, LibraryFilterOptions> = emptyMap(),
        addedAtByTab: Map<String, Map<Int, String?>> = emptyMap(),
    ): LibraryUiState {
        val displayResult = DisplayInputs(
            booksByTab = booksByTab,
            editionsByTab = editionsByTab,
            searchQuery = searchQuery,
            filtersByTab = filtersByTab,
            sortModeByTab = sortModeByTab,
            sortDirectionByTab = sortDirectionByTab,
            addedAtByTab = addedAtByTab,
            bookByBookId = bookByBookId,
        ).compute(
            prevBooksByTab = emptyMap(),
            prevEditionsByTab = emptyMap(),
        )
        return LibraryUiState(
            booksByTab = booksByTab.mapValues { (_, books) -> books.toImmutableList() }.toImmutableMap(),
            editionsByTab = editionsByTab.mapValues { (_, editions) -> editions.toImmutableList() }.toImmutableMap(),
            bookByBookId = bookByBookId.toImmutableMap(),
            searchQuery = searchQuery,
            sortModeByTab = sortModeByTab.toImmutableMap(),
            sortDirectionByTab = sortDirectionByTab.toImmutableMap(),
            filtersByTab = filtersByTab.toImmutableMap(),
            filterOptionsByTab = filterOptionsByTab.toImmutableMap(),
            displayBooksByTab = displayResult.displayBooksByTab,
            displayEditionsByTab = displayResult.displayEditionsByTab,
            tabStatsByTab = displayResult.tabStatsByTab,
        )
    }
    // endregion
    @Nested
    inner class MastheadFor {
        @Test
        fun `returns the tab's own masthead when its id is present in mastheads`() {
            // ----- Arrange -----
            val readMasthead = PageMastheadUiModel(title = "Read")
            val state = LibraryUiState(
                mastheads = persistentMapOf(readTabId to readMasthead),
            )

            // ----- Act & Assert -----
            state.mastheadFor(readTabId) shouldBe readMasthead
        }

        @Test
        fun `falls back to fallbackMasthead when the tab id is not in mastheads`() {
            // ----- Arrange -----
            val fallback = PageMastheadUiModel(title = "Fallback")
            val state = LibraryUiState(
                mastheads = persistentMapOf(readTabId to PageMastheadUiModel(title = "Read")),
                fallbackMasthead = fallback,
            )

            // ----- Act & Assert -----
            state.mastheadFor(otherTabId) shouldBe fallback
        }

        @Test
        fun `falls back to fallbackMasthead when the tab id is null`() {
            // ----- Arrange -----
            val fallback = PageMastheadUiModel(title = "Fallback")
            val state = LibraryUiState(fallbackMasthead = fallback)

            // ----- Act & Assert -----
            state.mastheadFor(null) shouldBe fallback
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
            result.shouldNotBeNull().map { it.title } shouldBe listOf("Zebra", "Apple", "Mango")
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
    }

    @Nested
    inner class AvailableFilterOptionsFor {
        private val sampleOptions = LibraryFilterOptions(
            tags = persistentListOf(
                Tag(
                    id = 1,
                    name = "Fiction",
                ),
            ),
            formats = persistentListOf("ebook"),
            releaseYears = persistentListOf(2024),
            supportsOwnedFilter = true,
            ratingBuckets = persistentListOf(4.0),
        )

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

    @Nested
    inner class FilterPillFor {
        @Test
        fun `returns the stored pill for a tab that has one`() {
            // ----- Arrange -----
            val storedPill = filterPillButton(active = true)
            val state = buildState().copy(filterPillByTab = persistentMapOf(readTabId to storedPill))

            // ----- Act -----
            val result = state.filterPillFor(readTabId)

            // ----- Assert -----
            result shouldBe storedPill
        }

        @Test
        fun `returns the inactive pill without an indicator for an unknown tab`() {
            // ----- Arrange -----
            val state = buildState().copy(
                filterPillByTab = persistentMapOf(readTabId to filterPillButton(active = true)),
            )

            // ----- Act -----
            val result = state.filterPillFor(otherTabId)

            // ----- Assert -----
            result shouldBe state.inactiveFilterPill
        }


    @Nested
    inner class WithSelectedBookIds {
        @Test
        fun `sets the ids and a count matching their size together`() {
            // ----- Arrange -----
            val state = LibraryUiState()

            // ----- Act -----
            val result = state.withSelectedBookIds(ids = setOf(3, 5, 8))

            // ----- Assert -----
            result.selectedBookIds shouldBe setOf(3, 5, 8)
            result.selectionCount shouldBe selectionCountOf(count = 3)
        }

        @Test
        fun `an empty set resets the count to zero`() {
            // ----- Arrange -----
            val state = LibraryUiState().withSelectedBookIds(ids = setOf(1, 2))

            // ----- Act -----
            val result = state.withSelectedBookIds(ids = emptySet())

            // ----- Assert -----
            result.selectedBookIds shouldBe emptySet()
            result.selectionCount shouldBe selectionCountOf(count = 0)
        }

        @Test
        fun `leaves every other field untouched`() {
            // ----- Arrange -----
            val state = LibraryUiState(
                selectionMode = true,
                bulkActionInProgress = true,
            )

            // ----- Act -----
            val result = state.withSelectedBookIds(ids = setOf(1))

            // ----- Assert -----
            result.selectionMode shouldBe true
            result.bulkActionInProgress shouldBe true
        }
    }
    }
}
