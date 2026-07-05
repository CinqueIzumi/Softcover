package nl.rhaydus.softcover.core.profile.data.datastore.serializer

import androidx.datastore.core.okio.OkioSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.profile.data.model.ProfileCacheEntity

internal object ProfileCacheSerializer : OkioSerializer<ProfileCacheEntity> {
    override val defaultValue: ProfileCacheEntity
        get() = ProfileCacheEntity()

    private val json: Json = Json { ignoreUnknownKeys = true }

    override suspend fun readFrom(source: BufferedSource): ProfileCacheEntity {
        return try {
            json.decodeFromString(
                deserializer = ProfileCacheEntity.serializer(),
                string = source.readUtf8(),
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
        sink: BufferedSink,
    ) {
        sink.writeUtf8(
            json.encodeToString(
                serializer = ProfileCacheEntity.serializer(),
                value = t,
            ),
        )
    }
}
