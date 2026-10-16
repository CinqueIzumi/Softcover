package nl.rhaydus.softcover.core.personal.data.mapper

import nl.rhaydus.softcover.core.domain.model.QuotePosition

private const val KEY_POSITION = "position"
private const val KEY_TYPE = "type"
private const val KEY_VALUE = "value"
private const val KEY_POSSIBLE = "possible"
private const val KEY_PERCENT = "percent"

private const val TYPE_PAGES = "pages"
private const val TYPE_PERCENT = "percent"
private const val TYPE_SECONDS = "seconds"

private const val PERCENT_POSSIBLE = 100

/** Builds the `metadata` jsonb Hardcover's own client writes for a quote; no position is an empty object. */
internal fun QuotePosition?.toQuoteMetadata(): Map<String, Any?> {
    val position: Map<String, Any?> = when (this) {
        null -> return emptyMap()

        is QuotePosition.Pages -> positionMap(
            type = TYPE_PAGES,
            value = page,
            possible = totalPages,
            percent = percent,
        )

        is QuotePosition.Percent -> positionMap(
            type = TYPE_PERCENT,
            value = percent.wholeOrFractional(),
            possible = PERCENT_POSSIBLE,
            percent = percent.wholeOrFractional(),
        )

        is QuotePosition.Seconds -> positionMap(
            type = TYPE_SECONDS,
            value = seconds,
            possible = totalSeconds,
            percent = percent,
        )
    }

    return mapOf(KEY_POSITION to position)
}

/**
 * Reads the position back from Apollo's default `jsonb` shape (nested [Map]s of primitives). Numbers may
 * arrive as any numeric type or a string; anything unrecognised yields `null` rather than failing the list.
 */
internal fun quotePositionFromMetadata(metadata: Any?): QuotePosition? {
    val position: Map<*, *> = (metadata as? Map<*, *>)?.get(KEY_POSITION) as? Map<*, *> ?: return null

    val value: Double? = position[KEY_VALUE].asDoubleOrNull()
    val possible: Double? = position[KEY_POSSIBLE].asDoubleOrNull()
    val percent: Double? = position[KEY_PERCENT].asDoubleOrNull()

    return when (position[KEY_TYPE]) {
        TYPE_PAGES -> value?.let {
            QuotePosition.Pages(
                page = it.toInt(),
                totalPages = possible?.toInt(),
                percent = percent,
            )
        }

        TYPE_SECONDS -> value?.let {
            QuotePosition.Seconds(
                seconds = it.toInt(),
                totalSeconds = possible?.toInt(),
                percent = percent,
            )
        }

        TYPE_PERCENT -> (percent ?: value)?.let { QuotePosition.Percent(percent = it) }

        else -> null
    }
}

private fun positionMap(
    type: String,
    value: Number,
    possible: Number?,
    percent: Number?,
): Map<String, Any?> = mapOf(
    KEY_TYPE to type,
    KEY_VALUE to value,
    KEY_POSSIBLE to possible,
    KEY_PERCENT to percent,
).filterValues { it != null }

private fun Double.wholeOrFractional(): Number = if (this % 1.0 == 0.0) toInt() else this

private fun Any?.asDoubleOrNull(): Double? = when (this) {
    is Number -> toDouble()
    is String -> toDoubleOrNull()
    else -> null
}
