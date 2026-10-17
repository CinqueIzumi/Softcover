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
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.domain.model.ThemeMode
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
        fun `carries the emitted configuration into the theme state`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ThemeConfigurationCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            themeConfigFlow.emit(
                ThemeConfiguration(
                    themeMode = ThemeMode.DARK,
                    colorPalette = ColorPalette.SEA,
                    useDynamicColor = true,
                ),
            )

            // ----- Assert -----
            stateFlow.value.themeMode shouldBe ThemeMode.DARK
            stateFlow.value.colorPalette shouldBe ColorPalette.SEA
            job.cancel()
        }

        @Test
        fun `steps the spine section back when Dim arrives with dynamic colour still on`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val collector = ThemeConfigurationCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                themeConfigFlow.emit(
                    ThemeConfiguration(
                        themeMode = ThemeMode.DIM,
                        colorPalette = ColorPalette.SEA,
                        useDynamicColor = true,
                    ),
                )

                // ----- Assert -----
                stateFlow.value.spineColourSection.interactive shouldBe false
                stateFlow.value.paletteChoices.none { it.enabled } shouldBe true
                job.cancel()
            }
    }
}
