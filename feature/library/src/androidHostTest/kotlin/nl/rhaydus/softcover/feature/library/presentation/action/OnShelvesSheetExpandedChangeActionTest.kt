package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnShelvesSheetExpandedChangeActionTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var eventChannel: Channel<LibraryEvent>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(LibraryUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = eventChannel,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `sets isShelvesSheetExpanded to true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isShelvesSheetExpanded = false,
            )

            // ----- Act -----
            OnShelvesSheetExpandedChangeAction(expanded = true).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isShelvesSheetExpanded shouldBe true
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `sets isShelvesSheetExpanded to false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isShelvesSheetExpanded = true,
            )

            // ----- Act -----
            OnShelvesSheetExpandedChangeAction(expanded = false).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isShelvesSheetExpanded shouldBe false
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `clears isManageShelvesPending when expanded is true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isManageShelvesPending = true,
            )

            // ----- Act -----
            OnShelvesSheetExpandedChangeAction(expanded = true).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isManageShelvesPending shouldBe false
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `keeps isManageShelvesPending when expanded is false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isManageShelvesPending = true,
            )

            // ----- Act -----
            OnShelvesSheetExpandedChangeAction(expanded = false).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isManageShelvesPending shouldBe true
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `leaves isManageShelvesPending false when expanded is true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isManageShelvesPending = false,
            )

            // ----- Act -----
            OnShelvesSheetExpandedChangeAction(expanded = true).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isManageShelvesPending shouldBe false
            eventChannel.tryReceive().getOrNull() shouldBe null
        }
    }
}
