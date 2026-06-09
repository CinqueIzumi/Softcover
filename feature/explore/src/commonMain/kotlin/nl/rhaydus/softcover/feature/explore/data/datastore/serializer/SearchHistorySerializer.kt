package nl.rhaydus.softcover.feature.explore.data.datastore.serializer

import androidx.datastore.core.okio.OkioSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource
import nl.rhaydus.softcover.core.domain.logging.AppLog

internal object SearchHistorySerializer : OkioSerializer<SearchHistoryEntity> {
    override val defaultValue: SearchHistoryEntity
        get() = SearchHistoryEntity()

    private val json: Json = Json { ignoreUnknownKeys = true }

    override suspend fun readFrom(source: BufferedSource): SearchHistoryEntity {
        return try {
            json.decodeFromString(
                deserializer = SearchHistoryEntity.serializer(),
                string = source.readUtf8(),
            )
        } catch (e: SerializationException) {
            AppLog.e(
                e,
                "Failed to deserialize search history",
            )

            defaultValue
        }
    }

    override suspend fun writeTo(
        t: SearchHistoryEntity,
        sink: BufferedSink,
    ) {
        sink.writeUtf8(
            json.encodeToString(
                serializer = SearchHistoryEntity.serializer(),
                value = t,
            ),
        )
    }
}
