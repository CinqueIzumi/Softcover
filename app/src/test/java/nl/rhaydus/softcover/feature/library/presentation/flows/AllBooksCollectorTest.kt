package nl.rhaydus.softcover.feature.library.presentation.flows

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.books.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetEnabledListIdsAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetEnabledStatusCodesAsFlowUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AllBooksCollectorTest {

    private lateinit var getAllUserBooksUseCase: GetAllUserBooksUseCase
    private lateinit var getAllUserListsUseCase: GetAllUserListsUseCase
    private lateinit var getEnabledStatusCodesAsFlowUseCase: GetEnabledStatusCodesAsFlowUseCase
    private lateinit var getEnabledListIdsAsFlowUseCase: GetEnabledListIdsAsFlowUseCase
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>
    private lateinit var booksFlow: MutableSharedFlow<List<Book>>
    private lateinit var listsFlow: MutableSharedFlow<List<BookList>>
    private lateinit var statusCodesFlow: MutableSharedFlow<Set<Int>>
    private lateinit var enabledListIdsFlow: MutableSharedFlow<Set<Int>>

    @BeforeEach
    fun setUp() {
        booksFlow = MutableSharedFlow()
        listsFlow = MutableSharedFlow()
        statusCodesFlow = MutableSharedFlow()
        enabledListIdsFlow = MutableSharedFlow()
        getAllUserBooksUseCase = mockk()
        getAllUserListsUseCase = mockk()
        getEnabledStatusCodesAsFlowUseCase = mockk()
        getEnabledListIdsAsFlowUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getAllUserBooksUseCase()
        } returns booksFlow

        every {
            getAllUserListsUseCase()
        } returns listsFlow

        every {
            getEnabledStatusCodesAsFlowUseCase()
        } returns statusCodesFlow

        every {
            getEnabledListIdsAsFlowUseCase()
        } returns enabledListIdsFlow

        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.getAllUserBooksUseCase
            } returns getAllUserBooksUseCase

            every {
                mock.getAllUserListsUseCase
            } returns getAllUserListsUseCase

            every {
                mock.getEnabledStatusCodesAsFlowUseCase
            } returns getEnabledStatusCodesAsFlowUseCase

            every {
                mock.getEnabledListIdsAsFlowUseCase
            } returns getEnabledListIdsAsFlowUseCase
        }
    }

    private fun stubBook(id: Int, status: UserBookStatus?): Book = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            userBook
        } returns if (status != null) {
            mockk<UserBook> {
                every {
                    this@mockk.status
                } returns BookStatus.getFromCode(status.code)
            }
        } else {
            null
        }
    }

    private fun stubBookList(id: Int, bookIds: List<Int>): BookList = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            books
        } returns bookIds.map { bookId ->
            mockk<ListBook> {
                every {
                    this@mockk.bookId
                } returns bookId
            }
        }
    }

    @Nested
    inner class OnLaunch {

        @Test
        fun `books with enabled status appear in the All tab`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val readBook = stubBook(id = 1, status = UserBookStatus.READ)
            val collector = AllBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(listOf(readBook))
            listsFlow.emit(emptyList())
            statusCodesFlow.emit(setOf(UserBookStatus.READ.code))
            enabledListIdsFlow.emit(emptySet())

            // ----- Assert -----
            stateFlow.value.booksByTab[LibraryTab.All.id] shouldBe listOf(readBook)
            job.cancel()
        }

        @Test
        fun `books with CURRENTLY_READING status always appear in All tab even when CR not in enabled set`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val crBook = stubBook(id = 2, status = UserBookStatus.CURRENTLY_READING)
            val collector = AllBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(listOf(crBook))
            listsFlow.emit(emptyList())
            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(emptySet())

            // ----- Assert -----
            stateFlow.value.booksByTab[LibraryTab.All.id] shouldBe listOf(crBook)
            job.cancel()
        }

        @Test
        fun `book in an enabled list appears in All even when its status is disabled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val bookId = 99
            val wtrBook = stubBook(id = bookId, status = UserBookStatus.WANT_TO_READ)
            val enabledList = stubBookList(id = 5, bookIds = listOf(bookId))
            val collector = AllBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(listOf(wtrBook))
            listsFlow.emit(listOf(enabledList))
            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(setOf(5))

            // ----- Assert -----
            stateFlow.value.booksByTab[LibraryTab.All.id] shouldBe listOf(wtrBook)
            job.cancel()
        }

        @Test
        fun `book in a disabled list with disabled status does not appear in All`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val bookId = 77
            val wtrBook = stubBook(id = bookId, status = UserBookStatus.WANT_TO_READ)
            val disabledList = stubBookList(id = 5, bookIds = listOf(bookId))
            val collector = AllBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(listOf(wtrBook))
            listsFlow.emit(listOf(disabledList))
            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(emptySet())

            // ----- Assert -----
            stateFlow.value.booksByTab[LibraryTab.All.id] shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `books without a userBook do not appear in All when not in any enabled list`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val noUserBookBook = stubBook(id = 55, status = null)
            val collector = AllBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(listOf(noUserBookBook))
            listsFlow.emit(emptyList())
            statusCodesFlow.emit(setOf(UserBookStatus.READ.code))
            enabledListIdsFlow.emit(emptySet())

            // ----- Assert -----
            stateFlow.value.booksByTab[LibraryTab.All.id] shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `sets isLoading to false after emitting`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isLoading = true)
            val collector = AllBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(emptyList())
            listsFlow.emit(emptyList())
            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(emptySet())

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `does not touch booksByTab before all four flows emit`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = AllBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act & Assert -----
            stateFlow.value.booksByTab.containsKey(LibraryTab.All.id) shouldBe false
            job.cancel()
        }

        @Test
        fun `reacts to updated books flow by re-computing All tab`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val readBook1 = stubBook(id = 1, status = UserBookStatus.READ)
            val readBook2 = stubBook(id = 2, status = UserBookStatus.READ)
            val collector = AllBooksCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            booksFlow.emit(listOf(readBook1))
            listsFlow.emit(emptyList())
            statusCodesFlow.emit(setOf(UserBookStatus.READ.code))
            enabledListIdsFlow.emit(emptySet())

            // ----- Act -----
            booksFlow.emit(listOf(readBook1, readBook2))

            // ----- Assert -----
            stateFlow.value.booksByTab[LibraryTab.All.id] shouldBe listOf(readBook1, readBook2)
            job.cancel()
        }
    }
}
