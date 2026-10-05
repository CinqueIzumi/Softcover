package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.GetSortedAllUserBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.LibrarySortSettings
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledListIdsAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledStatusCodesAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLibrarySortSettingsAsFlowUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AllBooksCollectorTest {
    private lateinit var settingsFlow: MutableStateFlow<Map<String, LibrarySortSettings>>
    private lateinit var booksFlow: MutableStateFlow<List<Book>>
    private lateinit var listsFlow: MutableStateFlow<List<BookList>>
    private lateinit var enabledStatusesFlow: MutableStateFlow<Set<Int>>
    private lateinit var enabledListIdsFlow: MutableStateFlow<Set<Int>>
    private lateinit var getSortedAllUserBooksUseCase: GetSortedAllUserBooksUseCase
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    @BeforeEach
    fun setUp() {
        settingsFlow = MutableStateFlow(emptyMap())
        booksFlow = MutableStateFlow(emptyList())
        listsFlow = MutableStateFlow(emptyList())
        enabledStatusesFlow = MutableStateFlow(emptySet())
        enabledListIdsFlow = MutableStateFlow(emptySet())
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        getSortedAllUserBooksUseCase = mockk()

        every {
            getSortedAllUserBooksUseCase(
                mode = any(),
                direction = any(),
            )
        } returns booksFlow

        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.getLibrarySortSettingsAsFlowUseCase
            } returns mockk<GetLibrarySortSettingsAsFlowUseCase> {
                every {
                    this@mockk()
                } returns settingsFlow
            }

            every {
                mock.getAllUserListsUseCase
            } returns mockk<GetAllUserListsUseCase> {
                every {
                    this@mockk()
                } returns listsFlow
            }

            every {
                mock.getEnabledStatusCodesAsFlowUseCase
            } returns mockk<GetEnabledStatusCodesAsFlowUseCase> {
                every {
                    this@mockk()
                } returns enabledStatusesFlow
            }

            every {
                mock.getEnabledListIdsAsFlowUseCase
            } returns mockk<GetEnabledListIdsAsFlowUseCase> {
                every {
                    this@mockk()
                } returns enabledListIdsFlow
            }

            every {
                mock.getSortedAllUserBooksUseCase
            } returns getSortedAllUserBooksUseCase
        }
    }

    private fun stubBook(
        id: Int,
        status: BookStatus?,
    ): Book = mockk<Book>(relaxed = true).also { book ->
        every {
            book.id
        } returns id

        every {
            book.userBook
        } returns status?.let { bookStatus ->
            mockk(relaxed = true) {
                every {
                    this@mockk.status
                } returns bookStatus
            }
        }
    }

    private fun listWith(
        listId: Int,
        bookId: Int,
    ): BookList = BookList(
        id = listId,
        name = "List $listId",
        slug = "list-$listId",
        books = listOf(bookId).map { id ->
            ListBook(
                listBookId = id,
                listId = listId,
                bookId = id,
                editionId = 1,
            )
        },
    )

    private fun TestScope.launchCollector() = launch {
        AllBooksCollector().onLaunch(
            scope = scope,
            dependencies = dependencies,
        )
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `keeps books on an enabled status and the always-visible currently-reading status`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val reading = stubBook(
                    id = 1,
                    status = BookStatus.Reading,
                )
                val read = stubBook(
                    id = 2,
                    status = BookStatus.Read,
                )
                val wantToRead = stubBook(
                    id = 3,
                    status = BookStatus.WantToRead,
                )
                val job = launchCollector()

                // ----- Act -----
                enabledStatusesFlow.value = setOf(BookStatus.Read.code)
                booksFlow.value = listOf(reading, read, wantToRead)

                // ----- Assert -----
                stateFlow.value.booksByTab.getValue("all") shouldBe listOf(reading, read)
                job.cancel()
            }

        @Test
        fun `drops books with no status unless they are on an enabled list`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val unshelved = stubBook(
                id = 1,
                status = null,
            )
            val onEnabledList = stubBook(
                id = 2,
                status = null,
            )
            val job = launchCollector()

            // ----- Act -----
            val list = listWith(
                listId = 10,
                bookId = 2,
            )
            listsFlow.value = listOf(list)
            enabledListIdsFlow.value = setOf(10)
            booksFlow.value = listOf(unshelved, onEnabledList)

            // ----- Assert -----
            stateFlow.value.booksByTab.getValue("all") shouldBe listOf(onEnabledList)
            job.cancel()
        }

        @Test
        fun `a book on a disabled list with a disabled status stays hidden`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(
                id = 1,
                status = BookStatus.Read,
            )
            val job = launchCollector()

            // ----- Act -----
            val list = listWith(
                listId = 10,
                bookId = 1,
            )
            listsFlow.value = listOf(list)
            enabledListIdsFlow.value = emptySet()
            booksFlow.value = listOf(book)

            // ----- Assert -----
            stateFlow.value.booksByTab.getValue("all") shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `a book on an enabled list shows even when its status is not enabled`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val book = stubBook(
                    id = 1,
                    status = BookStatus.Read,
                )
                val job = launchCollector()

                // ----- Act -----
                val list = listWith(
                    listId = 10,
                    bookId = 1,
                )
                listsFlow.value = listOf(list)
                enabledListIdsFlow.value = setOf(10)
                booksFlow.value = listOf(book)

                // ----- Assert -----
                stateFlow.value.booksByTab.getValue("all") shouldBe listOf(book)
                job.cancel()
            }

        @Test
        fun `the first emission marks the library as loaded and keeps other tabs`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val other = stubBook(
                    id = 9,
                    status = BookStatus.Read,
                )
                stateFlow.value = LibraryUiState(
                    booksByTab = mapOf("status-3" to listOf(other)),
                    isLoading = true,
                )
                val job = launchCollector()

                // ----- Act -----
                booksFlow.value = emptyList()

                // ----- Assert -----
                stateFlow.value.isLoading shouldBe false
                stateFlow.value.booksByTab.getValue("status-3") shouldBe listOf(other)
                job.cancel()
            }

        @Test
        fun `re-queries when the persisted All tab sort changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            settingsFlow.value = mapOf(
                "all" to LibrarySortSettings(
                    mode = LibrarySortMode.TITLE,
                    direction = SortDirection.DESCENDING,
                ),
            )

            // ----- Assert -----
            verify {
                getSortedAllUserBooksUseCase(
                    mode = LibrarySortMode.TITLE,
                    direction = SortDirection.DESCENDING,
                )
            }
            job.cancel()
        }
    }
}
