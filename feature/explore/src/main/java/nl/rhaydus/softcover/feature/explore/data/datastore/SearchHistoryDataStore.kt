package nl.rhaydus.softcover.feature.explore.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import nl.rhaydus.softcover.feature.explore.data.datastore.serializer.SearchHistoryEntity
import nl.rhaydus.softcover.feature.explore.data.datastore.serializer.SearchHistorySerializer

@JvmInline
internal value class SearchHistoryDataStore(val store: DataStore<SearchHistoryEntity>)

internal val Context.searchHistory by dataStore(
    fileName = "search_history.json",
    serializer = SearchHistorySerializer,
)