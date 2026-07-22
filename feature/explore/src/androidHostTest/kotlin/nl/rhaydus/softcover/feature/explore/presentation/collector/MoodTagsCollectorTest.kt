package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetMoodTagsUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

class MoodTagsCollectorTest {
    private lateinit var getMoodTagsUseCase: GetMoodTagsUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    @BeforeEach
    fun setUp() {
        getMoodTagsUseCase = mockk()
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<ExploreDependencies>(relaxed = true).also { mock ->
            every {
                mock.getMoodTagsUseCase
            } returns getMoodTagsUseCase
        }
    }

    private val moodTag = MoodTag(
        id = 1,
        label = "Cozy",
        slug = "cozy",
        bookCount = 10,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets moodTags and clears loadingMoodTags on success`() = runTest {
            // ----- Arrange -----
            coEvery {
                getMoodTagsUseCase()
            } returns Result.success(listOf(moodTag))
            val collector = MoodTagsCollector()

            // ----- Act -----
            collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            )

            // ----- Assert -----
            stateFlow.value.moodTags shouldBe listOf(moodTag)
            stateFlow.value.loadingMoodTags shouldBe false
        }

        @Test
        fun `clears loadingMoodTags but leaves moodTags empty instead of throwing on failure`() = runTest {
            // ----- Arrange -----
            coEvery {
                getMoodTagsUseCase()
            } returns Result.failure(RuntimeException("network error"))
            val collector = MoodTagsCollector()

            // ----- Act -----
            collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            )

            // ----- Assert -----
            stateFlow.value.moodTags shouldBe emptyList()
            stateFlow.value.loadingMoodTags shouldBe false
        }

        @Test
        fun `preserves other state fields when updating`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                searchText = "fantasy",
                loadingTrendingBooks = false,
            )
            coEvery {
                getMoodTagsUseCase()
            } returns Result.success(listOf(moodTag))
            val collector = MoodTagsCollector()

            // ----- Act -----
            collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            )

            // ----- Assert -----
            stateFlow.value.searchText shouldBe "fantasy"
            stateFlow.value.loadingTrendingBooks shouldBe false
        }
    }
}
