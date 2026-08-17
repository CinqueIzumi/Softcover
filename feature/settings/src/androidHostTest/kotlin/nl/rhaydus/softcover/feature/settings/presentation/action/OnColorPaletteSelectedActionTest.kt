package nl.rhaydus.softcover.feature.settings.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
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
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetColorPaletteUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope

class OnColorPaletteSelectedActionTest {
    private lateinit var setColorPaletteUseCase: SetColorPaletteUseCase
    private lateinit var dependencies: SettingsScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<SettingsScreenUiState>
    private lateinit var scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>

    @BeforeEach
    fun setUp() {
        setColorPaletteUseCase = mockk()
        stateFlow = MutableStateFlow(SettingsScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(SettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: TestScope): SettingsScreenDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<SettingsScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.setColorPaletteUseCase
            } returns setColorPaletteUseCase

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
        fun `invokes use case with the selected palette`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setColorPaletteUseCase(palette = ColorPalette.SEA)
            } returns Result.success(Unit)

            val action = OnColorPaletteSelectedAction(palette = ColorPalette.SEA)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                setColorPaletteUseCase(palette = ColorPalette.SEA)
            }
        }

        @Test
        fun `invokes use case with a different selected palette`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setColorPaletteUseCase(palette = ColorPalette.INK)
            } returns Result.success(Unit)

            val action = OnColorPaletteSelectedAction(palette = ColorPalette.INK)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                setColorPaletteUseCase(palette = ColorPalette.INK)
            }
        }

        @Test
        fun `does not throw when the use case returns a failure`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setColorPaletteUseCase(palette = ColorPalette.FOXED)
            } returns Result.failure(RuntimeException("storage error"))

            val action = OnColorPaletteSelectedAction(palette = ColorPalette.FOXED)

            // ----- Act -----
            val result = runCatching {
                action.execute(
                    dependencies = dependencies,
                    scope = scope,
                )
            }

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `does not mutate state after executing`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = SettingsScreenUiState(
                colorPalette = ColorPalette.DEFAULT,
                dropDownExpanded = true,
            )

            coEvery {
                setColorPaletteUseCase(palette = ColorPalette.SEA)
            } returns Result.success(Unit)

            val action = OnColorPaletteSelectedAction(palette = ColorPalette.SEA)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.colorPalette shouldBe ColorPalette.DEFAULT
            stateFlow.value.dropDownExpanded shouldBe true
        }
    }
}
