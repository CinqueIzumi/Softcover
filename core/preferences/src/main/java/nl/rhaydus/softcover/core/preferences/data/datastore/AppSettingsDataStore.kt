package nl.rhaydus.softcover.core.preferences.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import nl.rhaydus.softcover.core.preferences.data.datastore.serializer.AppSettingsSerializer
import nl.rhaydus.softcover.core.preferences.data.model.AppSettingsEntity

@JvmInline
internal value class AppSettingsDataStore(val store: DataStore<AppSettingsEntity>)

/**
 * Single platform-specific construction site for the app-settings DataStore. The only Android-bound
 * piece is the file location, so a future KMP split only has to provide the `produceFile` path per
 * target. The path (`filesDir/datastore/app_settings.json`) matches the previous `dataStore(...)`
 * delegate, so existing stores are picked up unchanged.
 */
internal fun createAppSettingsDataStore(context: Context): DataStore<AppSettingsEntity> =
    DataStoreFactory.create(
        serializer = AppSettingsSerializer,
        produceFile = { context.dataStoreFile("app_settings.json") },
    )
