package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.component.topbar.SearchTopBarUiModel
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

class SearchTopBarCollectorTest {
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

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

    private val moodTag = MoodTag(
        id = 1,
        label = "Cozy",
        slug = "cozy",
        bookCount = 10,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `searchTopBar mirrors searchText, searchFocused and isLoading from state`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = SearchTopBarCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                searchText = "fantasy",
                searchFocused = true,
                isLoading = true,
            )

            // ----- Assert -----
            stateFlow.value.searchTopBar shouldBe SearchTopBarUiModel(
                query = "fantasy",
                active = true,
                focused = true,
                isLoading = true,
            )
            job.cancel()
        }

        @Test
        fun `active is false at the FEED phase`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = SearchTopBarCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(searchText = "")

            // ----- Assert -----
            stateFlow.value.searchTopBar.active shouldBe false
            job.cancel()
        }

        @Test
        fun `active is true at the FOCUS phase`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = SearchTopBarCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(searchFocused = true)

            // ----- Assert -----
            stateFlow.value.searchTopBar.active shouldBe true
            job.cancel()
        }

        @Test
        fun `active is true at the LOADING phase`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = SearchTopBarCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(isLoading = true)

            // ----- Assert -----
            stateFlow.value.searchTopBar.active shouldBe true
            job.cancel()
        }

        @Test
        fun `active is true at the RESULTS phase driven by search text`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = SearchTopBarCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(searchText = "fantasy")

            // ----- Assert -----
            stateFlow.value.searchTopBar.active shouldBe true
            job.cancel()
        }

        @Test
        fun `active is true at the RESULTS phase driven by an active mood filter`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = SearchTopBarCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(activeMoodFilter = moodTag)

            // ----- Assert -----
            stateFlow.value.searchTopBar.active shouldBe true
            job.cancel()
        }

        @Test
        fun `an unrelated state change leaving the four inputs equal does not emit a new model`() =
            runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = SearchTopBarCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(searchText = "fantasy")
            val modelAfterFirstEmission = stateFlow.value.searchTopBar

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(sortMode = ExploreSortMode.POPULARITY)

            // ----- Assert -----
            stateFlow.value.searchTopBar shouldBeSameInstanceAs modelAfterFirstEmission
            job.cancel()
        }
    }
}
