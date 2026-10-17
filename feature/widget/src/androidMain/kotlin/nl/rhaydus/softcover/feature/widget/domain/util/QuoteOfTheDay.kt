package nl.rhaydus.softcover.feature.widget.domain.util

import nl.rhaydus.softcover.core.domain.model.Quote

private const val FNV_OFFSET = 0xcbf29ce484222325UL
private const val FNV_PRIME = 0x100000001b3UL
private const val MIX_INCREMENT = 0x9e3779b97f4a7c15UL
private const val MIX_FIRST = 0xbf58476d1ce4e5b9UL
private const val MIX_SECOND = 0x94d049bb133111ebUL
private const val MIX_FIRST_SHIFT = 30
private const val MIX_SECOND_SHIFT = 27
private const val MIX_FINAL_SHIFT = 31

/**
 * A key that survives a refresh: `localId` is reassigned on every refresh and `remoteId` only exists after
 * the quote has synced, so neither can identify a quote across them.
 */
internal fun quoteKey(quote: Quote): String =
    "${quote.bookId}:${quote.actionAt.toEpochMilliseconds()}:${quote.text}"

/**
 * A compact id for [quote], stable across refreshes, small enough to persist per widget instance in place
 * of the whole quote text.
 */
internal fun quoteId(quote: Quote): String = keyHash(quoteKey(quote)).toString()

/** [quotes] ordered by a hash of each key with [epochDay], so the order changes every day; rank 0 is the quote of the day. */
internal fun rankForDay(
    quotes: List<Quote>,
    epochDay: Long,
): List<Quote> {
    val dayMix = splitMix(epochDay.toULong())

    return quotes
        .distinctBy(::quoteKey)
        .sortedWith(
            compareBy<Quote>(
                { splitMix(keyHash(quoteKey(it)) xor dayMix) },
                ::quoteKey,
            ),
        )
}

/** The quote the user picked today if it still exists, otherwise the quote of the day. */
internal fun resolveShown(
    ranking: List<Quote>,
    selectedQuoteId: String?,
    selectedDay: Long?,
    today: Long,
): Quote? {
    val selected = if (selectedDay == today && selectedQuoteId != null) {
        ranking.firstOrNull { quoteId(it) == selectedQuoteId }
    } else {
        null
    }

    return selected ?: ranking.firstOrNull()
}

/** The quote [step] places from [currentQuoteId], wrapping around [ranking]. */
internal fun stepQuote(
    ranking: List<Quote>,
    currentQuoteId: String?,
    step: Int,
): Quote? {
    if (ranking.isEmpty()) return null

    val currentIndex = ranking.indexOfFirst { quoteId(it) == currentQuoteId }.coerceAtLeast(0)

    return ranking[(currentIndex + step).mod(ranking.size)]
}

private fun keyHash(key: String): ULong = key.fold(FNV_OFFSET) { hash, char ->
    (hash xor char.code.toULong()) * FNV_PRIME
}

private fun splitMix(value: ULong): ULong {
    val seeded = value + MIX_INCREMENT
    val first = (seeded xor (seeded shr MIX_FIRST_SHIFT)) * MIX_FIRST
    val second = (first xor (first shr MIX_SECOND_SHIFT)) * MIX_SECOND

    return second xor (second shr MIX_FINAL_SHIFT)
}
