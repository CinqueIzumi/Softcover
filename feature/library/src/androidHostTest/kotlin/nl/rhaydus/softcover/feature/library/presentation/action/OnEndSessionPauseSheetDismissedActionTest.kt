package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.PauseBookUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.uibinding.session.ConfirmedPause
import nl.rhaydus.softcover.core.uibinding.session.endSessionPauseSheetScaffold
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnEndSessionPauseSheetDismissedActionTest {
    private lateinit var activeSessionController: ActiveSessionController
    private lateinit var pauseBookUseCase: PauseBookUseCase
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val tabKey = "status-2"

    @BeforeEach
    fun setUp() {
        activeSessionController = mockk(relaxed = true)
        pauseBookUseCase = mockk(relaxed = true)
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.activeSessionController
            } returns activeSessionController

            every {
                mock.pauseBookUseCase
            } returns pauseBookUseCase
        }
    }

    private fun buildBook(id: Int): Book = mockk<Book>(relaxed = true).also { book ->
        every {
            book.id
        } returns id
    }

    @Nested
    inner class Execute {
        @Test
        fun `a confirmed pause stops the session and pauses the books`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 1)
            stateFlow.value = LibraryUiState(
                booksByTab = persistentMapOf(tabKey to persistentListOf(book)),
                activeSessionBookId = 1,
                endSessionPauseSheet = SheetSession.Open(
                    model = endSessionPauseSheetScaffold(bookTitle = "Title"),
                    pending = ConfirmedPause(bookIds = persistentSetOf(1)),
                ),
            )

            coEvery {
                pauseBookUseCase(book = book)
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            OnEndSessionPauseSheetDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 1) { activeSessionController.stop() }
            coVerify(exactly = 1) { pauseBookUseCase(book = book) }
            stateFlow.value.endSessionPauseSheet shouldBe SheetSession.Closed
            stateFlow.value.endSessionPauseBookIds shouldBe emptySet()
        }

        @Test
        fun `a dismissal without a confirmed pause neither stops the session nor pauses`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 1)
            stateFlow.value = LibraryUiState(
                booksByTab = persistentMapOf(tabKey to persistentListOf(book)),
                endSessionPauseBookIds = persistentSetOf(1),
                endSessionPauseSheet = SheetSession.Open(model = endSessionPauseSheetScaffold(bookTitle = "Title")),
            )

            // ----- Act -----
            OnEndSessionPauseSheetDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 0) { activeSessionController.stop() }
            coVerify(exactly = 0) { pauseBookUseCase(book = any()) }
            stateFlow.value.endSessionPauseSheet shouldBe SheetSession.Closed
            stateFlow.value.endSessionPauseBookIds shouldBe emptySet()
        }
    }
}
