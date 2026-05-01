package nl.rhaydus.softcover.feature.library.presentation.flows

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.books.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetWantToReadUserBooksUseCase
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

    private lateinit var getCurrentlyReadingUserBooksUseCase: GetCurrentlyReadingUserBooksUseCase
    private lateinit var getWantToReadUserBooksUseCase: GetWantToReadUserBooksUseCase
    private lateinit var getReadUserBooksUseCase: GetReadUserBooksUseCase
    private lateinit var getDidNotFinishUserBooksUseCase: GetDidNotFinishUserBooksUseCase
    private lateinit var getEnabledStatusCodesAsFlowUseCase: GetEnabledStatusCodesAsFlowUseCase
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private lateinit var currentlyReadingFlow: MutableSharedFlow<List<Book>>
    private lateinit var wantToReadFlow: MutableSharedFlow<List<Book>>
    private lateinit var readFlow: MutableSharedFlow<List<Book>>
    private lateinit var didNotFinishFlow: MutableSharedFlow<List<Book>>
    private lateinit var statusCodesFlow: MutableSharedFlow<Set<Int>>

    @BeforeEach
    fun setUp() {
        currentlyReadingFlow = MutableSharedFlow()
        wantToReadFlow = MutableSharedFlow()
        readFlow = MutableSharedFlow()
        didNotFinishFlow = MutableSharedFlow()
        statusCodesFlow = MutableSharedFlow()

        getCurrentlyReadingUserBooksUseCase = mockk()
        getWantToReadUserBooksUseCase = mockk()
        getReadUserBooksUseCase = mockk()
        getDidNotFinishUserBooksUseCase = mockk()
        getEnabledStatusCodesAsFlowUseCase = mockk()

        every { getCurrentlyReadingUserBooksUseCase() } returns currentlyReadingFlow
        every { getWantToReadUserBooksUseCase() } returns wantToReadFlow
        every { getReadUserBooksUseCase() } returns readFlow
        every { getDidNotFinishUserBooksUseCase() } returns didNotFinishFlow
        every { getEnabledStatusCodesAsFlowUseCase() } returns statusCodesFlow

        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every { mock.getCurrentlyReadingUserBooksUseCase } returns getCurrentlyReadingUserBooksUseCase
            every { mock.getWantToReadUserBooksUseCase } returns getWantToReadUserBooksUseCase
            every { mock.getReadUserBooksUseCase } returns getReadUserBooksUseCase
            every { mock.getDidNotFinishUserBooksUseCase } returns getDidNotFinishUserBooksUseCase
            every { mock.getEnabledStatusCodesAsFlowUseCase } returns getEnabledStatusCodesAsFlowUseCase
        }
    }

    @Nested
    inner class OnLaunch {

        @Test
        fun `groups books by their status tab id when all five flows emit`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val crBook = mockk<Book>()
            val readBook = mockk<Book>()
            val collector = BooksByStatusCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            currentlyReadingFlow.emit(listOf(crBook))
            wantToReadFlow.emit(emptyList())
            readFlow.emit(listOf(readBook))
            didNotFinishFlow.emit(emptyList())
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
            val crBook = mockk<Book>()
            val collector = BooksByStatusCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            currentlyReadingFlow.emit(listOf(crBook))
            wantToReadFlow.emit(emptyList())
            readFlow.emit(emptyList())
            didNotFinishFlow.emit(emptyList())
            statusCodesFlow.emit(emptySet())

            // ----- Assert -----
            val crTabId = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id
            stateFlow.value.booksByTab[crTabId] shouldBe listOf(crBook)
            job.cancel()
        }

        @Test
        fun `strips old status tabs when enabled statuses shrink`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val wtrBook = mockk<Book>()
            val collector = BooksByStatusCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            currentlyReadingFlow.emit(emptyList())
            wantToReadFlow.emit(listOf(wtrBook))
            readFlow.emit(emptyList())
            didNotFinishFlow.emit(emptyList())
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
        fun `empty lists from use cases produce empty lists in the map for those active tabs`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = BooksByStatusCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            currentlyReadingFlow.emit(emptyList())
            wantToReadFlow.emit(emptyList())
            readFlow.emit(emptyList())
            didNotFinishFlow.emit(emptyList())
            statusCodesFlow.emit(setOf(UserBookStatus.READ.code))

            // ----- Assert -----
            val crTabId = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id
            val readTabId = LibraryTab.Status.of(UserBookStatus.READ).id
            stateFlow.value.booksByTab[crTabId] shouldBe emptyList()
            stateFlow.value.booksByTab[readTabId] shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `does not change booksByTab before all five flows emit`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = BooksByStatusCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act & Assert -----
            // Only four of five flows emit — combine requires all to have emitted at least once
            currentlyReadingFlow.emit(emptyList())
            wantToReadFlow.emit(emptyList())
            readFlow.emit(emptyList())
            didNotFinishFlow.emit(emptyList())
            // statusCodesFlow has not emitted yet
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
            currentlyReadingFlow.emit(emptyList())
            wantToReadFlow.emit(emptyList())
            readFlow.emit(emptyList())
            didNotFinishFlow.emit(emptyList())
            statusCodesFlow.emit(emptySet())

            // ----- Assert -----
            stateFlow.value.booksByTab.containsKey("list-42") shouldBe true
            job.cancel()
        }

        @Test
        fun `reacts to updated per-status flow after initial emit`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val crBook1 = mockk<Book>()
            val crBook2 = mockk<Book>()
            val collector = BooksByStatusCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            currentlyReadingFlow.emit(listOf(crBook1))
            wantToReadFlow.emit(emptyList())
            readFlow.emit(emptyList())
            didNotFinishFlow.emit(emptyList())
            statusCodesFlow.emit(emptySet())

            val crTabId = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id

            // ----- Act -----
            currentlyReadingFlow.emit(listOf(crBook1, crBook2))

            // ----- Assert -----
            stateFlow.value.booksByTab[crTabId] shouldBe listOf(crBook1, crBook2)
            job.cancel()
        }

        @Test
        fun `unknown status code in enabledCodes does not crash and produces only recognised tab entries`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val crBook = mockk<Book>()
            val collector = BooksByStatusCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            currentlyReadingFlow.emit(listOf(crBook))
            wantToReadFlow.emit(emptyList())
            readFlow.emit(emptyList())
            didNotFinishFlow.emit(emptyList())
            // 999 is not a valid UserBookStatus code — it must be silently skipped
            statusCodesFlow.emit(setOf(999))

            // ----- Assert -----
            val crTabId = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING).id
            stateFlow.value.booksByTab.containsKey(crTabId) shouldBe true
            stateFlow.value.booksByTab.keys.none { it == "status-999" } shouldBe true
            job.cancel()
        }

        @Test
        fun `each per-status use case is invoked to supply its flow`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = BooksByStatusCollector()

            // ----- Act -----
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Assert -----
            verify(exactly = 1) { getCurrentlyReadingUserBooksUseCase() }
            verify(exactly = 1) { getWantToReadUserBooksUseCase() }
            verify(exactly = 1) { getReadUserBooksUseCase() }
            verify(exactly = 1) { getDidNotFinishUserBooksUseCase() }
            verify(exactly = 1) { getEnabledStatusCodesAsFlowUseCase() }
            job.cancel()
        }

        @Test
        fun `WANT_TO_READ code in enabledStatusCodes produces a status-1 tab`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val wtrBook = mockk<Book>()
            val collector = BooksByStatusCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            currentlyReadingFlow.emit(emptyList())
            wantToReadFlow.emit(listOf(wtrBook))
            readFlow.emit(emptyList())
            didNotFinishFlow.emit(emptyList())
            statusCodesFlow.emit(setOf(UserBookStatus.WANT_TO_READ.code))

            // ----- Assert -----
            val wtrTabId = LibraryTab.Status.of(UserBookStatus.WANT_TO_READ).id
            stateFlow.value.booksByTab.containsKey(wtrTabId) shouldBe true
            stateFlow.value.booksByTab[wtrTabId] shouldBe listOf(wtrBook)
            job.cancel()
        }
    }
}
