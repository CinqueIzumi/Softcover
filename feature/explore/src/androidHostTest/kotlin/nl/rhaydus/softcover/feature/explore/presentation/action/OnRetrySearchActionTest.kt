package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import nl.rhaydus.softcover.feature.explore.domain.usecase.SearchForNameUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnRetrySearchActionTest {
    private lateinit var searchForNameUseCase: SearchForNameUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<ExploreLocalVariables>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    @BeforeEach
    fun setUp() {
        searchForNameUseCase = mockk()
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        localVariablesFlow = MutableStateFlow(ExploreLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: TestScope): ExploreDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<ExploreDependencies>(relaxed = true).also { mock ->
            every {
                mock.searchForNameUseCase
            } returns searchForNameUseCase

            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher

            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `sets isLoading to true and clears searchError before executing the search`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(
                searchText = "dune",
                searchError = "previous error",
                isLoading = false,
            )

            var stateAtSearchTime: ExploreScreenUiState? = null

            coEvery {
                searchForNameUseCase(
                    name = any(),
                    sortMode = any(),
                )
            } answers {
                stateAtSearchTime = stateFlow.value
                Result.success(Unit)
            }

            // ----- Act -----
            OnRetrySearchAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateAtSearchTime.shouldNotBeNull().isLoading shouldBe true
            stateAtSearchTime.shouldNotBeNull().searchError shouldBe null
        }

        @Test
        fun `drops a pending load-more spinner before executing the search`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(
                searchText = "dune",
                loadingMoreQueriedBooks = true,
            )

            var stateAtSearchTime: ExploreScreenUiState? = null

            coEvery {
                searchForNameUseCase(
                    name = any(),
                    sortMode = any(),
                )
            } answers {
                stateAtSearchTime = stateFlow.value
                Result.success(Unit)
            }

            // ----- Act -----
            OnRetrySearchAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateAtSearchTime.shouldNotBeNull().loadingMoreQueriedBooks shouldBe false
        }

        @Test
        fun `searches the current searchText and tracks the new queryJob`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val searchText = "foundation"
            stateFlow.value = stateFlow.value.copy(searchText = searchText)

            coEvery {
                searchForNameUseCase(
                    name = searchText,
                    sortMode = any(),
                )
            } returns Result.success(Unit)

            // ----- Act -----
            OnRetrySearchAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                searchForNameUseCase(
                    name = searchText,
                    sortMode = ExploreSortMode.RELEVANCE,
                )
            }
            localVariablesFlow.value.queryJob.shouldNotBeNull()
        }

        @Test
        fun `on success leaves searchError as null and sets isLoading to false`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(
                searchText = "dune",
                searchError = "previous error",
            )

            coEvery {
                searchForNameUseCase(
                    name = any(),
                    sortMode = any(),
                )
            } returns Result.success(Unit)

            // ----- Act -----
            OnRetrySearchAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchError shouldBe null
            stateFlow.value.isLoading shouldBe false
        }

        @Test
        fun `on failure sets searchError and isLoading to false`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(searchText = "dune")

            coEvery {
                searchForNameUseCase(
                    name = any(),
                    sortMode = any(),
                )
            } returns Result.failure(RuntimeException())

            // ----- Act -----
            OnRetrySearchAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchError.shouldNotBeNull()
            stateFlow.value.isLoading shouldBe false
        }
    }
}
