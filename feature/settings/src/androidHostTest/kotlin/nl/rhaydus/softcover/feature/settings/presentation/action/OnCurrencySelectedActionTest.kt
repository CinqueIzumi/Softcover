package nl.rhaydus.softcover.feature.settings.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetUsualCurrencyUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.CurrencySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.CurrencySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnCurrencySelectedActionTest {
    private lateinit var stateFlow: MutableStateFlow<CurrencySettingsUiState>
    private lateinit var events: Channel<CurrencySettingsEvent>
    private lateinit var scope: ActionScope<CurrencySettingsUiState, CurrencySettingsEvent, CurrencySettingsLocalVariables>
    private lateinit var setUsualCurrencyUseCase: SetUsualCurrencyUseCase
    private lateinit var dependencies: CurrencySettingsDependencies

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(CurrencySettingsUiState(selectedCode = "EUR"))
        events = Channel(Channel.BUFFERED)

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(CurrencySettingsLocalVariables()),
            eventChannel = events,
        )

        setUsualCurrencyUseCase = mockk()
        dependencies = mockk(relaxed = true)

        every {
            dependencies.setUsualCurrencyUseCase
        } returns setUsualCurrencyUseCase
    }

    @Nested
    inner class Execute {
        @Test
        fun `stores the chosen code and leaves the page as it is`() = runTest {
            // ----- Arrange -----
            coEvery {
                setUsualCurrencyUseCase(code = "USD")
            } returns Result.success(Unit)

            val action = OnCurrencySelectedAction(code = "USD")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                setUsualCurrencyUseCase(code = "USD")
            }

            stateFlow.value shouldBe CurrencySettingsUiState(selectedCode = "EUR")
            events.tryReceive().isSuccess shouldBe false
        }

        @Test
        fun `a failed save does not throw or change the page`() = runTest {
            // ----- Arrange -----
            coEvery {
                setUsualCurrencyUseCase(code = "USD")
            } returns Result.failure(IllegalStateException("disk full"))

            val action = OnCurrencySelectedAction(code = "USD")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe CurrencySettingsUiState(selectedCode = "EUR")
        }
    }
}
