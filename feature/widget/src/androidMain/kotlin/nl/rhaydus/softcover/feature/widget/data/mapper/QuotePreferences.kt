package nl.rhaydus.softcover.feature.widget.data.mapper

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import nl.rhaydus.softcover.feature.widget.domain.model.QuoteSelection

private val SelectedKeyKey = stringPreferencesKey("quote_selected_key")
private val SelectedDayKey = longPreferencesKey("quote_selected_day")

internal fun Preferences.toQuoteSelection(): QuoteSelection? {
    val quoteId = this[SelectedKeyKey] ?: return null
    val day = this[SelectedDayKey] ?: return null

    return QuoteSelection(
        quoteId = quoteId,
        epochDay = day,
    )
}

internal fun MutablePreferences.write(selection: QuoteSelection) {
    this[SelectedKeyKey] = selection.quoteId
    this[SelectedDayKey] = selection.epochDay
}
