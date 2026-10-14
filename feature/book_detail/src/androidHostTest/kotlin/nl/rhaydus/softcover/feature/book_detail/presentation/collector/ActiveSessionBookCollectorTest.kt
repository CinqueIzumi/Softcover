package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.presentation.session.ActiveSession
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ActiveSessionBookCollectorTest {
    private lateinit var activeSessionFlow: MutableStateFlow<ActiveSession?>
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        activeSessionFlow = MutableStateFlow(null)
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        val controller = mockk<ActiveSessionController>().also { mock ->
            every {
                mock.activeSession
            } returns activeSessionFlow
        }

        dependencies = mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.activeSessionController
            } returns controller
        }
    }

    private fun sessionFor(bookId: Int): ActiveSession = mockk<ActiveSession>().also { session ->
        every {
            session.book
        } returns mockk {
            every {
                id
            } returns bookId
        }
    }

    private fun TestScope.launchCollector() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            ActiveSessionBookCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `leaves activeSessionBookId null when no session is running`() = runTest {
            // ----- Arrange -----
            launchCollector()

            // ----- Act -----
            val result = stateFlow.value.activeSessionBookId

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `mirrors the running session's book id`() = runTest {
            // ----- Arrange -----
            launchCollector()

            // ----- Act -----
            activeSessionFlow.value = sessionFor(bookId = 42)

            // ----- Assert -----
            stateFlow.value.activeSessionBookId shouldBe 42
        }

        @Test
        fun `clears the book id when the session ends`() = runTest {
            // ----- Arrange -----
            launchCollector()
            activeSessionFlow.value = sessionFor(bookId = 42)

            // ----- Act -----
            activeSessionFlow.value = null

            // ----- Assert -----
            stateFlow.value.activeSessionBookId.shouldBeNull()
        }
    }
}
