package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.designsystem.presentation.model.LibraryTab
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

class DisplayListsCollectorTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val testDispatcher = UnconfinedTestDispatcher()

    private fun buildEdition(
        id: Int = 1,
        bookId: Int = 1,
        title: String? = null,
        format: String = "paperback",
        owned: Boolean = false,
        pages: Int? = null,
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
        releaseYear = 2020,
        releaseDate = null,
        format = format,
        owned = owned,
    )

    private fun buildBook(
        id: Int = 1,
        title: String = "A Book",
        editions: List<BookEdition> = emptyList(),
        userBook: UserBook? = null,
        pages: Int? = null,
    ): Book {
        val edition = buildEdition(
            id = 10 + id,
            bookId = id,
            pages = pages,
        )
        return Book(
            id = id,
            canonicalId = null,
            title = title,
            editions = if (editions.isNotEmpty()) editions else listOf(edition),
            defaultEdition = if (editions.isNotEmpty()) null else edition,
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
            userBook = userBook,
            userBookRead = null,
        )
    }

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.defaultDispatcher
            } returns testDispatcher
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `populates displayBooksByTab when booksByTab is set in state`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val tabId = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id
            val book = buildBook(
                id = 1,
                title = "Kotlin in Action",
            )
            val collector = DisplayListsCollector()

            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = LibraryUiState(booksByTab = mapOf(tabId to listOf(book)))

            // ----- Assert -----
            stateFlow.value.displayBooksByTab[tabId] shouldBe listOf(book)
            job.cancel()
        }

        @Test
        fun `populates displayEditionsByTab when editionsByTab is set in state`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val listTabId = LibraryTab.CustomList(
                listId = 1,
                listName = "Owned",
            ).id
            val edition = buildEdition(
                id = 1,
                title = "Edition One",
            )
            val collector = DisplayListsCollector()

            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                editionsByTab = mapOf(listTabId to listOf(edition)),
                sortModeByTab = mapOf(listTabId to LibrarySortMode.TITLE),
                sortDirectionByTab = mapOf(listTabId to SortDirection.ASCENDING),
            )

            // ----- Assert -----
            stateFlow.value.displayEditionsByTab[listTabId] shouldBe listOf(edition)
            job.cancel()
        }

        @Test
        fun `tabStatsByTab for a book tab reports correct bookCount and totalPages`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val tabId = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id
            val book1 = buildBook(
                id = 1,
                pages = 200,
            )
            val book2 = buildBook(
                id = 2,
                pages = 150,
            )
            val collector = DisplayListsCollector()

            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = LibraryUiState(booksByTab = mapOf(tabId to listOf(book1, book2)))

            // ----- Assert -----
            val stats = stateFlow.value.tabStatsByTab[tabId]
            stats?.itemCount shouldBe 2
            stats?.totalPages shouldBe 350
            job.cancel()
        }

        @Test
        fun `tabStatsByTab for an edition tab always reports totalPages of 0`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val listTabId = LibraryTab.CustomList(
                listId = 5,
                listName = "Favourites",
            ).id
            val edition = buildEdition(
                id = 1,
                title = "Ed",
                pages = 300,
            )
            val collector = DisplayListsCollector()

            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                editionsByTab = mapOf(listTabId to listOf(edition)),
                sortModeByTab = mapOf(listTabId to LibrarySortMode.TITLE),
                sortDirectionByTab = mapOf(listTabId to SortDirection.ASCENDING),
            )

            // ----- Assert -----
            val stats = stateFlow.value.tabStatsByTab[listTabId]
            stats?.itemCount shouldBe 1
            stats?.totalPages shouldBe 0
            job.cancel()
        }

        @Test
        fun `searchQuery filters books in displayBooksByTab`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val tabId = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id
            val matching = buildBook(
                id = 1,
                title = "Kotlin in Action",
            )
            val nonMatching = buildBook(
                id = 2,
                title = "Clean Code",
            )
            val collector = DisplayListsCollector()

            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                booksByTab = mapOf(tabId to listOf(matching, nonMatching)),
                searchQuery = "kotlin",
            )

            // ----- Assert -----
            stateFlow.value.displayBooksByTab[tabId] shouldBe listOf(matching)
            job.cancel()
        }

        @Test
        fun `filter facets in filtersByTab narrow displayBooksByTab`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val tabId = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id
            val ebookEdition = buildEdition(
                id = 10,
                format = "ebook",
            )
            val paperEdition = buildEdition(
                id = 11,
                format = "paperback",
            )
            val ebookBook = buildBook(
                id = 1,
                editions = listOf(ebookEdition),
            )
            val paperBook = buildBook(
                id = 2,
                editions = listOf(paperEdition),
            )
            val collector = DisplayListsCollector()

            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                booksByTab = mapOf(tabId to listOf(ebookBook, paperBook)),
                filtersByTab = mapOf(tabId to LibraryFilters(formats = setOf("ebook"))),
            )

            // ----- Assert -----
            stateFlow.value.displayBooksByTab[tabId] shouldBe listOf(ebookBook)
            job.cancel()
        }

        @Test
        fun `changing an unrelated state field does not retrigger recompute`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val tabId = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id
            val book = buildBook(
                id = 1,
                title = "Stable Book",
            )
            val collector = DisplayListsCollector()

            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            stateFlow.value = LibraryUiState(booksByTab = mapOf(tabId to listOf(book)))
            val refAfterFirstEmit = stateFlow.value.displayBooksByTab[tabId]

            // ----- Act -----
            // Change a field not included in DisplayInputs — this should not retrigger collectLatest
            stateFlow.value = stateFlow.value.copy(isArrangeSheetExpanded = true)

            // ----- Assert -----
            stateFlow.value.displayBooksByTab[tabId] shouldBe listOf(book)
            // DisplayInputs didn't change so no new setState was called; the reference must be stable
            (stateFlow.value.displayBooksByTab[tabId] === refAfterFirstEmit) shouldBe true
            job.cancel()
        }

        @Test
        fun `same computed result reuses prior list reference for reference stability`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val tabId = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id
            val book = buildBook(
                id = 1,
                title = "Stable Book",
            )
            val collector = DisplayListsCollector()

            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // First emission: empty query, one book → computed list stored as prev
            stateFlow.value = LibraryUiState(
                booksByTab = mapOf(tabId to listOf(book)),
                searchQuery = "",
            )
            val refAfterFirst = stateFlow.value.displayBooksByTab[tabId]

            // ----- Act -----
            // Change searchQuery to whitespace — DisplayInputs changes, but trimmed query is still ""
            // so computeDisplayBooks returns the same list by value, triggering the stable=prev path
            stateFlow.value = stateFlow.value.copy(searchQuery = "  ")

            // ----- Assert -----
            val refAfterSecond = stateFlow.value.displayBooksByTab[tabId]
            refAfterSecond shouldBe listOf(book)
            (refAfterSecond === refAfterFirst) shouldBe true
            job.cancel()
        }

        @Test
        fun `displayBooksByTab is not set before first state emission beyond initial`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val collector = DisplayListsCollector()

            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            // No action — asserting on initial state.

            // ----- Assert -----
            // Initial LibraryUiState() has empty booksByTab, so displayBooksByTab is also empty
            stateFlow.value.displayBooksByTab shouldBe emptyMap()
            job.cancel()
        }
    }
}
