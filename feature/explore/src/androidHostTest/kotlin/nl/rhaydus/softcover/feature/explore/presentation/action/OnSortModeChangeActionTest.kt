package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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

class OnSortModeChangeActionTest {
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
        fun `only closes the sort menu when the mode matches the current sortMode`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(
                sortMode = ExploreSortMode.RELEVANCE,
                isSortMenuExpanded = true,
            )
            val stateBefore = stateFlow.value
            val action = OnSortModeChangeAction(mode = ExploreSortMode.RELEVANCE)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe stateBefore.copy(isSortMenuExpanded = false)
            coVerify(exactly = 0) {
                searchForNameUseCase(
                    name = any(),
                    sortMode = any(),
                )
            }
        }

        @Test
        fun `updates sortMode without triggering a search when searchText is empty`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(
                sortMode = ExploreSortMode.RELEVANCE,
                searchText = "",
            )
            val action = OnSortModeChangeAction(mode = ExploreSortMode.POPULARITY)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.sortMode shouldBe ExploreSortMode.POPULARITY
            stateFlow.value.isLoading shouldBe false
            coVerify(exactly = 0) {
                searchForNameUseCase(
                    name = any(),
                    sortMode = any(),
                )
            }
        }

        @Test
        fun `re-runs the search with the new sortMode, replacing the prior search job and closing the menu`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val priorJob = Job()
            stateFlow.value = stateFlow.value.copy(
                sortMode = ExploreSortMode.RELEVANCE,
                searchText = "dune",
                isSortMenuExpanded = true,
            )
            localVariablesFlow.value = localVariablesFlow.value.copy(
                queryJob = priorJob,
                searchResultsPage = 3,
            )

            coEvery {
                searchForNameUseCase(
                    name = "dune",
                    sortMode = ExploreSortMode.POPULARITY,
                )
            } returns Result.success(Unit)

            val action = OnSortModeChangeAction(mode = ExploreSortMode.POPULARITY)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.sortMode shouldBe ExploreSortMode.POPULARITY
            stateFlow.value.isSortMenuExpanded shouldBe false
            stateFlow.value.isLoading shouldBe false
            coVerify {
                searchForNameUseCase(
                    name = "dune",
                    sortMode = ExploreSortMode.POPULARITY,
                )
            }
            priorJob.isCancelled shouldBe true
            localVariablesFlow.value.queryJob.shouldNotBeNull()
            localVariablesFlow.value.queryJob shouldNotBe priorJob
            localVariablesFlow.value.searchResultsPage shouldBe 1
        }

        @Test
        fun `sets isLoading true and clears searchError while the re-run search is in flight`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(
                sortMode = ExploreSortMode.RELEVANCE,
                searchText = "dune",
                searchError = "previous error",
            )
            val gate = CompletableDeferred<Unit>()

            coEvery {
                searchForNameUseCase(
                    name = "dune",
                    sortMode = ExploreSortMode.POPULARITY,
                )
            } coAnswers {
                gate.await()
                Result.success(Unit)
            }

            val action = OnSortModeChangeAction(mode = ExploreSortMode.POPULARITY)

            // ----- Act -----
            val job = launch {
                action.execute(
                    dependencies = dependencies,
                    scope = scope,
                )
            }

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe true
            stateFlow.value.searchError shouldBe null

            gate.complete(Unit)
            job.join()
        }

        @Test
        fun `drops a pending load-more spinner while the re-run search is in flight`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(
                sortMode = ExploreSortMode.RELEVANCE,
                searchText = "dune",
                loadingMoreQueriedBooks = true,
            )
            val gate = CompletableDeferred<Unit>()

            coEvery {
                searchForNameUseCase(
                    name = "dune",
                    sortMode = ExploreSortMode.POPULARITY,
                )
            } coAnswers {
                gate.await()
                Result.success(Unit)
            }

            val action = OnSortModeChangeAction(mode = ExploreSortMode.POPULARITY)

            // ----- Act -----
            val job = launch {
                action.execute(
                    dependencies = dependencies,
                    scope = scope,
                )
            }

            // ----- Assert -----
            stateFlow.value.loadingMoreQueriedBooks shouldBe false

            gate.complete(Unit)
            job.join()
        }

        @Test
        fun `sets searchError and stops loading when the re-run search fails`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(
                sortMode = ExploreSortMode.RELEVANCE,
                searchText = "dune",
            )

            coEvery {
                searchForNameUseCase(
                    name = "dune",
                    sortMode = ExploreSortMode.POPULARITY,
                )
            } returns Result.failure(RuntimeException())

            val action = OnSortModeChangeAction(mode = ExploreSortMode.POPULARITY)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchError.shouldNotBeNull()
            stateFlow.value.isLoading shouldBe false
        }
    }
}
