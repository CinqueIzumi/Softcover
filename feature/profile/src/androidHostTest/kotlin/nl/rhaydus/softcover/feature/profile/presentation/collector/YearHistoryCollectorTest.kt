package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.StatsWindow
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasScopeChips
import nl.rhaydus.softcover.feature.profile.presentation.mapper.statsScopeKey
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

    private fun buildReadingHistory(): ReadingHistory = ReadingHistory(
        booksByYear = books,
        pagesByYear = pages,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `writes the loading chart while the reading history is loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(
                isLoading = true,
                readingHistory = buildReadingHistory(),
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
        fun `writes the loading chart when there is no reading history`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(
                isLoading = false,
                readingHistory = null,
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
                readingHistory = buildReadingHistory(),
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
                readingHistory = buildReadingHistory(),
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

    @Nested
    inner class OnLaunchWithScope {
        private fun scopedState(scope: StatsScope): StatsAtlasUiState {
            val key = statsScopeKey(scope)

            return StatsAtlasUiState(
                isLoading = false,
                readingHistory = buildReadingHistory(),
                selectedScopeKey = key,
                scopeChips = atlasScopeChips(
                    yearsRead = listOf(2024, 2023),
                    selectedKey = key,
                ),
            )
        }

        @Test
        fun `lights the selected calendar year and names it with its count`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = scopedState(StatsScope.CalendarYear(2023))

            // ----- Act -----
            val job = launch {
                YearHistoryCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.yearHistoryChart.columns.map { it.tone } shouldBe listOf(ChartTone.Lead, ChartTone.Second)
            stateFlow.value.yearHistoryCaption shouldBe "In 2023 you finished 4 books."
            job.cancel()
        }

        @Test
        fun `a rolling scope keeps the busiest year as Lead`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = scopedState(StatsScope.Rolling(StatsWindow.LAST_12_MONTHS))

            // ----- Act -----
            val job = launch {
                YearHistoryCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.yearHistoryChart.columns.map { it.tone } shouldBe listOf(ChartTone.Second, ChartTone.Lead)
            stateFlow.value.yearHistoryCaption shouldBe "2024 was your busiest year yet: 9 books."
            job.cancel()
        }

        @Test
        fun `returns to the busiest year when the scope changes back to all time`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = scopedState(StatsScope.CalendarYear(2023))
            val job = launch {
                YearHistoryCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = scopedState(StatsScope.AllTime)

            // ----- Assert -----
            stateFlow.value.yearHistoryChart.columns.map { it.tone } shouldBe listOf(ChartTone.Second, ChartTone.Lead)
            stateFlow.value.yearHistoryCaption shouldBe "2024 was your busiest year yet: 9 books."
            job.cancel()
        }

        @Test
        fun `a selected year switches the pages caption to pages`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = scopedState(StatsScope.CalendarYear(2024)).copy(selectedYearMetric = YearMetric.PAGES)

            // ----- Act -----
            val job = launch {
                YearHistoryCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.yearHistoryCaption shouldBe "In 2024 you read 800 pages."
            job.cancel()
        }
    }
}
