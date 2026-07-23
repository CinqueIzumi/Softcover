package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

class OnSearchDismissedActionTest {
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<ExploreLocalVariables>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        localVariablesFlow = MutableStateFlow(ExploreLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `sets searchFocused to false when it was previously true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(searchFocused = true)

            // ----- Act -----
            OnSearchDismissedAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchFocused shouldBe false
        }

        @Test
        fun `sets searchFocused to false when it was already false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(searchFocused = false)

            // ----- Act -----
            OnSearchDismissedAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchFocused shouldBe false
        }

        @Test
        fun `leaves searchText and queriedBooks untouched`() = runTest {
            // ----- Arrange -----
            val existingBooks: List<Book> = listOf(mockk())
            stateFlow.value = stateFlow.value.copy(
                searchFocused = true,
                searchText = "dune",
                queriedBooks = existingBooks,
            )

            // ----- Act -----
            OnSearchDismissedAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchText shouldBe "dune"
            stateFlow.value.queriedBooks shouldBe existingBooks
        }
    }
}
