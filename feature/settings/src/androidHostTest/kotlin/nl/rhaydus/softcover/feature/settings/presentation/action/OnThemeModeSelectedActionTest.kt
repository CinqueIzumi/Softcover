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
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetThemeModeUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope

class OnThemeModeSelectedActionTest {
    private lateinit var setThemeModeUseCase: SetThemeModeUseCase
    private lateinit var dependencies: SettingsScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<SettingsScreenUiState>
    private lateinit var scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>

    @BeforeEach
    fun setUp() {
        setThemeModeUseCase = mockk()
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
                mock.setThemeModeUseCase
            } returns setThemeModeUseCase

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
        fun `invokes use case with the selected mode`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setThemeModeUseCase(mode = ThemeMode.DARK)
            } returns Result.success(Unit)

            val action = OnThemeModeSelectedAction(mode = ThemeMode.DARK)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                setThemeModeUseCase(mode = ThemeMode.DARK)
            }
        }

        @Test
        fun `invokes use case with a different selected mode`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setThemeModeUseCase(mode = ThemeMode.LIGHT)
            } returns Result.success(Unit)

            val action = OnThemeModeSelectedAction(mode = ThemeMode.LIGHT)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                setThemeModeUseCase(mode = ThemeMode.LIGHT)
            }
        }

        @Test
        fun `does not throw when the use case returns a failure`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setThemeModeUseCase(mode = ThemeMode.SYSTEM)
            } returns Result.failure(RuntimeException("storage error"))

            val action = OnThemeModeSelectedAction(mode = ThemeMode.SYSTEM)

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
                themeMode = ThemeMode.DEFAULT,
                dropDownExpanded = true,
            )

            coEvery {
                setThemeModeUseCase(mode = ThemeMode.DARK)
            } returns Result.success(Unit)

            val action = OnThemeModeSelectedAction(mode = ThemeMode.DARK)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.themeMode shouldBe ThemeMode.DEFAULT
            stateFlow.value.dropDownExpanded shouldBe true
        }
    }
}
