package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnLayoutMenuExpandedChangeActionTest {

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
        fun `sets isLayoutMenuExpanded to true when expanded is true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isLayoutMenuExpanded = false)
            val action = OnLayoutMenuExpandedChangeAction(expanded = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLayoutMenuExpanded shouldBe true
        }

        @Test
        fun `sets isLayoutMenuExpanded to false when expanded is false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isLayoutMenuExpanded = true)
            val action = OnLayoutMenuExpandedChangeAction(expanded = false)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLayoutMenuExpanded shouldBe false
        }

        @Test
        fun `preserves other state fields when updating isLayoutMenuExpanded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isLoading = false,
                isLayoutMenuExpanded = false,
                gridLayout = LibraryGridLayout.LIST_COMPACT,
            )
            val action = OnLayoutMenuExpandedChangeAction(expanded = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.gridLayout shouldBe LibraryGridLayout.LIST_COMPACT
            stateFlow.value.isLayoutMenuExpanded shouldBe true
        }

        @Test
        fun `setting the same value does not change other state fields`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isLayoutMenuExpanded = true)
            val action = OnLayoutMenuExpandedChangeAction(expanded = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLayoutMenuExpanded shouldBe true
        }
    }
}
