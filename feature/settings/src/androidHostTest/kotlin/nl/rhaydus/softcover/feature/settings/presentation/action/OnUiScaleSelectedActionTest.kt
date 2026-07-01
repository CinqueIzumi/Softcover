package nl.rhaydus.softcover.feature.settings.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.UiScale
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetUiScaleUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnUiScaleSelectedActionTest {
    private lateinit var setUiScaleUseCase: SetUiScaleUseCase
    private lateinit var dependencies: SettingsScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<SettingsScreenUiState>
    private lateinit var scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>

    @BeforeEach
    fun setUp() {
        setUiScaleUseCase = mockk()
        stateFlow = MutableStateFlow(SettingsScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(SettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): SettingsScreenDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<SettingsScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.setUiScaleUseCase
            } returns setUiScaleUseCase

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
        fun `invokes use case with the selected scale`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setUiScaleUseCase(scale = UiScale.PERCENT_150)
            } returns Result.success(Unit)

            val action = OnUiScaleSelectedAction(scale = UiScale.PERCENT_150)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                setUiScaleUseCase(scale = UiScale.PERCENT_150)
            }
        }

        @Test
        fun `invokes use case with a different selected scale`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setUiScaleUseCase(scale = UiScale.PERCENT_200)
            } returns Result.success(Unit)

            val action = OnUiScaleSelectedAction(scale = UiScale.PERCENT_200)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                setUiScaleUseCase(scale = UiScale.PERCENT_200)
            }
        }

        @Test
        fun `does not throw when the use case returns a failure`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setUiScaleUseCase(scale = UiScale.PERCENT_150)
            } returns Result.failure(RuntimeException("storage error"))

            val action = OnUiScaleSelectedAction(scale = UiScale.PERCENT_150)

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
                uiScale = UiScale.DEFAULT,
                dropDownExpanded = true,
            )

            coEvery {
                setUiScaleUseCase(scale = UiScale.PERCENT_150)
            } returns Result.success(Unit)

            val action = OnUiScaleSelectedAction(scale = UiScale.PERCENT_150)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.uiScale shouldBe UiScale.DEFAULT
            stateFlow.value.dropDownExpanded shouldBe true
        }
    }
}
