package nl.rhaydus.softcover.feature.reading.presentation.action

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.session.ActiveSession
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnStartSessionClickActionTest {
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var controller: ActiveSessionController
    private lateinit var activeSession: MutableStateFlow<ActiveSession?>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    private val book = mockk<Book>()

    @BeforeEach
    fun setUp() {
        activeSession = MutableStateFlow(null)
        controller = mockk(relaxed = true)

        every {
            controller.activeSession
        } returns activeSession

        dependencies = mockk(relaxed = true)

        every {
            dependencies.activeSessionController
        } returns controller

        scope = ActionScope(
            stateFlow = MutableStateFlow(ReadingScreenUiState()),
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `starts the book when no session is active`() = runTest {
            // ----- Arrange -----
            activeSession.value = null

            // ----- Act -----
            OnStartSessionClickAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 1) { controller.start(book) }
        }

        @Test
        fun `does not start a second session while one is active`() = runTest {
            // ----- Arrange -----
            activeSession.value = mockk<ActiveSession>()

            // ----- Act -----
            OnStartSessionClickAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 0) { controller.start(any()) }
        }
    }
}
