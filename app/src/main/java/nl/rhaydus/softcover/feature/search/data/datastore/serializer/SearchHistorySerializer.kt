package nl.rhaydus.softcover.feature.search.data.datastore.serializer

import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class SearchHistoryEntity(
    val previousQueries: List<String> = emptyList(),
)

object SearchHistorySerializer : Serializer<SearchHistoryEntity> {
    override val defaultValue: SearchHistoryEntity
        get() = SearchHistoryEntity()

    val json: Json
        get() = Json { ignoreUnknownKeys = true }

    override suspend fun readFrom(input: InputStream): SearchHistoryEntity {
        return try {
            json.decodeFromString(
                deserializer = SearchHistoryEntity.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
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
                    value = t
                ).encodeToByteArray()
            )
        }
    }
}