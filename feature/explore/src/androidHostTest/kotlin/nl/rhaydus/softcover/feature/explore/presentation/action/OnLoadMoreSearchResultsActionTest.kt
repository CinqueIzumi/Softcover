package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.domain.usecase.SearchByMoodUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.SearchForNameUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

class OnLoadMoreSearchResultsActionTest {
    private lateinit var searchForNameUseCase: SearchForNameUseCase
    private lateinit var searchByMoodUseCase: SearchByMoodUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<ExploreLocalVariables>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    @BeforeEach
    fun setUp() {
        searchForNameUseCase = mockk()
        searchByMoodUseCase = mockk()
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
                mock.searchByMoodUseCase
            } returns searchByMoodUseCase

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
        fun `is a no-op when isLoading is true`() = runTest {
            // ----- Arrange -----
            dependencies = mockk(relaxed = true)
            stateFlow.value = stateFlow.value.copy(
                isLoading = true,
                loadingMoreQueriedBooks = false,
                queriedBooksHasMore = true,
            )
            val stateBefore = stateFlow.value
            val localVariablesBefore = localVariablesFlow.value

            // ----- Act -----
            OnLoadMoreSearchResultsAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe stateBefore
            localVariablesFlow.value shouldBe localVariablesBefore
            verify(exactly = 0) {
                dependencies.launch(any())
            }
        }

        @Test
        fun `is a no-op when loadingMoreQueriedBooks is true`() = runTest {
            // ----- Arrange -----
            dependencies = mockk(relaxed = true)
            stateFlow.value = stateFlow.value.copy(
                isLoading = false,
                loadingMoreQueriedBooks = true,
                queriedBooksHasMore = true,
            )
            val stateBefore = stateFlow.value
            val localVariablesBefore = localVariablesFlow.value

            // ----- Act -----
            OnLoadMoreSearchResultsAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe stateBefore
            localVariablesFlow.value shouldBe localVariablesBefore
            verify(exactly = 0) {
                dependencies.launch(any())
            }
        }

        @Test
        fun `is a no-op when queriedBooksHasMore is false`() = runTest {
            // ----- Arrange -----
            dependencies = mockk(relaxed = true)
            stateFlow.value = stateFlow.value.copy(
                isLoading = false,
                loadingMoreQueriedBooks = false,
                queriedBooksHasMore = false,
            )
            val stateBefore = stateFlow.value
            val localVariablesBefore = localVariablesFlow.value

            // ----- Act -----
            OnLoadMoreSearchResultsAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe stateBefore
            localVariablesFlow.value shouldBe localVariablesBefore
            verify(exactly = 0) {
                dependencies.launch(any())
            }
        }

        @Test
        fun `launches the fetch and advances searchResultsPage from the default starting page`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(searchText = "dune")

            coEvery {
                searchForNameUseCase(
                    name = any(),
                    sortMode = any(),
                    page = any(),
                )
            } returns Result.success(Unit)

            // ----- Act -----
            OnLoadMoreSearchResultsAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.searchResultsPage shouldBe 2
            localVariablesFlow.value.queryJob shouldNotBe null
        }

        @Test
        fun `advances searchResultsPage from a non-default starting page`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(searchText = "dune")
            localVariablesFlow.value = localVariablesFlow.value.copy(searchResultsPage = 3)

            coEvery {
                searchForNameUseCase(
                    name = any(),
                    sortMode = any(),
                    page = any(),
                )
            } returns Result.success(Unit)

            // ----- Act -----
            OnLoadMoreSearchResultsAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.searchResultsPage shouldBe 4
        }

        @Test
        fun `sets loadingMoreQueriedBooks true while the launched fetch is in flight and false after`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                dependencies = stubDependencies(this)
                stateFlow.value = stateFlow.value.copy(searchText = "dune")
                val gate = CompletableDeferred<Unit>()

                coEvery {
                    searchForNameUseCase(
                        name = any(),
                        sortMode = any(),
                        page = any(),
                    )
                } coAnswers {
                    gate.await()
                    Result.success(Unit)
                }

                // ----- Act -----
                OnLoadMoreSearchResultsAction.execute(
                    dependencies = dependencies,
                    scope = scope,
                )

                // ----- Assert -----
                stateFlow.value.loadingMoreQueriedBooks shouldBe true

                gate.complete(Unit)

                stateFlow.value.loadingMoreQueriedBooks shouldBe false
            }

        @Test
        fun `invokes executeSearch with the incremented page`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val searchText = "foundation"
            stateFlow.value = stateFlow.value.copy(
                searchText = searchText,
                sortMode = ExploreSortMode.POPULARITY,
            )
            localVariablesFlow.value = localVariablesFlow.value.copy(searchResultsPage = 2)

            coEvery {
                searchForNameUseCase(
                    name = any(),
                    sortMode = any(),
                    page = any(),
                )
            } returns Result.success(Unit)

            // ----- Act -----
            OnLoadMoreSearchResultsAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                searchForNameUseCase(
                    name = searchText,
                    sortMode = ExploreSortMode.POPULARITY,
                    page = 3,
                )
            }
        }

        @Test
        fun `invokes searchByMoodUseCase with the incremented page when a mood filter is active`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val mood = MoodTag(
                id = 1,
                label = "Cozy",
                slug = "cozy",
                bookCount = 10,
            )
            stateFlow.value = stateFlow.value.copy(activeMoodFilter = mood)

            coEvery {
                searchByMoodUseCase(
                    mood = any(),
                    page = any(),
                )
            } returns Result.success(Unit)

            // ----- Act -----
            OnLoadMoreSearchResultsAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                searchByMoodUseCase(
                    mood = mood,
                    page = 2,
                )
            }
            coVerify(exactly = 0) {
                searchForNameUseCase(
                    name = any(),
                    sortMode = any(),
                    page = any(),
                )
            }
        }

        @Test
        fun `invokes searchForNameUseCase and never searchByMoodUseCase when no mood filter is active`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val searchText = "dune"
            stateFlow.value = stateFlow.value.copy(
                searchText = searchText,
                activeMoodFilter = null,
            )

            coEvery {
                searchForNameUseCase(
                    name = any(),
                    sortMode = any(),
                    page = any(),
                )
            } returns Result.success(Unit)

            // ----- Act -----
            OnLoadMoreSearchResultsAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                searchForNameUseCase(
                    name = searchText,
                    sortMode = ExploreSortMode.RELEVANCE,
                    page = 2,
                )
            }
            coVerify(exactly = 0) {
                searchByMoodUseCase(
                    mood = any(),
                    page = any(),
                )
            }
        }

        @Test
        fun `sets fallback searchError and loadingMoreQueriedBooks false on failure`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(searchText = "dune")

            coEvery {
                searchForNameUseCase(
                    name = any(),
                    sortMode = any(),
                    page = any(),
                )
            } returns Result.failure(RuntimeException())

            // ----- Act -----
            OnLoadMoreSearchResultsAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchError shouldBe "We couldn't load results. Please try again."
            stateFlow.value.loadingMoreQueriedBooks shouldBe false
        }
    }
}
