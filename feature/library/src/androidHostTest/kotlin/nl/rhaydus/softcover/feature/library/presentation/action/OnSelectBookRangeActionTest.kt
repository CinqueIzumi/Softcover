package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

class OnSelectBookRangeActionTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `empty bookIds — no state change`() = runTest {
            // ----- Arrange -----
            val initialState = LibraryUiState(
                selectionMode = false,
                selectedBookIds = emptySet(),
                isRearranging = false,
            )
            stateFlow.value = initialState

            val action = OnSelectBookRangeAction(bookIds = emptyList())

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState
        }

        @Test
        fun `non-empty bookIds — sets selectionMode to true`() = runTest {
            // ----- Arrange -----
            val action = OnSelectBookRangeAction(bookIds = listOf(1, 2, 3))

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectionMode shouldBe true
        }

        @Test
        fun `non-empty bookIds — adds ids to selectedBookIds`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(selectedBookIds = setOf(10))

            val action = OnSelectBookRangeAction(bookIds = listOf(20, 30))

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedBookIds shouldBe setOf(10, 20, 30)
        }

        @Test
        fun `non-empty bookIds — clears isRearranging`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isRearranging = true)

            val action = OnSelectBookRangeAction(bookIds = listOf(5))

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isRearranging shouldBe false
        }

        @Test
        fun `empty bookIds — does not change isRearranging`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isRearranging = true)

            val action = OnSelectBookRangeAction(bookIds = emptyList())

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isRearranging shouldBe true
        }
    }
}
