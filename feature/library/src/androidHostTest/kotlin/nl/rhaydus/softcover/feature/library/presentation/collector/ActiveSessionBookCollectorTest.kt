package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.session.ActiveSession
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ActiveSessionBookCollectorTest {
    private lateinit var activeSession: MutableStateFlow<ActiveSession?>
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    @BeforeEach
    fun setUp() {
        activeSession = MutableStateFlow(null)
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.activeSessionController
            } returns mockk<ActiveSessionController> {
                every {
                    this@mockk.activeSession
                } returns this@ActiveSessionBookCollectorTest.activeSession
            }
        }
    }

    private fun sessionFor(bookId: Int): ActiveSession = mockk<ActiveSession> {
        every {
            book
        } returns mockk<Book> {
            every {
                id
            } returns bookId
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `mirrors the running session's book id and clears it when the session ends`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    ActiveSessionBookCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                stateFlow.value.activeSessionBookId shouldBe null

                // ----- Act -----
                activeSession.value = sessionFor(bookId = 7)
                val whileRunning = stateFlow.value.activeSessionBookId
                activeSession.value = null

                // ----- Assert -----
                whileRunning shouldBe 7
                stateFlow.value.activeSessionBookId shouldBe null
                job.cancel()
            }
    }
}
