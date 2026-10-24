package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.YearMetric
import nl.rhaydus.softcover.feature.profile.presentation.state.yearMetricToggleFor
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YearMetricToggleCollectorTest {
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

    @Nested
    inner class OnLaunch {
        @Test
        fun `disables the toggle while the reading life is loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(
                isLoading = true,
                readingLife = mockk<ReadingLife>(),
            )
            val collector = YearMetricToggleCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.yearMetricToggle shouldBe yearMetricToggleFor(
                selected = YearMetric.BOOKS,
                enabled = false,
            )
            job.cancel()
        }

        @Test
        fun `disables the toggle when loading finished without a reading life`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(
                isLoading = false,
                readingLife = null,
            )
            val collector = YearMetricToggleCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.yearMetricToggle shouldBe yearMetricToggleFor(
                selected = YearMetric.BOOKS,
                enabled = false,
            )
            job.cancel()
        }

        @Test
        fun `enables the toggle once the reading life has loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(
                isLoading = false,
                readingLife = mockk<ReadingLife>(),
            )
            val collector = YearMetricToggleCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.yearMetricToggle shouldBe yearMetricToggleFor(
                selected = YearMetric.BOOKS,
                enabled = true,
            )
            job.cancel()
        }

        @Test
        fun `follows the selected metric when it changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(
                isLoading = false,
                readingLife = mockk<ReadingLife>(),
            )
            val collector = YearMetricToggleCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectedYearMetric = YearMetric.PAGES)

            // ----- Assert -----
            stateFlow.value.yearMetricToggle shouldBe yearMetricToggleFor(
                selected = YearMetric.PAGES,
                enabled = true,
            )
            job.cancel()
        }
    }
}
