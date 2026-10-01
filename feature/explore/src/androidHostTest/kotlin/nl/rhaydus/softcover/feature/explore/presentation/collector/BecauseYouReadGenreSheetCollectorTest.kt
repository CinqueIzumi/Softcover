package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetRowUiModel
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BecauseYouReadGenreSheetCollectorTest {
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

    @Nested
    inner class BecauseYouReadGenreRowsFor {
        @Test
        fun `selects only Auto when the genre is null`() {
            // ----- Act -----
            val rows = becauseYouReadGenreRowsFor(
                genre = null,
                options = listOf("Romance", "Horror"),
            )

            // ----- Assert -----
            rows.map { it.selected } shouldBe listOf(true, false, false)
        }

        @Test
        fun `selects only Auto when the genre matches no option`() {
            // ----- Act -----
            val rows = becauseYouReadGenreRowsFor(
                genre = "Fantasy",
                options = listOf("Romance", "Horror"),
            )

            // ----- Assert -----
            rows.map { it.selected } shouldBe listOf(true, false, false)
        }

        @Test
        fun `selects only the matching option when the genre matches`() {
            // ----- Act -----
            val rows = becauseYouReadGenreRowsFor(
                genre = "Horror",
                options = listOf("Romance", "Horror"),
            )

            // ----- Assert -----
            rows.map { it.selected } shouldBe listOf(false, false, true)
        }

        @Test
        fun `puts the null-keyed Auto row first followed by the options in order`() {
            // ----- Act -----
            val rows = becauseYouReadGenreRowsFor(
                genre = null,
                options = listOf("Romance", "Horror"),
            )

            // ----- Assert -----
            rows.map { it.key } shouldBe listOf(null, "Romance", "Horror")
            rows.map { it.label } shouldBe listOf("Auto (most-read)", "Romance", "Horror")
        }

        @Test
        fun `returns only the Auto row, selected, when there are no options`() {
            // ----- Act -----
            val rows = becauseYouReadGenreRowsFor(
                genre = "Romance",
                options = emptyList(),
            )

            // ----- Assert -----
            rows shouldBe listOf(
                SheetRowUiModel<String?>(
                    key = null,
                    label = "Auto (most-read)",
                    selected = true,
                ),
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `writes the rows for the initial genre and options`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                becauseYouReadGenre = "Romance",
                becauseYouReadGenreOptions = listOf("Romance", "Horror"),
            )
            val collector = BecauseYouReadGenreSheetCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.becauseYouReadGenreGroup.rows.map { it.selected } shouldBe listOf(false, true, false)
            job.cancel()
        }

        @Test
        fun `the group dismisses the sheet itself on selection, also after a rewrite`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val collector = BecauseYouReadGenreSheetCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(becauseYouReadGenreOptions = listOf("Horror"))

                // ----- Assert -----
                stateFlow.value.becauseYouReadGenreGroup.dismissesSheetOnSelect shouldBe true
                job.cancel()
            }

        @Test
        fun `rewrites the rows when the genre changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                becauseYouReadGenre = "Romance",
                becauseYouReadGenreOptions = listOf("Romance", "Horror"),
            )
            val collector = BecauseYouReadGenreSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(becauseYouReadGenre = "Horror")

            // ----- Assert -----
            stateFlow.value.becauseYouReadGenreGroup.rows.map { it.selected } shouldBe listOf(false, false, true)
            job.cancel()
        }

        @Test
        fun `rewrites the rows when the options change`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = BecauseYouReadGenreSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(becauseYouReadGenreOptions = listOf("Romance", "Horror"))

            // ----- Assert -----
            stateFlow.value.becauseYouReadGenreGroup.rows.map { it.key } shouldBe listOf(null, "Romance", "Horror")
            job.cancel()
        }
    }
}
