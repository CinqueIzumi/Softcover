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
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDateStyleUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnDateStyleClickActionTest {
    private lateinit var setDateStyleUseCase: SetDateStyleUseCase
    private lateinit var dependencies: SettingsScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<SettingsScreenUiState>
    private lateinit var scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>

    @BeforeEach
    fun setUp() {
        setDateStyleUseCase = mockk()
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
                mock.setDateStyleUseCase
            } returns setDateStyleUseCase

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
        fun `closes dropdown after invoking the use case on success`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = SettingsScreenUiState(dropDownExpanded = true)

            coEvery {
                setDateStyleUseCase(newStyle = DateStyle.MONTH_DAY_YEAR)
            } returns Result.success(Unit)

            val action = OnDateStyleClickAction(style = DateStyle.MONTH_DAY_YEAR)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.dropDownExpanded shouldBe false
        }

        @Test
        fun `closes dropdown even when the use case fails`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = SettingsScreenUiState(dropDownExpanded = true)

            coEvery {
                setDateStyleUseCase(newStyle = DateStyle.YEAR_MONTH_DAY)
            } returns Result.failure(RuntimeException("storage error"))

            val action = OnDateStyleClickAction(style = DateStyle.YEAR_MONTH_DAY)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.dropDownExpanded shouldBe false
        }

        @Test
        fun `invokes use case with the style provided to the action constructor`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setDateStyleUseCase(newStyle = DateStyle.DAY_MONTH_YEAR)
            } returns Result.success(Unit)

            val action = OnDateStyleClickAction(style = DateStyle.DAY_MONTH_YEAR)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                setDateStyleUseCase(newStyle = DateStyle.DAY_MONTH_YEAR)
            }
        }

        @Test
        fun `preserves other state fields when closing dropdown`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = SettingsScreenUiState(
                useFloatingBarChecked = false,
                dropDownExpanded = true,
            )

            coEvery {
                setDateStyleUseCase(newStyle = DateStyle.MONTH_DAY_YEAR)
            } returns Result.success(Unit)

            val action = OnDateStyleClickAction(style = DateStyle.MONTH_DAY_YEAR)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.useFloatingBarChecked shouldBe false
            stateFlow.value.dropDownExpanded shouldBe false
        }
    }
}
