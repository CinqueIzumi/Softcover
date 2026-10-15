package nl.rhaydus.softcover.feature.scan.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.IsbnLookupResult
import nl.rhaydus.softcover.core.book.domain.usecase.ResolveBookByIsbnUseCase
import nl.rhaydus.softcover.core.component.field.TextFieldError
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.IsbnOrigin
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ResolvedScan
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import nl.rhaydus.softcover.feature.scan.presentation.state.UnknownIsbnSheetUiModel
import nl.rhaydus.softcover.feature.scan.presentation.state.unknownIsbnSheetScaffoldFor
import nl.rhaydus.toad.ActionScope
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

    private fun stubDependencies(testScope: TestScope): ScanDependencies {
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
        fun `does nothing when isResolving is already true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(isResolving = true)
            val dependencies = stubDependencies(this)
            val stateBefore = stateFlow.value
            val action = OnIsbnSubmittedAction(
                isbn = "9780451524935",
                origin = IsbnOrigin.Camera,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                resolveBookByIsbnUseCase(isbn = any())
            }
            stateFlow.value shouldBe stateBefore
        }

        @Test
        fun `does nothing when a book is already being added`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(isAddingBook = true)
            val dependencies = stubDependencies(this)
            val stateBefore = stateFlow.value
            val action = OnIsbnSubmittedAction(
                isbn = "9780451524935",
                origin = IsbnOrigin.Camera,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                resolveBookByIsbnUseCase(isbn = any())
            }
            stateFlow.value shouldBe stateBefore
        }

        @Test
        fun `marks the lookup as resolving while the use case runs`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            var stateAtInvocation: ScanUiState? = null

            coEvery {
                resolveBookByIsbnUseCase(isbn = any())
            } coAnswers {
                stateAtInvocation = stateFlow.value
                Result.success(IsbnLookupResult.InvalidIsbn)
            }

            val action = OnIsbnSubmittedAction(
                isbn = "9780451524935",
                origin = IsbnOrigin.Camera,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateAtInvocation.shouldNotBeNull().isResolving shouldBe true
        }

        @Test
        fun `sets isResolving to false and sends BookResolvedEvent when result is Found`() = runTest {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val editionId = 42
            val dependencies = stubDependencies(this)

            coEvery {
                resolveBookByIsbnUseCase(isbn = "9780451524935")
            } returns Result.success(
                IsbnLookupResult.Found(
                    book = book,
                    editionId = editionId,
                ),
            )

            val action = OnIsbnSubmittedAction(
                isbn = "9780451524935",
                origin = IsbnOrigin.Camera,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isResolving shouldBe false

            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeInstanceOf<ScanEvent.BookResolvedEvent>()
            event.book shouldBe book
            event.editionId shouldBe editionId
        }

        @Test
        fun `requests the unknown isbn sheet for the normalized isbn and sends no event when result is UnknownEdition`() = runTest {
            // ----- Arrange -----
            val normalizedIsbn = "9780451524935"
            val dependencies = stubDependencies(this)

            coEvery {
                resolveBookByIsbnUseCase(isbn = normalizedIsbn)
            } returns Result.success(IsbnLookupResult.UnknownEdition(normalizedIsbn = normalizedIsbn))

            val action = OnIsbnSubmittedAction(
                isbn = normalizedIsbn,
                origin = IsbnOrigin.Camera,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isResolving shouldBe false
            stateFlow.value.unknownIsbn shouldBe normalizedIsbn
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `sets isResolving to false and sends InvalidIsbnEvent without a field error for a camera InvalidIsbn`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                resolveBookByIsbnUseCase(isbn = "not-a-valid-isbn")
            } returns Result.success(IsbnLookupResult.InvalidIsbn)

            val action = OnIsbnSubmittedAction(
                isbn = "not-a-valid-isbn",
                origin = IsbnOrigin.Camera,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isResolving shouldBe false
            stateFlow.value.isbnField.error.shouldBeNull()

            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<ScanEvent.InvalidIsbnEvent>()
        }

        @Test
        fun `sets a shaking field error and sends no event when result is InvalidIsbn from manual entry`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                resolveBookByIsbnUseCase(isbn = "not-a-valid-isbn")
            } returns Result.success(IsbnLookupResult.InvalidIsbn)

            val action = OnIsbnSubmittedAction(
                isbn = "not-a-valid-isbn",
                origin = IsbnOrigin.Manual,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isResolving shouldBe false
            stateFlow.value.isbnField.error shouldBe TextFieldError(message = "That doesn't look like a valid ISBN.")
            stateFlow.value.isbnField.error.shouldNotBeNull().shaking shouldBe true
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `sets isResolving to false and sends ResolutionFailedEvent on use case failure`() = runTest {
            // ----- Arrange -----
            val existingSheet = UnknownIsbnSheetUiModel(
                isbn = "9780451524935",
                scaffold = unknownIsbnSheetScaffoldFor(
                isAdding = false,
                isImportPending = false,
            ),
            )
            val existingSession = SheetSession.Open<UnknownIsbnSheetUiModel, ResolvedScan>(model = existingSheet)
            stateFlow.value = ScanUiState(unknownIsbnSheet = existingSession)
            val dependencies = stubDependencies(this)

            coEvery {
                resolveBookByIsbnUseCase(isbn = any())
            } returns Result.failure(RuntimeException("network error"))

            val action = OnIsbnSubmittedAction(
                isbn = "9780000000001",
                origin = IsbnOrigin.Camera,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isResolving shouldBe false
            stateFlow.value.unknownIsbnSheet shouldBe existingSession

            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<ScanEvent.ResolutionFailedEvent>()
        }
    

        @Test
        fun `resets the pending flag when a new unknown isbn is requested`() = runTest {
            // ----- Arrange -----
            val normalizedIsbn = "9780451524935"
            stateFlow.value = ScanUiState(isImportPending = true)
            val dependencies = stubDependencies(this)

            coEvery {
                resolveBookByIsbnUseCase(isbn = normalizedIsbn)
            } returns Result.success(IsbnLookupResult.UnknownEdition(normalizedIsbn = normalizedIsbn))

            val action = OnIsbnSubmittedAction(
                isbn = normalizedIsbn,
                origin = IsbnOrigin.Manual,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isImportPending shouldBe false
            stateFlow.value.unknownIsbn shouldBe normalizedIsbn
        }
}
}
