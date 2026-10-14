package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.PauseBookUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.uibinding.session.ConfirmedPause
import nl.rhaydus.softcover.core.uibinding.session.endSessionPauseSheetScaffold
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnEndSessionPauseSheetDismissedActionTest {
    private lateinit var pauseBookUseCase: PauseBookUseCase
    private lateinit var activeSessionController: ActiveSessionController
    private lateinit var book: Book
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        pauseBookUseCase = mockk()
        activeSessionController = mockk(relaxed = true)
        book = mockk<Book>().also { mock ->
            every {
                mock.id
            } returns 42

            every {
                mock.userBook
            } returns null
        }
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)

        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.pauseBookUseCase
            } returns pauseBookUseCase

            every {
                mock.activeSessionController
            } returns activeSessionController

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

    private fun stubPauseSuccess() {
        coEvery {
            pauseBookUseCase(
                book = book,
                editionId = any(),
            )
        } returns Result.success(ShelfMutationOutcome.Applied)
    }

    private val confirmedPause = ConfirmedPause(bookIds = persistentSetOf(42))

    private fun closedSheetState(pending: ConfirmedPause?): BookDetailUiState = buildBookDetailUiState().copy(
        book = book,
        activeSessionBookId = 42,
        endSessionPauseRequested = true,
        endSessionPauseSheet = SheetSession.Open(
            model = endSessionPauseSheetScaffold(bookTitle = "Dune"),
            pending = pending,
        ),
    )

    @Nested
    inner class Execute {
        @Test
        fun `stops the running session when the pause was confirmed`() = runTest {
            // ----- Arrange -----
            stateFlow.value = closedSheetState(pending = confirmedPause)
            val dependencies = stubDependencies(this)
            stubPauseSuccess()

            // ----- Act -----
            OnEndSessionPauseSheetDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 1) {
                activeSessionController.stop()
            }
        }

        @Test
        fun `pauses even though the session book id is still this book`() = runTest {
            // ----- Arrange -----
            stateFlow.value = closedSheetState(pending = confirmedPause)
            val dependencies = stubDependencies(this)
            stubPauseSuccess()

            // ----- Act -----
            OnEndSessionPauseSheetDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                pauseBookUseCase(
                    book = book,
                    editionId = any(),
                )
            }
        }

        @Test
        fun `does neither stop nor pause when the reader chose to keep reading`() = runTest {
            // ----- Arrange -----
            stateFlow.value = closedSheetState(pending = null)
            val dependencies = stubDependencies(this)

            // ----- Act -----
            OnEndSessionPauseSheetDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 0) {
                activeSessionController.stop()
            }
            coVerify(exactly = 0) {
                pauseBookUseCase(
                    book = any(),
                    editionId = any(),
                )
            }
        }

        @Test
        fun `closes the sheet session and clears the request when confirmed`() = runTest {
            // ----- Arrange -----
            stateFlow.value = closedSheetState(pending = confirmedPause)
            val dependencies = stubDependencies(this)
            stubPauseSuccess()

            // ----- Act -----
            OnEndSessionPauseSheetDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.endSessionPauseSheet shouldBe SheetSession.Closed
            stateFlow.value.endSessionPauseRequested shouldBe false
        }

        @Test
        fun `closes the sheet session and clears the request when the reader keeps reading`() = runTest {
            // ----- Arrange -----
            stateFlow.value = closedSheetState(pending = null)
            val dependencies = stubDependencies(this)

            // ----- Act -----
            OnEndSessionPauseSheetDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.endSessionPauseSheet shouldBe SheetSession.Closed
            stateFlow.value.endSessionPauseRequested shouldBe false
        }

        @Test
        fun `does not stop or pause when the book is not loaded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = closedSheetState(pending = confirmedPause).copy(book = null)
            val dependencies = stubDependencies(this)

            // ----- Act -----
            OnEndSessionPauseSheetDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 0) {
                activeSessionController.stop()
            }
        }
    }
}
