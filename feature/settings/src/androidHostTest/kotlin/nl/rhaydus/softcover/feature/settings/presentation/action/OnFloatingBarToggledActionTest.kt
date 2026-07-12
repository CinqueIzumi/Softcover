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
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetBottomBarStyleUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope

class OnFloatingBarToggledActionTest {
    private lateinit var setBottomBarStyleUseCase: SetBottomBarStyleUseCase
    private lateinit var dependencies: SettingsScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<SettingsScreenUiState>
    private lateinit var scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>

    @BeforeEach
    fun setUp() {
        setBottomBarStyleUseCase = mockk()
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
                mock.setBottomBarStyleUseCase
            } returns setBottomBarStyleUseCase

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
        fun `invokes use case with FLOATING when newValue is true`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setBottomBarStyleUseCase(newStyle = BottomBarStyle.FLOATING)
            } returns Result.success(Unit)

            val action = OnFloatingBarToggledAction(newValue = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                setBottomBarStyleUseCase(newStyle = BottomBarStyle.FLOATING)
            }
        }

        @Test
        fun `invokes use case with DOCKED when newValue is false`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setBottomBarStyleUseCase(newStyle = BottomBarStyle.DOCKED)
            } returns Result.success(Unit)

            val action = OnFloatingBarToggledAction(newValue = false)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                setBottomBarStyleUseCase(newStyle = BottomBarStyle.DOCKED)
            }
        }

        @Test
        fun `does not throw when the use case returns a failure`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setBottomBarStyleUseCase(newStyle = BottomBarStyle.FLOATING)
            } returns Result.failure(RuntimeException("storage error"))

            val action = OnFloatingBarToggledAction(newValue = true)

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
                useFloatingBarChecked = false,
                dropDownExpanded = true,
            )

            coEvery {
                setBottomBarStyleUseCase(newStyle = BottomBarStyle.FLOATING)
            } returns Result.success(Unit)

            val action = OnFloatingBarToggledAction(newValue = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.useFloatingBarChecked shouldBe false
            stateFlow.value.dropDownExpanded shouldBe true
        }
    }
}
