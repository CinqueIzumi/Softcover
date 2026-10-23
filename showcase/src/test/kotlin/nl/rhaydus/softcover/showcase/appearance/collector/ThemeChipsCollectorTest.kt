package nl.rhaydus.softcover.showcase.appearance.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.showcase.appearance.mapper.brightnessChips
import nl.rhaydus.softcover.showcase.appearance.mapper.paletteChips
import nl.rhaydus.softcover.showcase.home.event.HomeEvent
import nl.rhaydus.softcover.showcase.home.screenmodel.HomeDependencies
import nl.rhaydus.softcover.showcase.home.state.HomeLocalVariables
import nl.rhaydus.softcover.showcase.home.state.HomeUiState
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ThemeChipsCollectorTest {
    private lateinit var store: ShowcaseThemeStore
    private lateinit var dependencies: HomeDependencies
    private lateinit var stateFlow: MutableStateFlow<HomeUiState>
    private lateinit var scope: ActionScope<HomeUiState, HomeEvent, HomeLocalVariables>

    @BeforeEach
    fun setUp() {
        store = ShowcaseThemeStore()
        stateFlow = MutableStateFlow(HomeUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(HomeLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<HomeDependencies>(relaxed = true).also { mock ->
            every {
                mock.themeStore
            } returns store
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `marks the palette and brightness the store already holds`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            store.selectPalette(SpinePalette.FOXED)
            store.selectBrightness(SchemeBrightness.DARK)
            val collector = ThemeChipsCollector<HomeUiState, HomeEvent, HomeDependencies, HomeLocalVariables>(themeStore = store)

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.appearance.paletteChips shouldBe paletteChips(selected = SpinePalette.FOXED)
            stateFlow.value.appearance.brightnessChips shouldBe brightnessChips(selected = SchemeBrightness.DARK)
            job.cancel()
        }

        @Test
        fun `moves the marked chips when the store changes later`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ThemeChipsCollector<HomeUiState, HomeEvent, HomeDependencies, HomeLocalVariables>(themeStore = store)
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            store.selectPalette(SpinePalette.SEA)
            store.selectBrightness(SchemeBrightness.DARK)

            // ----- Assert -----
            stateFlow.value.appearance.paletteChips shouldBe paletteChips(selected = SpinePalette.SEA)
            stateFlow.value.appearance.brightnessChips shouldBe brightnessChips(selected = SchemeBrightness.DARK)
            job.cancel()
        }
    }
}
