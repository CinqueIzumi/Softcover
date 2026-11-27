package nl.rhaydus.softcover.core.preferences.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Currency
import nl.rhaydus.softcover.core.preferences.data.datasource.CurrencyCatalog

class GetCurrenciesUseCase(
    private val currencyCatalog: CurrencyCatalog,
) {
    operator fun invoke(): List<Currency> = currencyCatalog.all().sortedBy { it.name }
}
