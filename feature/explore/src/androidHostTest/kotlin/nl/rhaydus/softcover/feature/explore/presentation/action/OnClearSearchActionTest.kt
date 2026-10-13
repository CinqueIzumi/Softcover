package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.domain.usecase.ClearSearchResultsUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
        fun `leaves search entirely, dropping chrome, query, mood filter and in-flight fetch`() = runTest {
            // ----- Arrange -----
            val queryJob = Job()
            stateFlow.value = stateFlow.value.copy(
                searchFocused = true,
                isSortMenuExpanded = true,
                searchText = "dune",
                queriedBooks = persistentListOf(mockk()),
                activeMoodFilter = MoodTag(
                    id = 1,
                    label = "Cozy",
                    slug = "cozy",
                    bookCount = 10,
                ),
            )
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
            val state = stateFlow.value
            state.searchFocused shouldBe false
            state.isSortMenuExpanded shouldBe false
            state.searchText shouldBe ""
            state.queriedBooks shouldBe emptyList()
            state.activeMoodFilter shouldBe null
            queryJob.isCancelled shouldBe true
            localVariablesFlow.value.queryJob shouldBe null
            localVariablesFlow.value.searchResultsPage shouldBe 1
        }

        @Test
        fun `clears the persisted search results exactly once`() = runTest {
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
