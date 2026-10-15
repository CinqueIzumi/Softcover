package nl.rhaydus.softcover.feature.widget.randompick

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

internal data class RandomPickSettings(
    val source: RandomPickSource = RandomPickSource.WantToRead,
    val cadence: RandomPickCadence = RandomPickCadence.Daily,
)

private val SourceKindKey = stringPreferencesKey("random_pick_source_kind")
private val SourceListIdKey = intPreferencesKey("random_pick_source_list_id")
private val CadenceKey = stringPreferencesKey("random_pick_cadence")

private const val KIND_OWNED = "owned"
private const val KIND_LIST = "list"

internal fun Preferences.toRandomPickSettings(): RandomPickSettings {
    val listId = this[SourceListIdKey]

    val source = when {
        this[SourceKindKey] == KIND_OWNED -> RandomPickSource.Owned
        this[SourceKindKey] == KIND_LIST && listId != null -> RandomPickSource.UserList(listId = listId)
        else -> RandomPickSource.WantToRead
    }

    val cadence = RandomPickCadence.entries.firstOrNull { it.name == this[CadenceKey] }
        ?: RandomPickCadence.Daily

    return RandomPickSettings(
        source = source,
        cadence = cadence,
    )
}

internal fun MutablePreferences.write(settings: RandomPickSettings) {
    remove(SourceKindKey)
    remove(SourceListIdKey)

    when (val source = settings.source) {
        RandomPickSource.WantToRead -> Unit
        RandomPickSource.Owned -> this[SourceKindKey] = KIND_OWNED
        is RandomPickSource.UserList -> {
            this[SourceKindKey] = KIND_LIST
            this[SourceListIdKey] = source.listId
        }
    }

    this[CadenceKey] = settings.cadence.name
}
