package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetRowUiModel
import nl.rhaydus.softcover.core.domain.model.Book
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
            // ----- Arrange -----
            val genre: String? = null
            val options: List<String> = listOf("Romance", "Horror")

            // ----- Act -----
            val rows = becauseYouReadGenreRowsFor(
                genre = genre,
                options = options,
            )

            // ----- Assert -----
            rows.map { it.selected } shouldBe listOf(true, false, false)
        }

        @Test
        fun `selects only Auto when the genre matches no option`() {
            // ----- Arrange -----
            val genre: String? = "Fantasy"
            val options: List<String> = listOf("Romance", "Horror")

            // ----- Act -----
            val rows = becauseYouReadGenreRowsFor(
                genre = genre,
                options = options,
            )

            // ----- Assert -----
            rows.map { it.selected } shouldBe listOf(true, false, false)
        }

        @Test
        fun `selects only the matching option when the genre matches`() {
            // ----- Arrange -----
            val genre: String? = "Horror"
            val options: List<String> = listOf("Romance", "Horror")

            // ----- Act -----
            val rows = becauseYouReadGenreRowsFor(
                genre = genre,
                options = options,
            )

            // ----- Assert -----
            rows.map { it.selected } shouldBe listOf(false, false, true)
        }

        @Test
        fun `puts the null-keyed Auto row first followed by the options in order`() {
            // ----- Arrange -----
            val genre: String? = null
            val options: List<String> = listOf("Romance", "Horror")

            // ----- Act -----
            val rows = becauseYouReadGenreRowsFor(
                genre = genre,
                options = options,
            )

            // ----- Assert -----
            rows.map { it.key } shouldBe listOf(null, "Romance", "Horror")
            rows.map { it.label } shouldBe listOf("Auto (most-read)", "Romance", "Horror")
        }

        @Test
        fun `returns only the Auto row, selected, when there are no options`() {
            // ----- Arrange -----
            val genre: String? = "Romance"
            val options: List<String> = emptyList()

            // ----- Act -----
            val rows = becauseYouReadGenreRowsFor(
                genre = genre,
                options = options,
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
            stateFlow.value.becauseYouReadGenreControl.genreGroup.rows.map { it.selected } shouldBe listOf(false, true, false)
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
            stateFlow.value.becauseYouReadGenreControl.genreGroup.rows.map { it.selected } shouldBe listOf(false, false, true)
            job.cancel()
        }

        @Test
        fun `labels the control from the genre and shows it`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                becauseYouReadGenre = "Horror",
                becauseYouReadGenreOptions = listOf("Horror"),
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
            val control = stateFlow.value.becauseYouReadGenreControl
            control.label shouldBe "GENRE · HORROR"
            control.visible shouldBe true
            control.hasOptions shouldBe true
            job.cancel()
        }

        @Test
        fun `uses the Fantasy label and hides the control when the genre is null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(becauseYouReadGenre = null)
            val collector = BecauseYouReadGenreSheetCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            val control = stateFlow.value.becauseYouReadGenreControl
            control.label shouldBe "GENRE · FANTASY"
            control.visible shouldBe false
            control.hasOptions shouldBe false
            job.cancel()
        }

        @Test
        fun `is loading while books load and none exist yet`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                loadingBecauseYouReadBooks = true,
                becauseYouReadBooks = emptyList(),
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
            stateFlow.value.becauseYouReadGenreControl.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `stops loading once books exist even while loading continues`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                loadingBecauseYouReadBooks = true,
                becauseYouReadBooks = emptyList(),
            )
            val collector = BecauseYouReadGenreSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(becauseYouReadBooks = listOf(mockk<Book>()))

            // ----- Assert -----
            stateFlow.value.becauseYouReadGenreControl.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `stops loading when loading ends with no books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                loadingBecauseYouReadBooks = true,
                becauseYouReadBooks = emptyList(),
            )
            val collector = BecauseYouReadGenreSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(loadingBecauseYouReadBooks = false)

            // ----- Assert -----
            stateFlow.value.becauseYouReadGenreControl.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `preserves sheetVisible across rebuilds`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(becauseYouReadGenre = "Romance")
            val collector = BecauseYouReadGenreSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = stateFlow.value.copy(
                becauseYouReadGenreControl = stateFlow.value.becauseYouReadGenreControl.copy(sheetVisible = true),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(becauseYouReadGenre = "Horror")

            // ----- Assert -----
            val control = stateFlow.value.becauseYouReadGenreControl
            control.label shouldBe "GENRE · HORROR"
            control.sheetVisible shouldBe true
            job.cancel()
        }
    }
}
