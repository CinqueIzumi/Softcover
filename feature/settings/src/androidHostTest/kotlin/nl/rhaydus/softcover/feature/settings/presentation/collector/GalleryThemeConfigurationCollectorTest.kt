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
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.core.uibinding.theme.toSpinePalette
import nl.rhaydus.softcover.feature.settings.presentation.event.ComponentGalleryEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.ComponentGalleryDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GalleryThemeConfigurationCollectorTest {
    private lateinit var dependencies: ComponentGalleryDependencies
    private lateinit var stateFlow: MutableStateFlow<ComponentGalleryUiState>
    private lateinit var scope: ActionScope<
        ComponentGalleryUiState,
        ComponentGalleryEvent,
        ComponentGalleryLocalVariables,
        >
    private lateinit var themeConfigFlow: MutableSharedFlow<ThemeConfiguration>

    @BeforeEach
    fun setUp() {
        themeConfigFlow = MutableSharedFlow()
        stateFlow = MutableStateFlow(ComponentGalleryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ComponentGalleryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        val useCase = mockk<GetThemeConfigurationUseCase>()
        every {
            useCase()
        } returns themeConfigFlow

        dependencies = mockk<ComponentGalleryDependencies>(relaxed = true).also { mock ->
            every {
                mock.getThemeConfigurationUseCase
            } returns useCase
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `follows the app palette when there is no override`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = GalleryThemeConfigurationCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            themeConfigFlow.emit(ThemeConfiguration(colorPalette = ColorPalette.SEA))

            // ----- Assert -----
            stateFlow.value.currentPalette shouldBe ColorPalette.SEA.toSpinePalette()
            stateFlow.value.resolvedPalette shouldBe ColorPalette.SEA.toSpinePalette()
            job.cancel()
        }

        @Test
        fun `keeps the override as the resolved palette while tracking the app palette`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = ComponentGalleryUiState(paletteOverride = ColorPalette.INK)
                val collector = GalleryThemeConfigurationCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                themeConfigFlow.emit(ThemeConfiguration(colorPalette = ColorPalette.SEA))

                // ----- Assert -----
                stateFlow.value.currentPalette shouldBe ColorPalette.SEA.toSpinePalette()
                stateFlow.value.resolvedPalette shouldBe ColorPalette.INK.toSpinePalette()
                stateFlow.value.paletteOverride shouldBe ColorPalette.INK
                job.cancel()
            }
    }
}
