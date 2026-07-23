package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.domain.usecase.ClearSearchResultsUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

class OnClearSearchActionTest {
    private lateinit var clearSearchResultsUseCase: ClearSearchResultsUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<ExploreLocalVariables>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    @BeforeEach
    fun setUp() {
        clearSearchResultsUseCase = mockk()
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        localVariablesFlow = MutableStateFlow(ExploreLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = Channel(Channel.BUFFERED),
        )

        coEvery {
            clearSearchResultsUseCase()
        } returns Result.success(Unit)

        dependencies = mockk<ExploreDependencies>(relaxed = true).also { mock ->
            every {
                mock.clearSearchResultsUseCase
            } returns clearSearchResultsUseCase
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `sets searchFocused to false when it was previously true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(searchFocused = true)

            // ----- Act -----
            OnClearSearchAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchFocused shouldBe false
        }

        @Test
        fun `clears the query text and queried results`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                searchText = "dune",
                queriedBooks = listOf(mockk()),
            )

            // ----- Act -----
            OnClearSearchAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchText shouldBe ""
            stateFlow.value.queriedBooks shouldBe emptyList()
        }

        @Test
        fun `clears an active mood filter`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                activeMoodFilter = MoodTag(
                    id = 1,
                    label = "Cozy",
                    slug = "cozy",
                    bookCount = 10,
                ),
            )

            // ----- Act -----
            OnClearSearchAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.activeMoodFilter shouldBe null
        }

        @Test
        fun `cancels an active queryJob and resets searchResultsPage to 1`() = runTest {
            // ----- Arrange -----
            val queryJob = Job()
            localVariablesFlow.value = ExploreLocalVariables(
                queryJob = queryJob,
                searchResultsPage = 3,
            )

            // ----- Act -----
            OnClearSearchAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            queryJob.isCancelled shouldBe true
            localVariablesFlow.value.queryJob shouldBe null
            localVariablesFlow.value.searchResultsPage shouldBe 1
        }

        @Test
        fun `calls clearSearchResultsUseCase exactly once`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(searchText = "dune")

            // ----- Act -----
            OnClearSearchAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                clearSearchResultsUseCase()
            }
        }
    }
}
