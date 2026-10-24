package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearHistoryCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearHistoryChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.YearMetric
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YearHistoryCollectorTest {
    private lateinit var dependencies: StatsAtlasDependencies
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>

    private val books = listOf(
        YearCount(
            year = 2023,
            count = 4,
        ),
        YearCount(
            year = 2024,
            count = 9,
        ),
    )
    private val pages = listOf(
        YearCount(
            year = 2023,
            count = 1_200,
        ),
        YearCount(
            year = 2024,
            count = 800,
        ),
    )

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

    private fun buildReadingLife(): ReadingLife = ReadingLife(
        booksByYear = books,
        pagesByYear = pages,
        pagesByMonth = emptyList(),
        genres = GenreBreakdown(),
        ratings = RatingsDistribution(),
        recentlyLoved = emptyList(),
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `writes the loading chart while the reading life is loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(
                isLoading = true,
                readingLife = buildReadingLife(),
            )

            // ----- Act -----
            val job = launch {
                YearHistoryCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.yearHistoryChart shouldBe yearHistoryChartUiModel(
                series = null,
                metric = YearMetric.BOOKS,
            )
            stateFlow.value.yearHistoryChart.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `writes the loading chart when there is no reading life`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(
                isLoading = false,
                readingLife = null,
            )

            // ----- Act -----
            val job = launch {
                YearHistoryCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.yearHistoryChart shouldBe yearHistoryChartUiModel(
                series = null,
                metric = YearMetric.BOOKS,
            )
            job.cancel()
        }

        @Test
        fun `uses the books series by default`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(
                isLoading = false,
                readingLife = buildReadingLife(),
            )

            // ----- Act -----
            val job = launch {
                YearHistoryCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.yearHistoryChart shouldBe yearHistoryChartUiModel(
                series = books,
                metric = YearMetric.BOOKS,
            )
            stateFlow.value.yearHistoryCaption shouldBe yearHistoryCaption(
                series = books,
                metric = YearMetric.BOOKS,
            )
            job.cancel()
        }

        @Test
        fun `switches to the pages series when the selected metric changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(
                isLoading = false,
                readingLife = buildReadingLife(),
            )
            val job = launch {
                YearHistoryCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectedYearMetric = YearMetric.PAGES)

            // ----- Assert -----
            stateFlow.value.yearHistoryChart shouldBe yearHistoryChartUiModel(
                series = pages,
                metric = YearMetric.PAGES,
            )
            stateFlow.value.yearHistoryCaption shouldBe yearHistoryCaption(
                series = pages,
                metric = YearMetric.PAGES,
            )
            job.cancel()
        }
    }
}
