package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnEditionSelectedActionTest {
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
        fun `writes the edition id into selectedEditionId`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(selectedEditionId = null)
            val action = OnEditionSelectedAction(editionId = 42)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedEditionId shouldBe 42
        }

        @Test
        fun `overwrites a previously selected edition id`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(selectedEditionId = 7)
            val action = OnEditionSelectedAction(editionId = 42)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedEditionId shouldBe 42
        }

        @Test
        fun `does not mutate any other state fields`() = runTest {
            // ----- Arrange -----
            val initialState = BookDetailUiState(
                loadingBookDetails = false,
                showEditEditionSheet = true,
                fabMenuExpanded = true,
                selectedEditionId = null,
            )
            stateFlow.value = initialState
            val action = OnEditionSelectedAction(editionId = 42)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState.copy(selectedEditionId = 42)
        }
    }
}
