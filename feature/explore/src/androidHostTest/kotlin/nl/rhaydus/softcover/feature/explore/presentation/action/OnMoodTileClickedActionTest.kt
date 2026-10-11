package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.domain.usecase.SearchByMoodUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnMoodTileClickedActionTest {
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

    private val gritty = MoodTag(
        id = 2,
        label = "Gritty",
        slug = "gritty",
        bookCount = 4,
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
        fun `a key that matches no mood tag is a no-op`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val initialState = ExploreScreenUiState(moodTags = persistentListOf(cozy))
            stateFlow.value = initialState
            val action = OnMoodTileClickedAction(key = 99)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState
            localVariablesFlow.value shouldBe ExploreLocalVariables()
            coVerify(exactly = 0) { searchByMoodUseCase(mood = any()) }
        }

        @Test
        fun `a matching key searches by that mood and makes it the active filter`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = ExploreScreenUiState(moodTags = persistentListOf(cozy, gritty))

            coEvery {
                searchByMoodUseCase(mood = gritty)
            } returns Result.success(Unit)

            val action = OnMoodTileClickedAction(key = 2)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.activeMoodFilter shouldBe gritty
            coVerify(exactly = 1) { searchByMoodUseCase(mood = gritty) }
            coVerify(exactly = 0) { searchByMoodUseCase(mood = cozy) }
        }
    }
}
