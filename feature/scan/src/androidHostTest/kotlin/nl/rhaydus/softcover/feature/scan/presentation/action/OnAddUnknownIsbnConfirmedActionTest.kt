package nl.rhaydus.softcover.feature.scan.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.AddBookByIsbnUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.IsbnLookupResult
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.domain.exception.BookImportPendingException
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ResolvedScan
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import nl.rhaydus.softcover.feature.scan.presentation.state.UnknownIsbnSheetUiModel
import nl.rhaydus.softcover.feature.scan.presentation.state.unknownIsbnSheetScaffoldFor
import nl.rhaydus.toad.ActionScope
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

    private fun stubDependencies(testScope: TestScope): ScanDependencies {
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

    private fun openSession(isbn: String): SheetSession<UnknownIsbnSheetUiModel, ResolvedScan> = SheetSession.Open(
        model = UnknownIsbnSheetUiModel(
            isbn = isbn,
            scaffold = unknownIsbnSheetScaffoldFor(
                isAdding = false,
                isImportPending = false,
            ),
        ),
    )

    @Nested
    inner class Execute {
        @Test
        fun `does nothing when no unknown isbn is requested`() = runTest {
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
            coVerify(exactly = 0) {
                addBookByIsbnUseCase(isbn = any())
            }
            stateFlow.value shouldBe stateBefore
        }

        @Test
        fun `does nothing when isAddingBook is already true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbn = "9780451524935",
                unknownIsbnSheet = openSession(isbn = "9780451524935"),
                isAddingBook = true,
            )
            val dependencies = stubDependencies(this)
            val stateBefore = stateFlow.value
            val action = OnAddUnknownIsbnConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                addBookByIsbnUseCase(isbn = any())
            }
            stateFlow.value shouldBe stateBefore
        }

        @Test
        fun `adds the book using the requested unknown isbn`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbn = "9781111111111",
                unknownIsbnSheet = openSession(isbn = "9781111111111"),
            )
            val dependencies = stubDependencies(this)

            coEvery {
                addBookByIsbnUseCase(isbn = any())
            } returns Result.failure(RuntimeException("network error"))

            val action = OnAddUnknownIsbnConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                addBookByIsbnUseCase(isbn = "9781111111111")
            }
        }

        @Test
        fun `marks the book as being added while the use case runs`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbn = "9780451524935",
                unknownIsbnSheet = openSession(isbn = "9780451524935"),
            )
            val dependencies = stubDependencies(this)
            var stateAtInvocation: ScanUiState? = null

            coEvery {
                addBookByIsbnUseCase(isbn = any())
            } coAnswers {
                stateAtInvocation = stateFlow.value
                Result.failure(RuntimeException("network error"))
            }

            val action = OnAddUnknownIsbnConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateAtInvocation.shouldNotBeNull().isAddingBook shouldBe true
        }

        @Test
        fun `records the resolved scan, requests the sheet to close and sends no event on success`() = runTest {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val editionId = 7
            stateFlow.value = ScanUiState(
                unknownIsbn = "9780451524935",
                unknownIsbnSheet = openSession(isbn = "9780451524935"),
            )
            val dependencies = stubDependencies(this)

            coEvery {
                addBookByIsbnUseCase(isbn = "9780451524935")
            } returns Result.success(
                IsbnLookupResult.Found(
                    book = book,
                    editionId = editionId,
                ),
            )

            val action = OnAddUnknownIsbnConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.unknownIsbnSheet.isClosing shouldBe true
            stateFlow.value.unknownIsbnSheet.modelOrNull.shouldNotBeNull().isbn shouldBe "9780451524935"
            stateFlow.value.isAddingBook shouldBe false
            stateFlow.value.unknownIsbn.shouldBeNull()

            val resolved = stateFlow.value.unknownIsbnSheet.dismissed().shouldNotBeNull()
            resolved.book shouldBe book
            resolved.editionId shouldBe editionId
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `keeps the sheet open and sends AddBookFailedEvent on failure`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbn = "9780451524935",
                unknownIsbnSheet = openSession(isbn = "9780451524935"),
            )
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
            stateFlow.value.unknownIsbnSheet.modelOrNull.shouldNotBeNull().isbn shouldBe "9780451524935"
            stateFlow.value.unknownIsbn shouldBe "9780451524935"
            stateFlow.value.unknownIsbnSheet.isClosing shouldBe false
            stateFlow.value.unknownIsbnSheet.dismissed().shouldBeNull()

            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<ScanEvent.AddBookFailedEvent>()
        }

        @Test
        fun `keeps the sheet open in the pending state without an event when the import is still pending`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbn = "9780451524935",
                unknownIsbnSheet = openSession(isbn = "9780451524935"),
            )
            val dependencies = stubDependencies(this)

            coEvery {
                addBookByIsbnUseCase(isbn = "9780451524935")
            } returns Result.failure(BookImportPendingException())

            val action = OnAddUnknownIsbnConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isAddingBook shouldBe false
            stateFlow.value.unknownIsbn shouldBe "9780451524935"
            stateFlow.value.unknownIsbnSheet.isClosing shouldBe false

            stateFlow.value.isImportPending shouldBe true
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `a non-pending failure sends only AddBookFailedEvent`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbn = "9780451524935",
                unknownIsbnSheet = openSession(isbn = "9780451524935"),
            )
            val dependencies = stubDependencies(this)

            coEvery {
                addBookByIsbnUseCase(isbn = "9780451524935")
            } returns Result.failure(IllegalStateException("boom"))

            val action = OnAddUnknownIsbnConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<ScanEvent.AddBookFailedEvent>()
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `isAddingBook stays true until the use case returns`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbn = "9780451524935",
                unknownIsbnSheet = openSession(isbn = "9780451524935"),
            )
            val dependencies = stubDependencies(this)
            val gate = CompletableDeferred<Unit>()

            coEvery {
                addBookByIsbnUseCase(isbn = "9780451524935")
            } coAnswers {
                gate.await()
                Result.failure(BookImportPendingException())
            }

            val action = OnAddUnknownIsbnConfirmedAction()

            // ----- Act -----
            val job = launch(UnconfinedTestDispatcher(testScheduler)) {
                action.execute(
                    dependencies = dependencies,
                    scope = scope,
                )
            }
            val isAddingWhileSuspended = stateFlow.value.isAddingBook
            val eventWhileSuspended = eventChannel.tryReceive().getOrNull()
            gate.complete(Unit)
            job.join()

            // ----- Assert -----
            isAddingWhileSuspended shouldBe true
            eventWhileSuspended.shouldBeNull()
            stateFlow.value.isAddingBook shouldBe false
        }
    

        @Test
        fun `clears a previous pending flag as soon as a new attempt starts`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbn = "9780451524935",
                unknownIsbnSheet = openSession(isbn = "9780451524935"),
                isImportPending = true,
            )
            val dependencies = stubDependencies(this)
            var pendingAtInvocation: Boolean? = null

            coEvery {
                addBookByIsbnUseCase(isbn = "9780451524935")
            } coAnswers {
                pendingAtInvocation = stateFlow.value.isImportPending
                Result.failure(RuntimeException("network error"))
            }

            val action = OnAddUnknownIsbnConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            pendingAtInvocation shouldBe false
        }

        @Test
        fun `a non-pending failure after a pending attempt leaves the pending flag false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbn = "9780451524935",
                unknownIsbnSheet = openSession(isbn = "9780451524935"),
                isImportPending = true,
            )
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
            stateFlow.value.isImportPending shouldBe false
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<ScanEvent.AddBookFailedEvent>()
        }

        @Test
        fun `success leaves the pending flag false after an earlier pending attempt`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbn = "9780451524935",
                unknownIsbnSheet = openSession(isbn = "9780451524935"),
                isImportPending = true,
            )
            val dependencies = stubDependencies(this)

            coEvery {
                addBookByIsbnUseCase(isbn = "9780451524935")
            } returns Result.success(
                IsbnLookupResult.Found(
                    book = PreviewData.baseBook,
                    editionId = 7,
                ),
            )

            val action = OnAddUnknownIsbnConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isImportPending shouldBe false
        }
}
}
