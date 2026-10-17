package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.domain.usecase.SearchForNameUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

class OnQueryChangeActionTest {
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
        fun `a non-empty query starts a debounced search and resets the stale search state`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(
                searchError = "previous error",
                activeMoodFilter = MoodTag(
                    id = 1,
                    label = "Cozy",
                    slug = "cozy",
                    bookCount = 10,
                ),
            )

            coEvery {
                searchForNameUseCase(
                    name = any(),
                    sortMode = any(),
                )
            } returns Result.success(Unit)

            val action = OnQueryChangeAction(
                newQuery = "kotlin",
                searchDelay = 1_000_000.milliseconds,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            // With a very large delay the launched job won't finish, so isLoading stays true
            stateFlow.value.searchText shouldBe "kotlin"
            stateFlow.value.isLoading shouldBe true
            stateFlow.value.searchError shouldBe null
            stateFlow.value.activeMoodFilter shouldBe null
            localVariablesFlow.value.queryJob.shouldNotBeNull()
        }

        @Test
        fun `a non-empty query drops a pending load-more spinner`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(loadingMoreQueriedBooks = true)

            coEvery {
                searchForNameUseCase(
                    name = any(),
                    sortMode = any(),
                )
            } returns Result.success(Unit)

            val action = OnQueryChangeAction(
                newQuery = "kotlin",
                searchDelay = 1_000_000.milliseconds,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingMoreQueriedBooks shouldBe false
        }

        @Test
        fun `stops loading once the search for a non-empty query succeeds`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                searchForNameUseCase(
                    name = any(),
                    sortMode = any(),
                )
            } returns Result.success(Unit)

            val action = OnQueryChangeAction(
                newQuery = "android",
                searchDelay = 0.milliseconds,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.searchError shouldBe null
        }

        @Test
        fun `an empty query drops the results, error, mood filter and search job`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(
                queriedBooks = persistentListOf(mockk()),
                searchError = "previous error",
                activeMoodFilter = MoodTag(
                    id = 1,
                    label = "Cozy",
                    slug = "cozy",
                    bookCount = 10,
                ),
            )

            val action = OnQueryChangeAction(
                newQuery = "",
                searchDelay = 0.milliseconds,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.queriedBooks shouldBe emptyList()
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.searchError shouldBe null
            stateFlow.value.activeMoodFilter shouldBe null
            localVariablesFlow.value.queryJob shouldBe null
        }

        @Test
        fun `failed search with OfflineException sets offline searchError and isLoading to false`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                searchForNameUseCase(
                    name = any(),
                    sortMode = any(),
                )
            } returns Result.failure(OfflineException())

            val action = OnQueryChangeAction(
                newQuery = "dune",
                searchDelay = 0.milliseconds,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchError shouldBe "You're offline. Check your connection and try again."
            stateFlow.value.isLoading shouldBe false
        }

        @Test
        fun `failed search with unknown error sets fallback searchError and isLoading to false`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                searchForNameUseCase(
                    name = any(),
                    sortMode = any(),
                )
            } returns Result.failure(RuntimeException())

            val action = OnQueryChangeAction(
                newQuery = "dune",
                searchDelay = 0.milliseconds,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchError shouldBe "We couldn't load results. Please try again."
            stateFlow.value.isLoading shouldBe false
        }
    }
}
