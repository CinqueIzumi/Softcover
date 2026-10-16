package nl.rhaydus.softcover.feature.widget.data.mapper

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey

private val SelectedBookKey = intPreferencesKey("selected_book_id")

internal fun Preferences.toSelectedBookId(): Int? = this[SelectedBookKey]

internal fun MutablePreferences.writeSelectedBookId(bookId: Int) {
    this[SelectedBookKey] = bookId
}
