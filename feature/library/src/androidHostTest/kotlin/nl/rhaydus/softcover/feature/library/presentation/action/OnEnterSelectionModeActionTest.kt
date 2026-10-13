package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.selectionCountOf
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnEnterSelectionModeActionTest {
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
        fun `seeds the selection with the given book and leaves rearrange mode`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isRearranging = true)

            // ----- Act -----
            OnEnterSelectionModeAction(bookId = 42).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectionMode shouldBe true
            stateFlow.value.selectedBookIds shouldBe setOf(42)
            stateFlow.value.isRearranging shouldBe false
        }

        @Test
        fun `starts with an empty selection when no book is given and leaves rearrange mode`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isRearranging = true)

            // ----- Act -----
            OnEnterSelectionModeAction(bookId = null).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectionMode shouldBe true
            stateFlow.value.selectedBookIds shouldBe emptySet()
            stateFlow.value.isRearranging shouldBe false
        }


        @Test
        fun `seeding one book counts one selected`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState()

            // ----- Act -----
            OnEnterSelectionModeAction(bookId = 4).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectionCount shouldBe selectionCountOf(count = 1)
        }

        @Test
        fun `entering without a book resets a stale count to zero`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectedBookIds = persistentSetOf(1, 2),
                selectionCount = selectionCountOf(count = 2),
            )

            // ----- Act -----
            OnEnterSelectionModeAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectionCount shouldBe selectionCountOf(count = 0)
        }
    }
}
