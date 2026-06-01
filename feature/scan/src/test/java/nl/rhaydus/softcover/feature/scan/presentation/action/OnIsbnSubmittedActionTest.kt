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
import nl.rhaydus.softcover.core.PreviewData
import nl.rhaydus.softcover.core.book.domain.usecase.AddBookByIsbnUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.IsbnLookupResult
import nl.rhaydus.softcover.core.book.domain.usecase.ResolveBookByIsbnUseCase
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.scan.presentation.event.BookResolvedEvent
import nl.rhaydus.softcover.feature.scan.presentation.event.InvalidIsbnEvent
import nl.rhaydus.softcover.feature.scan.presentation.event.ResolutionFailedEvent
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnIsbnSubmittedActionTest {

    private lateinit var resolveBookByIsbnUseCase: ResolveBookByIsbnUseCase
    private lateinit var stateFlow: MutableStateFlow<ScanUiState>
    private lateinit var eventChannel: Channel<ScanEvent>
    private lateinit var scope: ActionScope<ScanUiState, ScanEvent, LocalScanVariables>

    @BeforeEach
    fun setUp() {
        resolveBookByIsbnUseCase = mockk()
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
                mock.resolveBookByIsbnUseCase
            } returns resolveBookByIsbnUseCase

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
        fun `does not invoke use case when isResolving is already true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(isResolving = true)
            val dependencies = stubDependencies(this)
            val action = OnIsbnSubmittedAction(isbn = "9780451524935")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            io.mockk.coVerify(exactly = 0) {
                resolveBookByIsbnUseCase(isbn = any())
            }
        }

        @Test
        fun `does not change state when isResolving is already true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(isResolving = true)
            val dependencies = stubDependencies(this)
            val stateBefore = stateFlow.value
            val action = OnIsbnSubmittedAction(isbn = "9780451524935")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe stateBefore
        }

        @Test
        fun `sets isResolving to true before invoking use case`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            var stateAtInvocation: ScanUiState? = null

            coEvery {
                resolveBookByIsbnUseCase(isbn = any())
            } coAnswers {
                stateAtInvocation = stateFlow.value
                Result.success(IsbnLookupResult.InvalidIsbn)
            }

            val action = OnIsbnSubmittedAction(isbn = "9780451524935")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateAtInvocation?.isResolving shouldBe true
        }

        @Test
        fun `sets isResolving to false and sends BookResolvedEvent when result is Found`() = runTest {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val editionId = 42
            val dependencies = stubDependencies(this)

            coEvery {
                resolveBookByIsbnUseCase(isbn = "9780451524935")
            } returns Result.success(IsbnLookupResult.Found(book = book, editionId = editionId))

            val action = OnIsbnSubmittedAction(isbn = "9780451524935")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isResolving shouldBe false

            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeInstanceOf<BookResolvedEvent>()
            (event as BookResolvedEvent).book shouldBe book
            event.editionId shouldBe editionId
        }

        @Test
        fun `sends BookResolvedEvent with null editionId when Found carries null editionId`() = runTest {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val dependencies = stubDependencies(this)

            coEvery {
                resolveBookByIsbnUseCase(isbn = "9780451524935")
            } returns Result.success(IsbnLookupResult.Found(book = book, editionId = null))

            val action = OnIsbnSubmittedAction(isbn = "9780451524935")

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
        fun `sets isResolving to false and stores unknownIsbn when result is UnknownEdition`() = runTest {
            // ----- Arrange -----
            val normalizedIsbn = "9780451524935"
            val dependencies = stubDependencies(this)

            coEvery {
                resolveBookByIsbnUseCase(isbn = normalizedIsbn)
            } returns Result.success(IsbnLookupResult.UnknownEdition(normalizedIsbn = normalizedIsbn))

            val action = OnIsbnSubmittedAction(isbn = normalizedIsbn)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isResolving shouldBe false
            stateFlow.value.unknownIsbn shouldBe normalizedIsbn
        }

        @Test
        fun `does not send any event when result is UnknownEdition`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                resolveBookByIsbnUseCase(isbn = any())
            } returns Result.success(IsbnLookupResult.UnknownEdition(normalizedIsbn = "9780451524935"))

            val action = OnIsbnSubmittedAction(isbn = "9780451524935")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event shouldBe null
        }

        @Test
        fun `sets isResolving to false and sends InvalidIsbnEvent when result is InvalidIsbn`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                resolveBookByIsbnUseCase(isbn = "not-a-valid-isbn")
            } returns Result.success(IsbnLookupResult.InvalidIsbn)

            val action = OnIsbnSubmittedAction(isbn = "not-a-valid-isbn")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isResolving shouldBe false

            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeInstanceOf<InvalidIsbnEvent>()
        }

        @Test
        fun `sets isResolving to false and sends ResolutionFailedEvent on use case failure`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                resolveBookByIsbnUseCase(isbn = any())
            } returns Result.failure(RuntimeException("network error"))

            val action = OnIsbnSubmittedAction(isbn = "9780451524935")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isResolving shouldBe false

            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeInstanceOf<ResolutionFailedEvent>()
        }

        @Test
        fun `does not change unknownIsbn when use case fails`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(unknownIsbn = "9780451524935")
            val dependencies = stubDependencies(this)

            coEvery {
                resolveBookByIsbnUseCase(isbn = any())
            } returns Result.failure(RuntimeException("network error"))

            val action = OnIsbnSubmittedAction(isbn = "9780000000001")

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
