package nl.rhaydus.softcover.core.preferences.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Currency
import nl.rhaydus.softcover.core.preferences.data.datasource.CurrencyCatalog
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetUsualCurrencyAsFlowUseCaseTest {
    private val eur = Currency(
        code = "EUR",
        name = "Euro",
        symbol = "€",
        fractionDigits = 2,
    )
    private val usd = Currency(
        code = "USD",
        name = "US Dollar",
        symbol = "US$",
        fractionDigits = 2,
    )
    private val jpy = Currency(
        code = "JPY",
        name = "Japanese Yen",
        symbol = "JP¥",
        fractionDigits = 0,
    )

    private lateinit var storedCode: MutableStateFlow<String?>
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var currencyCatalog: CurrencyCatalog
    private lateinit var useCase: GetUsualCurrencyAsFlowUseCase

    @BeforeEach
    fun setUp() {
        storedCode = MutableStateFlow(null)
        val codes = storedCode
        settingsRepository = mockk()

        every {
            settingsRepository.usualCurrencyCode
        } returns codes

        currencyCatalog = mockk()

        every {
            currencyCatalog.all()
        } returns listOf(usd, jpy, eur)

        every {
            currencyCatalog.deviceDefaultCode()
        } returns "JPY"

        useCase = GetUsualCurrencyAsFlowUseCase(
            settingsRepository = settingsRepository,
            currencyCatalog = currencyCatalog,
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `the stored code wins over the device default`() = runTest {
            // ----- Arrange -----
            storedCode.value = "USD"

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe usd
            }
        }

        @Test
        fun `no stored code falls back to the device default`() = runTest {
            // ----- Arrange -----
            storedCode.value = null

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe jpy
            }
        }

        @Test
        fun `a stored code the catalogue lacks falls back to the device default`() = runTest {
            // ----- Arrange -----
            storedCode.value = "ZZZ"

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe jpy
            }
        }

        @Test
        fun `no stored code and no device default falls back to euro`() = runTest {
            // ----- Arrange -----
            every {
                currencyCatalog.deviceDefaultCode()
            } returns null

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe eur
            }
        }

        @Test
        fun `an empty catalogue still yields euro`() = runTest {
            // ----- Arrange -----
            every {
                currencyCatalog.all()
            } returns emptyList()

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe eur
            }
        }

        @Test
        fun `a changed stored code emits the new currency`() = runTest {
            // ----- Arrange -----
            storedCode.value = "USD"

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe usd
                storedCode.value = "EUR"
                awaitItem() shouldBe eur
            }
        }
    }
}
