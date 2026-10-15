package nl.rhaydus.softcover.core.preferences.data.datastore.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject

/**
 * Serializes a `Map<String, E>` like the default map serializer, but drops entries whose value is not
 * a known constant of [E] instead of failing the whole decode. `coerceInputValues` does not reach map
 * values, so a newer build's enum constant would otherwise wipe every setting.
 */
internal open class LenientEnumMapSerializer<E : Enum<E>>(
    private val enumSerializer: KSerializer<E>,
) : KSerializer<Map<String, E>> {
    private val delegate = MapSerializer(
        keySerializer = String.serializer(),
        valueSerializer = enumSerializer,
    )

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(
        encoder: Encoder,
        value: Map<String, E>,
    ) = delegate.serialize(
        encoder = encoder,
        value = value,
    )

    override fun deserialize(decoder: Decoder): Map<String, E> {
        val jsonDecoder = decoder as? JsonDecoder ?: return delegate.deserialize(decoder)

        val entries = jsonDecoder.decodeJsonElement() as? JsonObject
            ?: throw SerializationException("Expected a JSON object for ${descriptor.serialName}")

        return buildMap {
            entries.forEach { (key, element) ->
                try {
                    put(
                        key,
                        jsonDecoder.json.decodeFromJsonElement(
                            deserializer = enumSerializer,
                            element = element,
                        ),
                    )
                } catch (_: SerializationException) {
                    // Unknown constant: skip only this entry.
                }
            }
        }
    }
}
