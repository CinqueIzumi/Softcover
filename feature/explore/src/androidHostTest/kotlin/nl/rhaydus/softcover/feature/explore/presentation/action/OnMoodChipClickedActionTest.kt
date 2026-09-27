package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.domain.usecase.SearchByMoodUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

class OnMoodChipClickedActionTest {
    private lateinit var searchByMoodUseCase: SearchByMoodUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<ExploreLocalVariables>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    private val cozy = MoodTag(
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
        fun `a key missing from moodTagByChipKey is a no-op`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val initialState = ExploreScreenUiState(moodTagByChipKey = emptyMap())
            stateFlow.value = initialState
            val action = OnMoodChipClickedAction(key = "mood:missing")

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
        fun `a valid key produces the same state effect as OnMoodChipClickAction`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                searchByMoodUseCase(mood = cozy)
            } returns Result.success(Unit)

            val directStateFlow = MutableStateFlow(ExploreScreenUiState())
            val directScope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables> = ActionScope(
                stateFlow = directStateFlow,
                localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
                eventChannel = Channel(Channel.BUFFERED),
            )

            OnMoodChipClickAction(mood = cozy).execute(
                dependencies = dependencies,
                scope = directScope,
            )

            stateFlow.value = ExploreScreenUiState(moodTagByChipKey = mapOf("mood:1" to cozy))
            val action = OnMoodChipClickedAction(key = "mood:1")

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
            stateFlow.value.searchFocused shouldBe directStateFlow.value.searchFocused
        }
    }
}
