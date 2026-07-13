package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.designsystem.presentation.model.ProgressSheetTab
import nl.rhaydus.softcover.core.domain.model.ProgressUnit
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

class LastUsedProgressUnitCollectorTest {
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun buildDependencies(unit: ProgressUnit): ReadingScreenDependencies =
        mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.getLastUsedProgressUnitAsFlowUseCase()
            } returns flowOf(unit)
        }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets progressSheetTab to PERCENTAGE when last used unit is PERCENTAGE`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val dependencies = buildDependencies(ProgressUnit.PERCENTAGE)
            val collector = LastUsedProgressUnitCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Assert -----
            stateFlow.value.progressSheetTab shouldBe ProgressSheetTab.PERCENTAGE
            job.cancel()
        }

        @Test
        fun `sets progressSheetTab to PAGE when last used unit is PAGE`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val dependencies = buildDependencies(ProgressUnit.PAGE)
            val collector = LastUsedProgressUnitCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Assert -----
            stateFlow.value.progressSheetTab shouldBe ProgressSheetTab.PAGE
            job.cancel()
        }

        @Test
        fun `sets progressSheetTab to TIME when last used unit is TIME`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val dependencies = buildDependencies(ProgressUnit.TIME)
            val collector = LastUsedProgressUnitCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Assert -----
            stateFlow.value.progressSheetTab shouldBe ProgressSheetTab.TIME
            job.cancel()
        }
    }
}
