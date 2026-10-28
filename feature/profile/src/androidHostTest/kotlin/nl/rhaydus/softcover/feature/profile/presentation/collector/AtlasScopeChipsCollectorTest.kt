package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.StatsWindow
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveReadingStatsBackfilledUseCase
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingHistoryUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.statsScopeKey
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AtlasScopeChipsCollectorTest {
    private lateinit var observeReadingHistoryUseCase: ObserveReadingHistoryUseCase
    private lateinit var observeReadingStatsBackfilledUseCase: ObserveReadingStatsBackfilledUseCase
    private lateinit var historyFlow: MutableStateFlow<ReadingHistory>
    private lateinit var backfilledFlow: MutableStateFlow<Boolean>
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>

    private val thisYear = 2026

    @BeforeEach
    fun setUp() {
        historyFlow = MutableStateFlow(ReadingHistory())
        backfilledFlow = MutableStateFlow(true)
        observeReadingHistoryUseCase = mockk()
        observeReadingStatsBackfilledUseCase = mockk()
        stateFlow = MutableStateFlow(StatsAtlasUiState())
        scope = buildScope(StatsAtlasLocalVariables())

        every {
            observeReadingHistoryUseCase()
        } returns historyFlow

        every {
            observeReadingStatsBackfilledUseCase()
        } returns backfilledFlow
    }

    private fun buildScope(
        localVariables: StatsAtlasLocalVariables,
    ): ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables> = ActionScope(
        stateFlow = stateFlow,
        localVariablesFlow = MutableStateFlow(localVariables),
        eventChannel = Channel(Channel.BUFFERED),
    )

    private fun stubDependencies(): StatsAtlasDependencies {
        val observeCurrentDateUseCase = mockk<ObserveCurrentDateUseCase>()

        every {
            observeCurrentDateUseCase()
        } returns flowOf(
            LocalDate(
                2026,
                10,
                23,
            ),
        )

        return mockk<StatsAtlasDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeReadingHistoryUseCase
            } returns observeReadingHistoryUseCase

            every {
                mock.observeReadingStatsBackfilledUseCase
            } returns observeReadingStatsBackfilledUseCase

            every {
                mock.observeCurrentDateUseCase
            } returns observeCurrentDateUseCase
        }
    }

    private fun historyOf(vararg years: Int): ReadingHistory = ReadingHistory(yearsRead = years.toList())

    private fun TestScope.launchCollector(): Job {
        val dependencies = stubDependencies()

        return launch {
            AtlasScopeChipsCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `defaults to the current year when the reader finished a book in it`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                historyFlow.value = historyOf(
                    thisYear,
                    2025,
                )

                // ----- Act -----
                val job = launchCollector()

                // ----- Assert -----
                stateFlow.value.selectedScope shouldBe StatsScope.CalendarYear(thisYear)
                job.cancel()
            }

        @Test
        fun `defaults to all time when the current year has no finished books`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                historyFlow.value = historyOf(
                    2025,
                    2024,
                )

                // ----- Act -----
                val job = launchCollector()

                // ----- Assert -----
                stateFlow.value.selectedScope shouldBe StatsScope.AllTime
                job.cancel()
            }

        @Test
        fun `defaults to all time for a history with no years`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            historyFlow.value = ReadingHistory()

            // ----- Act -----
            val job = launchCollector()

            // ----- Assert -----
            stateFlow.value.selectedScope shouldBe StatsScope.AllTime
            job.cancel()
        }

        @Test
        fun `publishes the history with one chip per year and exactly one selected`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val history = historyOf(
                    thisYear,
                    2025,
                )
                historyFlow.value = history

                // ----- Act -----
                val job = launchCollector()

                // ----- Assert -----
                stateFlow.value.readingHistory shouldBe history
                stateFlow.value.scopeChips.chips.map { it.label }.shouldContainExactly(
                    "Last 30 days",
                    "Last 6 months",
                    "Last 12 months",
                    "2026",
                    "2025",
                    "All time",
                )
                stateFlow.value.scopeChips.chips.filter { it.selected }.map { it.label }.shouldContainExactly("2026")
                job.cancel()
            }

        @Test
        fun `publishes no history and keeps all time while the library is not backfilled`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                backfilledFlow.value = false
                historyFlow.value = historyOf(thisYear)

                // ----- Act -----
                val job = launchCollector()

                // ----- Assert -----
                stateFlow.value.readingHistory.shouldBeNull()
                stateFlow.value.selectedScope shouldBe StatsScope.AllTime
                job.cancel()
            }

        @Test
        fun `applies the default once the backfill flag turns true`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            backfilledFlow.value = false
            historyFlow.value = historyOf(thisYear)
            val job = launchCollector()

            // ----- Act -----
            backfilledFlow.value = true

            // ----- Assert -----
            stateFlow.value.selectedScope shouldBe StatsScope.CalendarYear(thisYear)
            job.cancel()
        }

        @Test
        fun `a scope the reader picked is kept when the first history arrives`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                scope = buildScope(StatsAtlasLocalVariables(hasUserPickedScope = true))
                stateFlow.value = stateFlow.value.copy(selectedScopeKey = statsScopeKey(StatsScope.CalendarYear(2025)))
                historyFlow.value = historyOf(
                    thisYear,
                    2025,
                )

                // ----- Act -----
                val job = launchCollector()

                // ----- Assert -----
                stateFlow.value.selectedScope shouldBe StatsScope.CalendarYear(2025)
                job.cancel()
            }

        @Test
        fun `a later history never overwrites the default`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            historyFlow.value = historyOf(2025)
            val job = launchCollector()

            // ----- Act -----
            historyFlow.value = historyOf(
                thisYear,
                2025,
            )

            // ----- Assert -----
            stateFlow.value.selectedScope shouldBe StatsScope.AllTime
            job.cancel()
        }

        @Test
        fun `falls back to all time when the selected year leaves the history`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                historyFlow.value = historyOf(
                    thisYear,
                    2025,
                )
                val job = launchCollector()

                // ----- Act -----
                historyFlow.value = historyOf(2025)

                // ----- Assert -----
                stateFlow.value.selectedScope shouldBe StatsScope.AllTime
                stateFlow.value.scopeChips.chips.filter { it.selected }.map { it.label }.shouldContainExactly("All time")
                job.cancel()
            }

        @Test
        fun `keeps a selected rolling window when the history changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            historyFlow.value = historyOf(2025)
            val job = launchCollector()
            stateFlow.value = stateFlow.value.copy(selectedScopeKey = statsScopeKey(StatsScope.Rolling(StatsWindow.LAST_6_MONTHS)))

            // ----- Act -----
            historyFlow.value = historyOf(
                2025,
                2024,
            )

            // ----- Assert -----
            stateFlow.value.selectedScope shouldBe
                StatsScope.Rolling(StatsWindow.LAST_6_MONTHS)
            job.cancel()
        }
    }
}
