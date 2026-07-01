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
import nl.rhaydus.softcover.core.domain.model.UiScale
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetUiScaleAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UiScaleCollectorTest {
    private lateinit var getUiScaleAsFlowUseCase: GetUiScaleAsFlowUseCase
    private lateinit var dependencies: SettingsScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<SettingsScreenUiState>
    private lateinit var scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>
    private lateinit var uiScaleFlow: MutableSharedFlow<UiScale>

    @BeforeEach
    fun setUp() {
        uiScaleFlow = MutableSharedFlow()
        getUiScaleAsFlowUseCase = mockk()
        stateFlow = MutableStateFlow(SettingsScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(SettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getUiScaleAsFlowUseCase()
        } returns uiScaleFlow

        dependencies = mockk<SettingsScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.getUiScaleAsFlowUseCase
            } returns getUiScaleAsFlowUseCase
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets uiScale to the emitted value when flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = UiScaleCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            uiScaleFlow.emit(UiScale.PERCENT_150)

            // ----- Assert -----
            stateFlow.value.uiScale shouldBe UiScale.PERCENT_150
            job.cancel()
        }

        @Test
        fun `updates uiScale to the latest value when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = UiScaleCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            uiScaleFlow.emit(UiScale.PERCENT_125)
            uiScaleFlow.emit(UiScale.PERCENT_200)

            // ----- Assert -----
            stateFlow.value.uiScale shouldBe UiScale.PERCENT_200
            job.cancel()
        }

        @Test
        fun `does not change uiScale before the flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = UiScaleCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.uiScale shouldBe UiScale.DEFAULT
            job.cancel()
        }

        @Test
        fun `preserves other state fields when updating uiScale`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = SettingsScreenUiState(
                uiScale = UiScale.DEFAULT,
                dropDownExpanded = true,
            )
            val collector = UiScaleCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            uiScaleFlow.emit(UiScale.PERCENT_175)

            // ----- Assert -----
            stateFlow.value.dropDownExpanded shouldBe true
            stateFlow.value.uiScale shouldBe UiScale.PERCENT_175
            job.cancel()
        }

        @Test
        fun `retains last emitted uiScale after the collector job is cancelled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = UiScaleCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }
            uiScaleFlow.emit(UiScale.PERCENT_150)
            job.cancel()

            // ----- Act & Assert -----
            stateFlow.value.uiScale shouldBe UiScale.PERCENT_150
        }
    }
}
