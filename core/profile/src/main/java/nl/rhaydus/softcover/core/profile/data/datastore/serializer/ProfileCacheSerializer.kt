package nl.rhaydus.softcover.core.profile.data.datastore.serializer

import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.core.profile.data.model.ProfileCacheEntity

internal object ProfileCacheSerializer : Serializer<ProfileCacheEntity> {
    override val defaultValue: ProfileCacheEntity
        get() = ProfileCacheEntity()

    val json: Json
        get() = Json { ignoreUnknownKeys = true }

    override suspend fun readFrom(input: InputStream): ProfileCacheEntity {
        return try {
            json.decodeFromString(
                deserializer = ProfileCacheEntity.serializer(),
                string = input.readBytes().decodeToString(),
            )
        } catch (e: SerializationException) {
            AppLog.e(
                e,
                "Failed to deserialize ProfileCache; falling back to default",
            )

            defaultValue
        }
    }

    override suspend fun writeTo(
        t: ProfileCacheEntity,
        output: OutputStream,
    ) {
        withContext(Dispatchers.IO) {
            output.write(
                json.encodeToString(
                    serializer = ProfileCacheEntity.serializer(),
                    value = t,
                ).encodeToByteArray(),
            )
        }
    }
}
