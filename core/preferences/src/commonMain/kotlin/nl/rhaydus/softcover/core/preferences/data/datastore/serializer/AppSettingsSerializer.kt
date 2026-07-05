package nl.rhaydus.softcover.core.preferences.data.datastore.serializer

import androidx.datastore.core.okio.OkioSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.preferences.data.model.AppSettingsEntity

internal object AppSettingsSerializer : OkioSerializer<AppSettingsEntity> {
    override val defaultValue: AppSettingsEntity
        get() = AppSettingsEntity()

    private val json: Json = Json { ignoreUnknownKeys = true }

    override suspend fun readFrom(source: BufferedSource): AppSettingsEntity {
        return try {
            json.decodeFromString(
                deserializer = AppSettingsEntity.serializer(),
                string = source.readUtf8(),
            )
        } catch (e: SerializationException) {
            AppLog.e(
                e,
                "Failed to deserialize AppSettings; falling back to default",
            )

            defaultValue
        }
    }

    override suspend fun writeTo(
        t: AppSettingsEntity,
        sink: BufferedSink,
    ) {
        sink.writeUtf8(
            json.encodeToString(
                serializer = AppSettingsEntity.serializer(),
                value = t,
            ),
        )
    }
}
