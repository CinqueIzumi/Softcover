package nl.rhaydus.softcover.feature.reading.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

class OnDismissVerdictPromptActionTest {
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubBook(): Book = mockk()

    @Nested
    inner class Execute {
        @Test
        fun `sets verdictPromptBook to null when it was previously set to a book`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(verdictPromptBook = stubBook())

            // ----- Act -----
            OnDismissVerdictPromptAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictPromptBook shouldBe null
        }

        @Test
        fun `leaves verdictPromptBook null when it was already null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(verdictPromptBook = null)

            // ----- Act -----
            OnDismissVerdictPromptAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictPromptBook shouldBe null
        }

        @Test
        fun `preserves other state fields unchanged`() = runTest {
            // ----- Arrange -----
            val existingBooks = listOf(stubBook(), stubBook())
            stateFlow.value = ReadingScreenUiState(
                books = existingBooks,
                failedMutationBookIds = setOf(1, 2),
                verdictPromptBook = stubBook(),
            )

            // ----- Act -----
            OnDismissVerdictPromptAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.books shouldBe existingBooks
            stateFlow.value.failedMutationBookIds shouldBe setOf(1, 2)
        }
    }
}
