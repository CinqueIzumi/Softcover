package nl.rhaydus.softcover.feature.settings.presentation.flows

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ThemeConfigurationCollectorTest {
    private lateinit var getThemeConfigurationUseCase: GetThemeConfigurationUseCase
    private lateinit var dependencies: SettingsScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<SettingsScreenUiState>
    private lateinit var scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>
    private lateinit var themeConfigFlow: MutableSharedFlow<ThemeConfiguration>

    @BeforeEach
    fun setUp() {
        themeConfigFlow = MutableSharedFlow()
        getThemeConfigurationUseCase = mockk()
        stateFlow = MutableStateFlow(SettingsScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(SettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getThemeConfigurationUseCase()
        } returns themeConfigFlow

        dependencies = mockk<SettingsScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.getThemeConfigurationUseCase
            } returns getThemeConfigurationUseCase
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets useFloatingBarChecked to true when FLOATING configuration emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ThemeConfigurationCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            themeConfigFlow.emit(ThemeConfiguration(bottomBarStyle = BottomBarStyle.FLOATING))

            // ----- Assert -----
            stateFlow.value.useFloatingBarChecked shouldBe true
            job.cancel()
        }

        @Test
        fun `sets useFloatingBarChecked to false when DOCKED configuration emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ThemeConfigurationCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            themeConfigFlow.emit(ThemeConfiguration(bottomBarStyle = BottomBarStyle.DOCKED))

            // ----- Assert -----
            stateFlow.value.useFloatingBarChecked shouldBe false
            job.cancel()
        }

        @Test
        fun `updates useFloatingBarChecked to the latest value when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ThemeConfigurationCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            themeConfigFlow.emit(ThemeConfiguration(bottomBarStyle = BottomBarStyle.FLOATING))
            themeConfigFlow.emit(ThemeConfiguration(bottomBarStyle = BottomBarStyle.DOCKED))

            // ----- Assert -----
            stateFlow.value.useFloatingBarChecked shouldBe false
            job.cancel()
        }

        @Test
        fun `does not change useFloatingBarChecked before the flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ThemeConfigurationCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.useFloatingBarChecked shouldBe true
            job.cancel()
        }

        @Test
        fun `preserves other state fields when updating useFloatingBarChecked`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = SettingsScreenUiState(
                useFloatingBarChecked = true,
                dropDownExpanded = true,
            )
            val collector = ThemeConfigurationCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            themeConfigFlow.emit(ThemeConfiguration(bottomBarStyle = BottomBarStyle.DOCKED))

            // ----- Assert -----
            stateFlow.value.dropDownExpanded shouldBe true
            stateFlow.value.useFloatingBarChecked shouldBe false
            job.cancel()
        }

        @Test
        fun `retains last emitted useFloatingBarChecked after the collector job is cancelled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ThemeConfigurationCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }
            themeConfigFlow.emit(ThemeConfiguration(bottomBarStyle = BottomBarStyle.DOCKED))
            job.cancel()

            // ----- Act & Assert -----
            stateFlow.value.useFloatingBarChecked shouldBe false
        }
    }
}
