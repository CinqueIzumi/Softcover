package nl.rhaydus.softcover.core.preferences.data.datasource

import nl.rhaydus.softcover.core.domain.model.Currency

interface CurrencyCatalog {
    fun all(): List<Currency>

    fun deviceDefaultCode(): String?
}
