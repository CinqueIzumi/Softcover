package nl.rhaydus.softcover.feature.explore.data.datastore.serializer

import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream

internal object SearchHistorySerializer : Serializer<SearchHistoryEntity> {
    override val defaultValue: SearchHistoryEntity
        get() = SearchHistoryEntity()

    private val json: Json
        get() = Json { ignoreUnknownKeys = true }

    override suspend fun readFrom(input: InputStream): SearchHistoryEntity {
        return try {
            json.decodeFromString(
                deserializer = SearchHistoryEntity.serializer(),
                string = input.readBytes().decodeToString(),
            )
        } catch (e: SerializationException) {
            Timber.e(
                e,
                "Failed to deserialize search history",
            )

            defaultValue
        }
    }

    override suspend fun writeTo(
        t: SearchHistoryEntity,
        output: OutputStream,
    ) {
        withContext(Dispatchers.IO) {
            output.write(
                json.encodeToString(
                    serializer = SearchHistoryEntity.serializer(),
                    value = t,
                ).encodeToByteArray(),
            )
        }
    }
}
