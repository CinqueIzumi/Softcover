package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnOpenTagEditorActionTest {

    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {

        @Test
        fun `sets showTagEditorSheet to true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(showTagEditorSheet = false)
            val action = OnOpenTagEditorAction()

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.showTagEditorSheet shouldBe true
        }

        @Test
        fun `clears tagEditorInput`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(tagEditorInput = "fantasy")
            val action = OnOpenTagEditorAction()

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.tagEditorInput shouldBe ""
        }

        @Test
        fun `does not mutate any other state field`() = runTest {
            // ----- Arrange -----
            val initialState = stateFlow.value
            val action = OnOpenTagEditorAction()

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value shouldBe initialState.copy(
                showTagEditorSheet = true,
                tagEditorInput = "",
            )
        }
    }
}
