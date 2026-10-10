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
import nl.rhaydus.softcover.core.book.domain.usecase.AddBookByIsbnUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.IsbnLookupResult
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
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

    @Nested
    inner class Execute {
        @Test
        fun `does nothing when there is no unknown isbn sheet`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(unknownIsbnSheet = null)
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
                unknownIsbnSheet = UnknownIsbnSheetUiModel(
                    isbn = "9780451524935",
                    scaffold = unknownIsbnSheetScaffoldFor(isAdding = false),
                ),
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
        fun `adds the book using the isbn of the unknown isbn sheet`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbnSheet = UnknownIsbnSheetUiModel(
                    isbn = "9781111111111",
                    scaffold = unknownIsbnSheetScaffoldFor(isAdding = false),
                ),
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
                unknownIsbnSheet = UnknownIsbnSheetUiModel(
                    isbn = "9780451524935",
                    scaffold = unknownIsbnSheetScaffoldFor(isAdding = false),
                ),
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
                unknownIsbnSheet = UnknownIsbnSheetUiModel(
                    isbn = "9780451524935",
                    scaffold = unknownIsbnSheetScaffoldFor(isAdding = false),
                ),
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
            stateFlow.value.isUnknownIsbnCloseRequested shouldBe true
            stateFlow.value.unknownIsbnSheet.shouldNotBeNull().isbn shouldBe "9780451524935"
            stateFlow.value.isAddingBook shouldBe false

            val resolved = stateFlow.value.resolvedUnknownIsbn.shouldNotBeNull()
            resolved.book shouldBe book
            resolved.editionId shouldBe editionId
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `keeps the sheet open and sends AddBookFailedEvent on failure`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbnSheet = UnknownIsbnSheetUiModel(
                    isbn = "9780451524935",
                    scaffold = unknownIsbnSheetScaffoldFor(isAdding = false),
                ),
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
            stateFlow.value.unknownIsbnSheet.shouldNotBeNull().isbn shouldBe "9780451524935"
            stateFlow.value.isUnknownIsbnCloseRequested shouldBe false
            stateFlow.value.resolvedUnknownIsbn.shouldBeNull()

            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<ScanEvent.AddBookFailedEvent>()
        }
    }
}
