package nl.rhaydus.softcover.feature.widget.randompick

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey

internal data class RandomPickState(
    val pickedBookId: Int? = null,
    val pickedAtMillis: Long? = null,
)

private val PickedBookIdKey = intPreferencesKey("random_pick_book_id")
private val PickedAtKey = longPreferencesKey("random_pick_at_millis")

internal fun Preferences.toRandomPickState(): RandomPickState = RandomPickState(
    pickedBookId = this[PickedBookIdKey],
    pickedAtMillis = this[PickedAtKey],
)

internal fun MutablePreferences.write(state: RandomPickState) {
    remove(PickedBookIdKey)
    remove(PickedAtKey)

    state.pickedBookId?.let { this[PickedBookIdKey] = it }
    state.pickedAtMillis?.let { this[PickedAtKey] = it }
}
