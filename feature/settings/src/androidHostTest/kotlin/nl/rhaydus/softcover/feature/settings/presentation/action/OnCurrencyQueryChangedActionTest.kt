package nl.rhaydus.softcover.feature.settings.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Currency
import nl.rhaydus.softcover.feature.settings.presentation.event.CurrencySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.CurrencySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnCurrencyQueryChangedActionTest {
    private lateinit var stateFlow: MutableStateFlow<CurrencySettingsUiState>
    private lateinit var scope: ActionScope<CurrencySettingsUiState, CurrencySettingsEvent, CurrencySettingsLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(
            CurrencySettingsUiState(
                currencies = persistentListOf(
                    Currency(
                        code = "EUR",
                        name = "Euro",
                        symbol = "€",
                        fractionDigits = 2,
                    ),
                    Currency(
                        code = "USD",
                        name = "US Dollar",
                        symbol = "$",
                        fractionDigits = 2,
                    ),
                    Currency(
                        code = "JPY",
                        name = "Japanese Yen",
                        symbol = "¥",
                        fractionDigits = 0,
                    ),
                ),
                selectedCode = "EUR",
            ),
        )

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(CurrencySettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `filters rows by code or name ignoring case`() = runTest {
            // ----- Arrange -----
            val action = OnCurrencyQueryChangedAction(query = "yen")

            // ----- Act -----
            action.execute(
                dependencies = mockk<CurrencySettingsDependencies>(),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.query shouldBe "yen"
            stateFlow.value.rows.map { it.key } shouldBe listOf("JPY")
        }

        @Test
        fun `a blank query restores every row`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(query = "yen")
            val action = OnCurrencyQueryChangedAction(query = "   ")

            // ----- Act -----
            action.execute(
                dependencies = mockk<CurrencySettingsDependencies>(),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.rows.map { it.key } shouldBe listOf("EUR", "JPY", "USD")
        }
    }
}
