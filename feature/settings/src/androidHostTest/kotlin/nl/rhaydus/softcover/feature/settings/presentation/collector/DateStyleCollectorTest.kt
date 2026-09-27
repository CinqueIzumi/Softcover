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
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DateStyleCollectorTest {
    private lateinit var getDateStyleAsFlowUseCase: GetDateStyleAsFlowUseCase
    private lateinit var dependencies: SettingsScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<SettingsScreenUiState>
    private lateinit var scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>
    private lateinit var dateStyleFlow: MutableSharedFlow<DateStyle>

    @BeforeEach
    fun setUp() {
        dateStyleFlow = MutableSharedFlow()
        getDateStyleAsFlowUseCase = mockk()
        stateFlow = MutableStateFlow(SettingsScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(SettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getDateStyleAsFlowUseCase()
        } returns dateStyleFlow

        dependencies = mockk<SettingsScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.getDateStyleAsFlowUseCase
            } returns getDateStyleAsFlowUseCase
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `updates userDateStyle when flow emits a value`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = DateStyleCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            dateStyleFlow.emit(DateStyle.MONTH_DAY_YEAR)

            // ----- Assert -----
            stateFlow.value.userDateStyle shouldBe DateStyle.MONTH_DAY_YEAR
            job.cancel()
        }

        @Test
        fun `updates userDateStyle to the latest value when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = DateStyleCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            dateStyleFlow.emit(DateStyle.DAY_MONTH_YEAR)
            dateStyleFlow.emit(DateStyle.YEAR_MONTH_DAY)

            // ----- Assert -----
            stateFlow.value.userDateStyle shouldBe DateStyle.YEAR_MONTH_DAY
            job.cancel()
        }

        @Test
        fun `does not change userDateStyle before the flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = DateStyleCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.userDateStyle shouldBe DateStyle.DAY_MONTH_YEAR
            job.cancel()
        }

        @Test
        fun `retains the last emitted DateStyle after the collector job is cancelled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = DateStyleCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }
            dateStyleFlow.emit(DateStyle.MONTH_DAY_YEAR)
            job.cancel()

            // ----- Act & Assert -----
            stateFlow.value.userDateStyle shouldBe DateStyle.MONTH_DAY_YEAR
        }

        @Test
        fun `preserves all other state fields when updating userDateStyle`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = SettingsScreenUiState(
                useFloatingBarChecked = false,
                userDateStyle = DateStyle.DAY_MONTH_YEAR,
                dropDownExpanded = true,
            )
            val collector = DateStyleCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            dateStyleFlow.emit(DateStyle.MONTH_DAY_YEAR)

            // ----- Assert -----
            stateFlow.value.useFloatingBarChecked shouldBe false
            stateFlow.value.dropDownExpanded shouldBe true
            stateFlow.value.userDateStyle shouldBe DateStyle.MONTH_DAY_YEAR
            job.cancel()
        }
    }
}
