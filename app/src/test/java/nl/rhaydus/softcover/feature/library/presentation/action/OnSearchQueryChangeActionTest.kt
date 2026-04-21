package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSearchQueryChangeActionTest {

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
        fun `sets searchQuery to the provided query string`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(searchQuery = "")
            val action = OnSearchQueryChangeAction(query = "dune")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchQuery shouldBe "dune"
        }

        @Test
        fun `replaces an existing query with the new query string`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(searchQuery = "old query")
            val action = OnSearchQueryChangeAction(query = "new query")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchQuery shouldBe "new query"
        }

        @Test
        fun `sets searchQuery to an empty string when query is empty`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(searchQuery = "some text")
            val action = OnSearchQueryChangeAction(query = "")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchQuery shouldBe ""
        }

        @Test
        fun `sets searchQuery to a whitespace-only string when query is whitespace`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(searchQuery = "")
            val action = OnSearchQueryChangeAction(query = "   ")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchQuery shouldBe "   "
        }

        @Test
        fun `preserves other state fields when updating searchQuery`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                isLoading = false,
                isSearchActive = true,
                searchQuery = "",
            )
            val action = OnSearchQueryChangeAction(query = "fantasy")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.isSearchActive shouldBe true
            stateFlow.value.searchQuery shouldBe "fantasy"
        }
    }
}
