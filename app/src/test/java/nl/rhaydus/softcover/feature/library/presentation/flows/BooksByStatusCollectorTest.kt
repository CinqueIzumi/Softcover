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
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.books.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetEnabledStatusCodesAsFlowUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BooksByStatusCollectorTest {

    private lateinit var getAllUserBooksUseCase: GetAllUserBooksUseCase
    private lateinit var getEnabledStatusCodesAsFlowUseCase: GetEnabledStatusCodesAsFlowUseCase
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>
    private lateinit var booksFlow: MutableSharedFlow<List<Book>>
    private lateinit var statusCodesFlow: MutableSharedFlow<Set<Int>>

    @BeforeEach
    fun setUp() {
        booksFlow = MutableSharedFlow()
        statusCodesFlow = MutableSharedFlow()
        getAllUserBooksUseCase = mockk()
        getEnabledStatusCodesAsFlowUseCase = mockk()
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
            getEnabledStatusCodesAsFlowUseCase()
        } returns statusCodesFlow

        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.getAllUserBooksUseCase
            } returns getAllUserBooksUseCase

            every {
                mock.getEnabledStatusCodesAsFlowUseCase
            } returns getEnabledStatusCodesAsFlowUseCase
        }
    }

    private fun stubBook(status: UserBookStatus): Book = mockk {
        every {
            userBook
        } returns mockk<UserBook> {
            every {
                this@mockk.status
            } returns BookStatus.getFromCode(status.code)
        }
    }

    @Nested
    inner class OnLaunch {

        @Test
        fun `groups books by their status tab id when both flows emit`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val crBook = stubBook(UserBookStatus.CURRENTLY_READING)
            val readBook = stubBook(UserBookStatus.READ)
            val collector = BooksByStatusCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(listOf(crBook, readBook))
            statusCodesFlow.emit(setOf(UserBookStatus.READ.code))

            // ----- Assert -----
            val crTabId = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id
            val readTabId = LibraryTab.Status.of(UserBookStatus.READ).id
            stateFlow.value.booksByTab[crTabId] shouldBe listOf(crBook)
            stateFlow.value.booksByTab[readTabId] shouldBe listOf(readBook)
            job.cancel()
        }

        @Test
        fun `always includes CURRENTLY_READING in active statuses even when not in enabled set`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val crBook = stubBook(UserBookStatus.CURRENTLY_READING)
            val collector = BooksByStatusCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(listOf(crBook))
            statusCodesFlow.emit(emptySet())

            // ----- Assert -----
            val crTabId = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id
            stateFlow.value.booksByTab[crTabId] shouldBe listOf(crBook)
            job.cancel()
        }

        @Test
        fun `strips old status tabs when enabled statuses shrink`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val wtrBook = stubBook(UserBookStatus.WANT_TO_READ)
            val collector = BooksByStatusCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            booksFlow.emit(listOf(wtrBook))
            statusCodesFlow.emit(setOf(UserBookStatus.WANT_TO_READ.code))

            val wtrTabId = LibraryTab.Status.of(UserBookStatus.WANT_TO_READ).id
            stateFlow.value.booksByTab.containsKey(wtrTabId) shouldBe true

            // ----- Act -----
            statusCodesFlow.emit(emptySet())

            // ----- Assert -----
            stateFlow.value.booksByTab.containsKey(wtrTabId) shouldBe false
            job.cancel()
        }

        @Test
        fun `empty books list produces empty lists for each active status tab`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = BooksByStatusCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(emptyList())
            statusCodesFlow.emit(setOf(UserBookStatus.READ.code))

            // ----- Assert -----
            val crTabId = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id
            val readTabId = LibraryTab.Status.of(UserBookStatus.READ).id
            stateFlow.value.booksByTab[crTabId] shouldBe emptyList()
            stateFlow.value.booksByTab[readTabId] shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `does not change booksByTab before either flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = BooksByStatusCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act & Assert -----
            stateFlow.value.booksByTab shouldBe emptyMap()
            job.cancel()
        }

        @Test
        fun `preserves non-status keys in booksByTab when updating`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val existingBook = mockk<Book>()
            stateFlow.value = LibraryUiState(booksByTab = mapOf("list-42" to listOf(existingBook)))
            val collector = BooksByStatusCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(emptyList())
            statusCodesFlow.emit(emptySet())

            // ----- Assert -----
            stateFlow.value.booksByTab.containsKey("list-42") shouldBe true
            job.cancel()
        }

        @Test
        fun `reacts to updated books flow after initial emit`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val crBook1 = stubBook(UserBookStatus.CURRENTLY_READING)
            val crBook2 = stubBook(UserBookStatus.CURRENTLY_READING)
            val collector = BooksByStatusCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            booksFlow.emit(listOf(crBook1))
            statusCodesFlow.emit(emptySet())

            val crTabId = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id

            // ----- Act -----
            booksFlow.emit(listOf(crBook1, crBook2))

            // ----- Assert -----
            stateFlow.value.booksByTab[crTabId] shouldBe listOf(crBook1, crBook2)
            job.cancel()
        }

        @Test
        fun `WANT_TO_READ code in enabledStatusCodes produces a status-1 tab`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val wtrBook = stubBook(UserBookStatus.WANT_TO_READ)
            val collector = BooksByStatusCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(listOf(wtrBook))
            statusCodesFlow.emit(setOf(UserBookStatus.WANT_TO_READ.code))

            // ----- Assert -----
            val wtrTabId = LibraryTab.Status.of(UserBookStatus.WANT_TO_READ).id
            stateFlow.value.booksByTab.containsKey(wtrTabId) shouldBe true
            stateFlow.value.booksByTab[wtrTabId] shouldBe listOf(wtrBook)
            job.cancel()
        }

        @Test
        fun `unknown status code in enabledCodes does not crash and produces only recognised tab entries`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val crBook = stubBook(UserBookStatus.CURRENTLY_READING)
            val collector = BooksByStatusCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            booksFlow.emit(listOf(crBook))
            // 999 is not a valid UserBookStatus code — it must be silently skipped
            statusCodesFlow.emit(setOf(999))

            // ----- Assert -----
            // Only CURRENTLY_READING (always-visible) should be present; code 999 must be absent
            val crTabId = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id
            stateFlow.value.booksByTab.containsKey(crTabId) shouldBe true
            stateFlow.value.booksByTab.keys.none { it == "status-999" } shouldBe true
            job.cancel()
        }
    }
}
