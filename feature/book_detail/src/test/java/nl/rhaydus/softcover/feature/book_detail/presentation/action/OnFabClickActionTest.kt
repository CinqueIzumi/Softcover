package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnFabClickActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    @Nested
    inner class Execute {
        @Test
        fun `sets fabMenuExpanded to true when it starts as false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(fabMenuExpanded = false)
            val action = OnFabClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.fabMenuExpanded shouldBe true
        }

        @Test
        fun `sets fabMenuExpanded to false when it starts as true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(fabMenuExpanded = true)
            val action = OnFabClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.fabMenuExpanded shouldBe false
        }

        @Test
        fun `leaves all other state fields unchanged when toggling fabMenuExpanded`() = runTest {
            // ----- Arrange -----
            val initialState = BookDetailUiState(
                fabMenuExpanded = false,
                loadingBookDetails = false,
                showEditEditionSheet = true,
                showUpdateProgressSheet = true,
            )
            stateFlow.value = initialState
            val action = OnFabClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingBookDetails shouldBe initialState.loadingBookDetails
            stateFlow.value.book shouldBe initialState.book
            stateFlow.value.showEditEditionSheet shouldBe initialState.showEditEditionSheet
            stateFlow.value.showUpdateProgressSheet shouldBe initialState.showUpdateProgressSheet
        }

        @Test
        fun `toggles fabMenuExpanded back to original value on two consecutive calls`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(fabMenuExpanded = false)
            val action = OnFabClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.fabMenuExpanded shouldBe false
        }
    }
}
