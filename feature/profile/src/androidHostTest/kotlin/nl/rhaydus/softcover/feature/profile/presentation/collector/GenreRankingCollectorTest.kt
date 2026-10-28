package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GenreRankingCollectorTest {
    private lateinit var dependencies: StatsAtlasDependencies
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(StatsAtlasUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(StatsAtlasLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun statsWithGenres(count: Int): ReadingStats = ReadingStats(
        booksRead = 10,
        genres = GenreBreakdown(
            slices = List(count) { index ->
                GenreSlice(
                    name = "Genre $index",
                    count = 10 - index,
                    fraction = 0.9 - index * 0.1,
                )
            },
        ),
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `the default count maps every row, five visible, and a show-more button only`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = StatsAtlasUiState(readingStats = statsWithGenres(7))
                val collector = GenreRankingCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.genreRanking.rows.size shouldBe 7
                stateFlow.value.genreRanking.visibleCount shouldBe 5
                stateFlow.value.genreRankingShowMore.shouldNotBeNull()
                stateFlow.value.genreRankingShowLess.shouldBeNull()
                job.cancel()
            }

        @Test
        fun `a changed shown count rewrites the visible rows and both buttons`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = StatsAtlasUiState(readingStats = statsWithGenres(7))
                val collector = GenreRankingCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(genreRankingShownCount = 15)

                // ----- Assert -----
                stateFlow.value.genreRanking.visibleCount shouldBe 7
                stateFlow.value.genreRankingShowMore.shouldBeNull()
                stateFlow.value.genreRankingShowLess.shouldNotBeNull()
                job.cancel()
            }

        @Test
        fun `new stats re-map the header, rows and buttons`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(readingStats = statsWithGenres(7))
            val collector = GenreRankingCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(readingStats = statsWithGenres(2))

            // ----- Assert -----
            stateFlow.value.genreRanking.rows.size shouldBe 2
            stateFlow.value.genreRankingShowMore.shouldBeNull()
            stateFlow.value.genreRankingShowLess.shouldBeNull()
            stateFlow.value.genreHeader.headlineText.shouldNotBeNull() shouldBe "Mostly genre 0"
            job.cancel()
        }

        @Test
        fun `stats without genres map the empty header with the empty caption`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(
                readingStats = ReadingStats(),
                genreEmptyCaption = "No finished books in 2025.",
            )
            val collector = GenreRankingCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.genreHeader.headlineText shouldBe "The genres you read most"
            stateFlow.value.genreHeader.description.shouldNotBeNull().single().text shouldBe "No finished books in 2025."
            stateFlow.value.genreRanking.rows.size shouldBe 0
            job.cancel()
        }
    }
}
