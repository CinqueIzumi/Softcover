package nl.rhaydus.softcover.feature.scan.presentation.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.AddBookByIsbnUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.IsbnLookupResult
import nl.rhaydus.softcover.core.book.domain.usecase.ResolveBookByIsbnUseCase
import nl.rhaydus.softcover.core.designsystem.presentation.preview.PreviewData
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.scan.presentation.event.AddBookFailedEvent
import nl.rhaydus.softcover.feature.scan.presentation.event.BookResolvedEvent
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnAddUnknownIsbnConfirmedActionTest {

    private lateinit var addBookByIsbnUseCase: AddBookByIsbnUseCase
    private lateinit var stateFlow: MutableStateFlow<ScanUiState>
    private lateinit var eventChannel: Channel<ScanEvent>
    private lateinit var scope: ActionScope<ScanUiState, ScanEvent, LocalScanVariables>

    @BeforeEach
    fun setUp() {
        addBookByIsbnUseCase = mockk()
        stateFlow = MutableStateFlow(ScanUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalScanVariables()),
            eventChannel = eventChannel,
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): ScanDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)

        return mockk<ScanDependencies>(relaxed = true).also { mock ->
            every {
                mock.addBookByIsbnUseCase
            } returns addBookByIsbnUseCase

            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher

            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    @Nested
    inner class Execute {

        @Test
        fun `does not invoke use case when unknownIsbn is null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(unknownIsbn = null)
            val dependencies = stubDependencies(this)
            val action = OnAddUnknownIsbnConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            io.mockk.coVerify(exactly = 0) {
                addBookByIsbnUseCase(isbn = any())
            }
        }

        @Test
        fun `does not change state when unknownIsbn is null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(unknownIsbn = null)
            val dependencies = stubDependencies(this)
            val stateBefore = stateFlow.value
            val action = OnAddUnknownIsbnConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe stateBefore
        }

        @Test
        fun `does not invoke use case when isAddingBook is already true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(unknownIsbn = "9780451524935", isAddingBook = true)
            val dependencies = stubDependencies(this)
            val action = OnAddUnknownIsbnConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            io.mockk.coVerify(exactly = 0) {
                addBookByIsbnUseCase(isbn = any())
            }
        }

        @Test
        fun `does not change state when isAddingBook is already true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(unknownIsbn = "9780451524935", isAddingBook = true)
            val dependencies = stubDependencies(this)
            val stateBefore = stateFlow.value
            val action = OnAddUnknownIsbnConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe stateBefore
        }

        @Test
        fun `sets isAddingBook to true before invoking use case`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(unknownIsbn = "9780451524935")
            val dependencies = stubDependencies(this)
            var stateAtInvocation: ScanUiState? = null

            coEvery {
                addBookByIsbnUseCase(isbn = any())
            } coAnswers {
                stateAtInvocation = stateFlow.value
                Result.success(IsbnLookupResult.Found(book = PreviewData.baseBook, editionId = null))
            }

            val action = OnAddUnknownIsbnConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateAtInvocation?.isAddingBook shouldBe true
        }

        @Test
        fun `clears unknownIsbn and isAddingBook and sends BookResolvedEvent on success`() = runTest {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val editionId = 7
            stateFlow.value = ScanUiState(unknownIsbn = "9780451524935")
            val dependencies = stubDependencies(this)

            coEvery {
                addBookByIsbnUseCase(isbn = "9780451524935")
            } returns Result.success(IsbnLookupResult.Found(book = book, editionId = editionId))

            val action = OnAddUnknownIsbnConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.unknownIsbn shouldBe null
            stateFlow.value.isAddingBook shouldBe false

            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeInstanceOf<BookResolvedEvent>()
            (event as BookResolvedEvent).book shouldBe book
            event.editionId shouldBe editionId
        }

        @Test
        fun `sends BookResolvedEvent with null editionId when Found carries null editionId`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(unknownIsbn = "9780451524935")
            val dependencies = stubDependencies(this)

            coEvery {
                addBookByIsbnUseCase(isbn = "9780451524935")
            } returns Result.success(IsbnLookupResult.Found(book = PreviewData.baseBook, editionId = null))

            val action = OnAddUnknownIsbnConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeInstanceOf<BookResolvedEvent>()
            (event as BookResolvedEvent).editionId shouldBe null
        }

        @Test
        fun `sets isAddingBook to false and sends AddBookFailedEvent on failure`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(unknownIsbn = "9780451524935")
            val dependencies = stubDependencies(this)

            coEvery {
                addBookByIsbnUseCase(isbn = "9780451524935")
            } returns Result.failure(RuntimeException("network error"))

            val action = OnAddUnknownIsbnConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isAddingBook shouldBe false

            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeInstanceOf<AddBookFailedEvent>()
        }

        @Test
        fun `retains unknownIsbn when use case fails`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(unknownIsbn = "9780451524935")
            val dependencies = stubDependencies(this)

            coEvery {
                addBookByIsbnUseCase(isbn = "9780451524935")
            } returns Result.failure(RuntimeException("network error"))

            val action = OnAddUnknownIsbnConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.unknownIsbn shouldBe "9780451524935"
        }
    }
}
