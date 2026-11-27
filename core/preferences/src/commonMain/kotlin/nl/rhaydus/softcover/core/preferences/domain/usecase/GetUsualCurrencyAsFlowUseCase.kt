package nl.rhaydus.softcover.core.preferences.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.Currency
import nl.rhaydus.softcover.core.preferences.data.datasource.CurrencyCatalog
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class GetUsualCurrencyAsFlowUseCase(
    private val settingsRepository: SettingsRepository,
    private val currencyCatalog: CurrencyCatalog,
) {
    operator fun invoke(): Flow<Currency> = settingsRepository.usualCurrencyCode.map { stored ->
        val currencies = currencyCatalog.all()
        val deviceDefault = currencyCatalog.deviceDefaultCode()

        listOfNotNull(stored, deviceDefault, FALLBACK_CODE)
            .firstNotNullOfOrNull { code -> currencies.firstOrNull { it.code == code } }
            ?: Currency(
                code = FALLBACK_CODE,
                name = "Euro",
                symbol = "€",
                fractionDigits = 2,
            )
    }

    private companion object {
        const val FALLBACK_CODE = "EUR"
    }
}
