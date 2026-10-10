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

class OnBulkAddToListSheetShownActionTest {
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
        fun `sets isBulkAddToListSheetShown to true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isBulkAddToListSheetShown = false,
            )

            // ----- Act -----
            OnBulkAddToListSheetShownAction(shown = true).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isBulkAddToListSheetShown shouldBe true
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `sets isBulkAddToListSheetShown to false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isBulkAddToListSheetShown = true,
            )

            // ----- Act -----
            OnBulkAddToListSheetShownAction(shown = false).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isBulkAddToListSheetShown shouldBe false
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `clears isCreateListPending when shown is true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isCreateListPending = true,
            )

            // ----- Act -----
            OnBulkAddToListSheetShownAction(shown = true).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isCreateListPending shouldBe false
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `keeps isCreateListPending when shown is false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isCreateListPending = true,
            )

            // ----- Act -----
            OnBulkAddToListSheetShownAction(shown = false).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isCreateListPending shouldBe true
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `leaves isCreateListPending false when shown is true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isCreateListPending = false,
            )

            // ----- Act -----
            OnBulkAddToListSheetShownAction(shown = true).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isCreateListPending shouldBe false
            eventChannel.tryReceive().getOrNull() shouldBe null
        }
    }
}
