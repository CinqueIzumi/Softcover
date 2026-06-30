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
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetReadingStreakEnabledUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnReadingStreakToggledActionTest {
    private lateinit var setReadingStreakEnabledUseCase: SetReadingStreakEnabledUseCase
    private lateinit var dependencies: SettingsScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<SettingsScreenUiState>
    private lateinit var scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>

    @BeforeEach
    fun setUp() {
        setReadingStreakEnabledUseCase = mockk()
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
                mock.setReadingStreakEnabledUseCase
            } returns setReadingStreakEnabledUseCase

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
        fun `invokes use case with true when newValue is true`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setReadingStreakEnabledUseCase(enabled = true)
            } returns Result.success(Unit)

            val action = OnReadingStreakToggledAction(newValue = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                setReadingStreakEnabledUseCase(enabled = true)
            }
        }

        @Test
        fun `invokes use case with false when newValue is false`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setReadingStreakEnabledUseCase(enabled = false)
            } returns Result.success(Unit)

            val action = OnReadingStreakToggledAction(newValue = false)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                setReadingStreakEnabledUseCase(enabled = false)
            }
        }

        @Test
        fun `does not throw when the use case returns a failure`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setReadingStreakEnabledUseCase(enabled = true)
            } returns Result.failure(RuntimeException("storage error"))

            val action = OnReadingStreakToggledAction(newValue = true)

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
                readingStreakEnabledChecked = false,
                dropDownExpanded = true,
            )

            coEvery {
                setReadingStreakEnabledUseCase(enabled = true)
            } returns Result.success(Unit)

            val action = OnReadingStreakToggledAction(newValue = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.readingStreakEnabledChecked shouldBe false
            stateFlow.value.dropDownExpanded shouldBe true
        }
    }
}
