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

class OnFilterSheetExpandedChangeActionTest {
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
        fun `sets isFilterSheetExpanded to true when expanded is true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isFilterSheetExpanded = false)
            val action = OnFilterSheetExpandedChangeAction(expanded = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isFilterSheetExpanded shouldBe true
        }

        @Test
        fun `sets isFilterSheetExpanded to false when expanded is false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isFilterSheetExpanded = true)
            val action = OnFilterSheetExpandedChangeAction(expanded = false)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isFilterSheetExpanded shouldBe false
        }

        @Test
        fun `preserves other state fields when updating isFilterSheetExpanded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isLoading = false,
                isFilterSheetExpanded = false,
            )
            val action = OnFilterSheetExpandedChangeAction(expanded = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.isFilterSheetExpanded shouldBe true
        }
    }
}
