package nl.rhaydus.softcover.core.preferences.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import nl.rhaydus.softcover.core.preferences.data.datastore.serializer.AppSettingsSerializer
import nl.rhaydus.softcover.core.preferences.data.model.AppSettingsEntity

@JvmInline
value class AppSettingsDataStore(val store: DataStore<AppSettingsEntity>)

val Context.appSettings by dataStore(
    fileName = "app_settings.json",
    serializer = AppSettingsSerializer,
)