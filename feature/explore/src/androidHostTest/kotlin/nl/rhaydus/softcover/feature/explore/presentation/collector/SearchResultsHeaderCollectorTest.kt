package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SearchResultsHeaderCollectorTest {
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
        dependencies = mockk(relaxed = true)
    }

    private val moodTag = MoodTag(
        id = 1,
        label = "Cozy",
        slug = "cozy",
        bookCount = 10,
    )

    private fun books(count: Int): List<Book> = List(count) { mockk() }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets searchResultsHeader with the result count and no description when there is no search text or mood filter`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = ExploreScreenUiState(
                    queriedBooks = books(3),
                    searchText = "",
                    activeMoodFilter = null,
                )
                val collector = SearchResultsHeaderCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.searchResultsHeader shouldBe SectionHeaderUiModel.Section(
                    eyebrow = "Showing 3 results",
                    description = null,
                )
                job.cancel()
            }

        @Test
        fun `sets searchResultsHeader description to the search text when a text search is active`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = ExploreScreenUiState(
                    queriedBooks = books(5),
                    searchText = "dune",
                    activeMoodFilter = null,
                )
                val collector = SearchResultsHeaderCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.searchResultsHeader shouldBe SectionHeaderUiModel.Section(
                    eyebrow = "Showing 5 results",
                    description = "for \"dune\"",
                )
                job.cancel()
            }

        @Test
        fun `sets searchResultsHeader description to the mood filter label when a mood filter is active`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = ExploreScreenUiState(
                    queriedBooks = books(0),
                    searchText = "",
                    activeMoodFilter = moodTag,
                )
                val collector = SearchResultsHeaderCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.searchResultsHeader shouldBe SectionHeaderUiModel.Section(
                    eyebrow = "Showing 0 results",
                    description = "for \"Cozy\"",
                )
                job.cancel()
            }

        @Test
        fun `prefers the search text description when both search text and a mood filter are set`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = ExploreScreenUiState(
                    queriedBooks = books(1),
                    searchText = "dune",
                    activeMoodFilter = moodTag,
                )
                val collector = SearchResultsHeaderCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.searchResultsHeader shouldBe SectionHeaderUiModel.Section(
                    eyebrow = "Showing 1 results",
                    description = "for \"dune\"",
                )
                job.cancel()
            }

        @Test
        fun `re-derives searchResultsHeader when queriedBooks changes size`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                queriedBooks = books(1),
                searchText = "",
                activeMoodFilter = null,
            )
            val collector = SearchResultsHeaderCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(queriedBooks = books(4))

            // ----- Assert -----
            stateFlow.value.searchResultsHeader shouldBe SectionHeaderUiModel.Section(
                eyebrow = "Showing 4 results",
                description = null,
            )
            job.cancel()
        }
    }
}
