package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

class MoodChipsCollectorTest {
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    private val cozy = MoodTag(
        id = 1,
        label = "cosy",
        slug = "cosy",
        bookCount = 10,
    )

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<ExploreDependencies>(relaxed = true)
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets moodChips and moodTagByChipKey from moodTags`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = MoodChipsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = ExploreScreenUiState(moodTags = listOf(cozy))

            // ----- Assert -----
            stateFlow.value.moodChips.map { it.key } shouldBe listOf("mood:1")
            stateFlow.value.moodChips.map { it.label } shouldBe listOf("Cosy")
            stateFlow.value.moodTagByChipKey shouldBe mapOf("mood:1" to cozy)
            job.cancel()
        }

        @Test
        fun `updates to empty chips and map when moodTags becomes empty`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(moodTags = listOf(cozy))
            val collector = MoodChipsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(moodTags = emptyList())

            // ----- Assert -----
            stateFlow.value.moodChips shouldBe persistentListOf()
            stateFlow.value.moodTagByChipKey shouldBe emptyMap()
            job.cancel()
        }

        @Test
        fun `preserves other state fields when updating`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                searchText = "fantasy",
                loadingTrendingBooks = false,
            )
            val collector = MoodChipsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(moodTags = listOf(cozy))

            // ----- Assert -----
            stateFlow.value.searchText shouldBe "fantasy"
            stateFlow.value.loadingTrendingBooks shouldBe false
            job.cancel()
        }

        @Test
        fun `unrelated state change that leaves moodTags equal does not retrigger recompute`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = ExploreScreenUiState(moodTags = listOf(cozy))
                val collector = MoodChipsCollector()
                val job = launch { collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                ) }
                val chipsAfterFirstEmit = stateFlow.value.moodChips
                val mapAfterFirstEmit = stateFlow.value.moodTagByChipKey

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(isLoading = true)

                // ----- Assert -----
                (stateFlow.value.moodChips === chipsAfterFirstEmit) shouldBe true
                (stateFlow.value.moodTagByChipKey === mapAfterFirstEmit) shouldBe true
                job.cancel()
            }
    }
}
