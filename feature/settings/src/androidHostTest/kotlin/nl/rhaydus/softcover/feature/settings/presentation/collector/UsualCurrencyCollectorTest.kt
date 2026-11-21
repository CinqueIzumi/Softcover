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
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetCurrenciesUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetUsualCurrencyAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.CurrencySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.CurrencySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UsualCurrencyCollectorTest {
    private val euro = Currency(
        code = "EUR",
        name = "Euro",
        symbol = "€",
        fractionDigits = 2,
    )
    private val dollar = Currency(
        code = "USD",
        name = "US Dollar",
        symbol = "$",
        fractionDigits = 2,
    )
    private val yen = Currency(
        code = "JPY",
        name = "Japanese Yen",
        symbol = "¥",
        fractionDigits = 0,
    )

    private lateinit var dependencies: CurrencySettingsDependencies
    private lateinit var stateFlow: MutableStateFlow<CurrencySettingsUiState>
    private lateinit var scope: ActionScope<CurrencySettingsUiState, CurrencySettingsEvent, CurrencySettingsLocalVariables>
    private lateinit var currencyFlow: MutableSharedFlow<Currency>

    @BeforeEach
    fun setUp() {
        currencyFlow = MutableSharedFlow()
        stateFlow = MutableStateFlow(CurrencySettingsUiState())

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(CurrencySettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        val getCurrencies = mockk<GetCurrenciesUseCase>()
        val getUsual = mockk<GetUsualCurrencyAsFlowUseCase>()

        every {
            getCurrencies()
        } returns listOf(dollar, euro, yen)

        every {
            getUsual()
        } returns currencyFlow

        dependencies = mockk<CurrencySettingsDependencies>(relaxed = true).also { mock ->
            every {
                mock.getCurrenciesUseCase
            } returns getCurrencies

            every {
                mock.getUsualCurrencyAsFlowUseCase
            } returns getUsual
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `lists the stored usual currency first and marks only it selected`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = UsualCurrencyCollector()

            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            currencyFlow.emit(yen)

            // ----- Assert -----
            stateFlow.value.selectedCode shouldBe "JPY"

            stateFlow.value.rows.map { it.key to it.selected } shouldBe listOf(
                "JPY" to true,
                "EUR" to false,
                "USD" to false,
            )

            job.cancel()
        }

        @Test
        fun `moves the selection when the usual currency changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = UsualCurrencyCollector()

            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            currencyFlow.emit(yen)
            currencyFlow.emit(dollar)

            // ----- Assert -----
            stateFlow.value.rows.single { it.selected }.key shouldBe "USD"
            stateFlow.value.rows.first().key shouldBe "USD"
            job.cancel()
        }

        @Test
        fun `keeps the typed query applied when the usual currency changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(query = "dollar")
            val collector = UsualCurrencyCollector()

            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            currencyFlow.emit(euro)

            // ----- Assert -----
            stateFlow.value.rows.map { it.key } shouldBe listOf("USD")
            job.cancel()
        }
    }
}
