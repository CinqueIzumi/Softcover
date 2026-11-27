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
import nl.rhaydus.softcover.core.domain.model.Currency
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetUsualCurrencyAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UsualCurrencyGlossCollectorTest {
    private lateinit var dependencies: SettingsScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<SettingsScreenUiState>
    private lateinit var scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>
    private lateinit var currencyFlow: MutableSharedFlow<Currency>

    @BeforeEach
    fun setUp() {
        currencyFlow = MutableSharedFlow()
        stateFlow = MutableStateFlow(SettingsScreenUiState())

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(SettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        val useCase = mockk<GetUsualCurrencyAsFlowUseCase>()

        every {
            useCase()
        } returns currencyFlow

        dependencies = mockk<SettingsScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.getUsualCurrencyAsFlowUseCase
            } returns useCase
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `gloss follows the latest stored usual currency`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = UsualCurrencyGlossCollector()

            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            currencyFlow.emit(
                Currency(
                    code = "EUR",
                    name = "Euro",
                    symbol = "€",
                    fractionDigits = 2,
                ),
            )

            currencyFlow.emit(
                Currency(
                    code = "JPY",
                    name = "Japanese Yen",
                    symbol = "¥",
                    fractionDigits = 0,
                ),
            )

            // ----- Assert -----
            stateFlow.value.currencyGloss shouldBe "Japanese Yen, for the prices you note"
            job.cancel()
        }
    }
}
