package nl.rhaydus.softcover.feature.settings.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.feature.settings.presentation.event.ComponentGalleryEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.ComponentGalleryDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.ComponentGalleryUiState
import nl.rhaydus.toad.ActionScope

class OnGalleryPaletteSelectedActionTest {
    private lateinit var stateFlow: MutableStateFlow<ComponentGalleryUiState>
    private lateinit var scope: ActionScope<
        ComponentGalleryUiState,
        ComponentGalleryEvent,
        ComponentGalleryLocalVariables,
        >

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ComponentGalleryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ComponentGalleryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: TestScope): ComponentGalleryDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<ComponentGalleryDependencies>(relaxed = true).also { mock ->
            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher

            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `selecting a palette from null sets paletteOverride`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ComponentGalleryUiState(paletteOverride = null)
            val dependencies = stubDependencies(this)
            val action = OnGalleryPaletteSelectedAction(palette = ColorPalette.VELLUM)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.paletteOverride shouldBe ColorPalette.VELLUM
        }

        @Test
        fun `selecting a different palette replaces the current paletteOverride`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ComponentGalleryUiState(paletteOverride = ColorPalette.VELLUM)
            val dependencies = stubDependencies(this)
            val action = OnGalleryPaletteSelectedAction(palette = ColorPalette.INK)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.paletteOverride shouldBe ColorPalette.INK
        }

        @Test
        fun `selecting the already-selected palette clears paletteOverride back to null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ComponentGalleryUiState(paletteOverride = ColorPalette.VELLUM)
            val dependencies = stubDependencies(this)
            val action = OnGalleryPaletteSelectedAction(palette = ColorPalette.VELLUM)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.paletteOverride shouldBe null
        }

        @Test
        fun `does not touch selectedFamily or themeModeOverride`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ComponentGalleryUiState(
                selectedFamily = GalleryFamily.CHIP,
                themeModeOverride = ThemeMode.DARK,
                paletteOverride = null,
            )
            val dependencies = stubDependencies(this)
            val action = OnGalleryPaletteSelectedAction(palette = ColorPalette.VELLUM)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedFamily shouldBe GalleryFamily.CHIP
            stateFlow.value.themeModeOverride shouldBe ThemeMode.DARK
        }
    }
}
