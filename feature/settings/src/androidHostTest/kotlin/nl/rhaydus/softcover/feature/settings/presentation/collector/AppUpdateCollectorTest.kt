package nl.rhaydus.softcover.feature.settings.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.appupdate.ObserveAppUpdateStateUseCase
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.mapper.appUpdateCalloutFor
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AppUpdateCollectorTest {
    private lateinit var observeAppUpdateStateUseCase: ObserveAppUpdateStateUseCase
    private lateinit var dependencies: SettingsScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<SettingsScreenUiState>
    private lateinit var scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>
    private lateinit var updateStateFlow: MutableSharedFlow<AppUpdateState>

    @BeforeEach
    fun setUp() {
        updateStateFlow = MutableSharedFlow()
        observeAppUpdateStateUseCase = mockk()
        stateFlow = MutableStateFlow(SettingsScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(SettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            observeAppUpdateStateUseCase()
        } returns updateStateFlow

        dependencies = mockk<SettingsScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeAppUpdateStateUseCase
            } returns observeAppUpdateStateUseCase
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets the callout for the emitted state`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = AppUpdateCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            updateStateFlow.emit(AppUpdateState.Available)

            // ----- Assert -----
            stateFlow.value.appUpdateCallout.shouldNotBeNull() shouldBe
                appUpdateCalloutFor(state = AppUpdateState.Available)
            job.cancel()
        }

        @Test
        fun `clears the callout when the state returns to Idle`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = AppUpdateCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            updateStateFlow.emit(AppUpdateState.Downloaded)

            // ----- Act -----
            updateStateFlow.emit(AppUpdateState.Idle)

            // ----- Assert -----
            stateFlow.value.appUpdateCallout.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `follows the latest state across a download lifecycle`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = AppUpdateCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            updateStateFlow.emit(AppUpdateState.Available)
            updateStateFlow.emit(AppUpdateState.Downloading)
            updateStateFlow.emit(AppUpdateState.Failed)

            // ----- Assert -----
            stateFlow.value.appUpdateCallout shouldBe appUpdateCalloutFor(state = AppUpdateState.Failed)
            job.cancel()
        }

        @Test
        fun `leaves the callout null before the flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = AppUpdateCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act & Assert -----
            stateFlow.value.appUpdateCallout.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `preserves other state fields when setting the callout`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = SettingsScreenUiState(dropDownExpanded = true)
            val collector = AppUpdateCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            updateStateFlow.emit(AppUpdateState.Downloading)

            // ----- Assert -----
            stateFlow.value.dropDownExpanded shouldBe true
            job.cancel()
        }
    }
}
