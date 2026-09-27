package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.feature.explore.domain.usecase.SearchForNameUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class OnRecentSearchChipClickedActionTest {
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
        fun `a key missing from recentSearchChips is a no-op`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val initialState = ExploreScreenUiState(recentSearchChips = ChipSet())
            stateFlow.value = initialState
            val action = OnRecentSearchChipClickedAction(key = "recent:missing")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState
            localVariablesFlow.value shouldBe ExploreLocalVariables()
        }

        @Test
        fun `a valid key produces the same state effect as OnQueryChangeAction with a zero delay`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                searchForNameUseCase(
                    name = any(),
                    sortMode = any(),
                )
            } returns Result.success(Unit)

            val directStateFlow = MutableStateFlow(ExploreScreenUiState())
            val directScope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables> = ActionScope(
                stateFlow = directStateFlow,
                localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
                eventChannel = Channel(Channel.BUFFERED),
            )

            OnQueryChangeAction(
                newQuery = "kotlin",
                searchDelay = 0.seconds,
            ).execute(
                dependencies = dependencies,
                scope = directScope,
            )

            stateFlow.value = ExploreScreenUiState(
                recentSearchChips = ChipSet(payloadByKey = persistentMapOf("recent:kotlin" to "kotlin")),
            )
            val action = OnRecentSearchChipClickedAction(key = "recent:kotlin")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.searchText shouldBe directStateFlow.value.searchText
            stateFlow.value.isLoading shouldBe directStateFlow.value.isLoading
            stateFlow.value.searchError shouldBe directStateFlow.value.searchError
            stateFlow.value.activeMoodFilter shouldBe directStateFlow.value.activeMoodFilter
            stateFlow.value.queriedBooksHasMore shouldBe directStateFlow.value.queriedBooksHasMore
        }
    }
}
