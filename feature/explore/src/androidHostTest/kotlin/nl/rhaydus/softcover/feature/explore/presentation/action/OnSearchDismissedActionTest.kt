package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
        fun `unfocuses and closes the sort menu while keeping the query and its results`() = runTest {
            // ----- Arrange -----
            val existingBooks: List<Book> = listOf(mockk())
            stateFlow.value = stateFlow.value.copy(
                searchFocused = true,
                isSortMenuExpanded = true,
                searchText = "dune",
                queriedBooks = existingBooks,
            )

            // ----- Act -----
            OnSearchDismissedAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val state = stateFlow.value
            state.searchFocused shouldBe false
            state.isSortMenuExpanded shouldBe false
            state.searchText shouldBe "dune"
            state.queriedBooks shouldBe existingBooks
        }
    }
}
