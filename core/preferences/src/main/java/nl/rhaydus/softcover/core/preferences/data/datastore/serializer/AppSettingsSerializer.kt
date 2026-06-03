package nl.rhaydus.softcover.core.preferences.data.datastore.serializer

import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.core.preferences.data.model.AppSettingsEntity

internal object AppSettingsSerializer : Serializer<AppSettingsEntity> {
    override val defaultValue: AppSettingsEntity
        get() = AppSettingsEntity()

    val json: Json
        get() = Json { ignoreUnknownKeys = true }

    override suspend fun readFrom(input: InputStream): AppSettingsEntity {
        return try {
            json.decodeFromString(
                deserializer = AppSettingsEntity.serializer(),
                string = input.readBytes().decodeToString(),
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
        output: OutputStream,
    ) {
        withContext(Dispatchers.IO) {
            output.write(
                json.encodeToString(
                    serializer = AppSettingsEntity.serializer(),
                    value = t,
                ).encodeToByteArray(),
            )
        }
    }
}
