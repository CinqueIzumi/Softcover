package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.mood.MoodTileInk
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MoodTilesCollectorTest {
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    private val cosy = MoodTag(
        id = 1,
        label = "cosy",
        slug = "cosy",
        bookCount = 10,
    )
    private val dark = MoodTag(
        id = 2,
        label = "dark",
        slug = "dark",
        bookCount = 20,
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
        fun `sets moodTiles in tag order with index-driven inks`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                MoodTilesCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                moodTags = persistentListOf(cosy, dark),
                loadingMoodTags = false,
            )

            // ----- Assert -----
            stateFlow.value.moodTiles.map { it.key } shouldBe listOf(1, 2)
            stateFlow.value.moodTiles.map { it.title } shouldBe listOf("Cosy", "Dark")
            stateFlow.value.moodTiles.map { it.ink } shouldBe listOf(MoodTileInk.Cosy, MoodTileInk.Dread)
            job.cancel()
        }

        @Test
        fun `emits four loading stand-in tiles while the first load has no tags`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                moodTags = persistentListOf(),
                loadingMoodTags = true,
            )

            // ----- Act -----
            val job = launch {
                MoodTilesCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.moodTiles.size shouldBe 4
            stateFlow.value.moodTiles.all { it.isLoading } shouldBe true
            job.cancel()
        }

        @Test
        fun `real tags are shown rather than stand-ins even while a refresh is still loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                MoodTilesCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                moodTags = persistentListOf(cosy),
                loadingMoodTags = true,
            )

            // ----- Assert -----
            stateFlow.value.moodTiles.map { it.key } shouldBe listOf(1)
            job.cancel()
        }

        @Test
        fun `shows no tiles when loading finished with no tags`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                MoodTilesCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                moodTags = persistentListOf(),
                loadingMoodTags = false,
            )

            // ----- Assert -----
            stateFlow.value.moodTiles shouldBe persistentListOf()
            job.cancel()
        }

        @Test
        fun `unrelated state change does not recompute the tiles`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                moodTags = persistentListOf(cosy),
                loadingMoodTags = false,
            )
            val job = launch {
                MoodTilesCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            val tilesAfterFirstEmit = stateFlow.value.moodTiles

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(searchText = "fantasy")

            // ----- Assert -----
            (stateFlow.value.moodTiles === tilesAfterFirstEmit) shouldBe true
            stateFlow.value.searchText shouldBe "fantasy"
            job.cancel()
        }
    }
}
