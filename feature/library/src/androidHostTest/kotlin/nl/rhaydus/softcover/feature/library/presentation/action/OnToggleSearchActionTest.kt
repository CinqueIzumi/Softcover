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

class OnToggleSearchActionTest {
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
        fun `sets isSearchActive to true when it was false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isSearchActive = false)
            val action = OnToggleSearchAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isSearchActive shouldBe true
        }

        @Test
        fun `sets isSearchActive to false when it was true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(isSearchActive = true)
            val action = OnToggleSearchAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isSearchActive shouldBe false
        }

        @Test
        fun `clears searchQuery to empty string when closing search`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isSearchActive = true,
                searchQuery = "tolkien",
            )
            val action = OnToggleSearchAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchQuery shouldBe ""
        }

        @Test
        fun `preserves searchQuery when opening search`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isSearchActive = false,
                searchQuery = "tolkien",
            )
            val action = OnToggleSearchAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchQuery shouldBe "tolkien"
        }

        @Test
        fun `searchQuery remains empty when opening search with no prior query`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isSearchActive = false,
                searchQuery = "",
            )
            val action = OnToggleSearchAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchQuery shouldBe ""
        }

        @Test
        fun `preserves other state fields when toggling search active`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isLoading = false,
                isSearchActive = false,
                searchQuery = "",
            )
            val action = OnToggleSearchAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.isSearchActive shouldBe true
        }
    }
}
