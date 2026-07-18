package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.domain.usecase.SearchByMoodUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

class OnMoodChipClickActionTest {
    private lateinit var searchByMoodUseCase: SearchByMoodUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<ExploreLocalVariables>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    private val mood = MoodTag(
        id = 1,
        label = "Cozy",
        slug = "cozy",
        bookCount = 10,
    )

    @BeforeEach
    fun setUp() {
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
        fun `cancels an active queryJob and clears it from local variables`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val queryJob = Job()
            localVariablesFlow.value = ExploreLocalVariables(queryJob = queryJob)

            coEvery {
                searchByMoodUseCase(mood = mood)
            } returns Result.success(Unit)

            val action = OnMoodChipClickAction(mood = mood)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            queryJob.isCancelled shouldBe true
            // The action starts a fresh search of its own, so by completion the local variable
            // holds that new job - not null, but no longer the cancelled one asserted above.
            localVariablesFlow.value.queryJob shouldNotBe null
            localVariablesFlow.value.queryJob shouldNotBe queryJob
        }

        @Test
        fun `immediately clears focus, sets the mood filter and starts loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(
                searchFocused = true,
                searchError = "previous error",
            )
            val gate = CompletableDeferred<Unit>()

            coEvery {
                searchByMoodUseCase(mood = mood)
            } coAnswers {
                gate.await()
                Result.success(Unit)
            }

            val action = OnMoodChipClickAction(mood = mood)

            // ----- Act -----
            val job = launch {
                action.execute(
                    dependencies = dependencies,
                    scope = scope,
                )
            }

            // ----- Assert -----
            stateFlow.value.searchFocused shouldBe false
            stateFlow.value.activeMoodFilter shouldBe mood
            stateFlow.value.isLoading shouldBe true
            stateFlow.value.searchError shouldBe null

            gate.complete(Unit)
            job.join()
        }

        @Test
        fun `sets isLoading false when the mood search succeeds`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                searchByMoodUseCase(mood = mood)
            } returns Result.success(Unit)

            val action = OnMoodChipClickAction(mood = mood)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
        }

        @Test
        fun `sets offline searchError and isLoading false when the mood search fails offline`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                searchByMoodUseCase(mood = mood)
            } returns Result.failure(OfflineException())

            val action = OnMoodChipClickAction(mood = mood)

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
        fun `sets fallback searchError and isLoading false when the mood search fails with an unknown error`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                searchByMoodUseCase(mood = mood)
            } returns Result.failure(RuntimeException())

            val action = OnMoodChipClickAction(mood = mood)

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
