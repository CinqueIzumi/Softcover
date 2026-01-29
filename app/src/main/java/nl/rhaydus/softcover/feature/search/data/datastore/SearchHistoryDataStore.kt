package nl.rhaydus.softcover.feature.search.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import nl.rhaydus.softcover.feature.search.data.datastore.serializer.SearchHistoryEntity
import nl.rhaydus.softcover.feature.search.data.datastore.serializer.SearchHistorySerializer
import nl.rhaydus.softcover.feature.settings.data.model.AppSettingsEntity
import java.util.prefs.Preferences

@JvmInline
value class SearchHistoryDataStore(val store: DataStore<SearchHistoryEntity>)

val Context.searchHistory by dataStore(
    fileName = "search_history.json",
    serializer = SearchHistorySerializer,
)