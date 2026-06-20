package nl.rhaydus.softcover.feature.settings.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetReadingStreakEnabledAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingStreakCollectorTest {
    private lateinit var getReadingStreakEnabledAsFlowUseCase: GetReadingStreakEnabledAsFlowUseCase
    private lateinit var dependencies: SettingsScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<SettingsScreenUiState>
    private lateinit var scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>
    private lateinit var readingStreakEnabledFlow: MutableSharedFlow<Boolean>

    @BeforeEach
    fun setUp() {
        readingStreakEnabledFlow = MutableSharedFlow()
        getReadingStreakEnabledAsFlowUseCase = mockk()
        stateFlow = MutableStateFlow(SettingsScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(SettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getReadingStreakEnabledAsFlowUseCase()
        } returns readingStreakEnabledFlow

        dependencies = mockk<SettingsScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.getReadingStreakEnabledAsFlowUseCase
            } returns getReadingStreakEnabledAsFlowUseCase
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets readingStreakEnabledChecked to true when flow emits true`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ReadingStreakCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            readingStreakEnabledFlow.emit(true)

            // ----- Assert -----
            stateFlow.value.readingStreakEnabledChecked shouldBe true
            job.cancel()
        }

        @Test
        fun `sets readingStreakEnabledChecked to false when flow emits false`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ReadingStreakCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            readingStreakEnabledFlow.emit(false)

            // ----- Assert -----
            stateFlow.value.readingStreakEnabledChecked shouldBe false
            job.cancel()
        }

        @Test
        fun `updates readingStreakEnabledChecked to the latest value when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ReadingStreakCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            readingStreakEnabledFlow.emit(false)
            readingStreakEnabledFlow.emit(true)

            // ----- Assert -----
            stateFlow.value.readingStreakEnabledChecked shouldBe true
            job.cancel()
        }

        @Test
        fun `does not change readingStreakEnabledChecked before the flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ReadingStreakCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.readingStreakEnabledChecked shouldBe true
            job.cancel()
        }

        @Test
        fun `preserves other state fields when updating readingStreakEnabledChecked`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = SettingsScreenUiState(
                readingStreakEnabledChecked = true,
                dropDownExpanded = true,
            )
            val collector = ReadingStreakCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            readingStreakEnabledFlow.emit(false)

            // ----- Assert -----
            stateFlow.value.dropDownExpanded shouldBe true
            stateFlow.value.readingStreakEnabledChecked shouldBe false
            job.cancel()
        }

        @Test
        fun `retains last emitted readingStreakEnabledChecked after the collector job is cancelled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ReadingStreakCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }
            readingStreakEnabledFlow.emit(false)
            job.cancel()

            // ----- Act & Assert -----
            stateFlow.value.readingStreakEnabledChecked shouldBe false
        }
    }
}
