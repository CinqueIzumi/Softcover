package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

/**
 * A card list paired with the payload each [BookCardUiModel.key] resolves to. An event resolves back
 * through the set it came from.
 */
@Immutable
@ConsistentCopyVisibility
data class BookCardSet<out P> internal constructor(
    val cards: ImmutableList<BookCardUiModel>,
    val payloadByKey: ImmutableMap<BookCardKey, P>,
) {
    val isEmpty: Boolean get() = cards.isEmpty()

    operator fun get(key: BookCardKey): P? = payloadByKey[key]

    companion object {
        fun standIns(cards: ImmutableList<BookCardUiModel>): BookCardSet<Nothing> =
            BookCardSet(
                cards = cards,
                payloadByKey = persistentMapOf(),
            )

        fun empty(): BookCardSet<Nothing> = standIns(persistentListOf())
    }
}

fun <P> Iterable<Pair<BookCardUiModel, P>>.toBookCardSet(): BookCardSet<P> =
    BookCardSet(
        cards = map { it.first }.toImmutableList(),
        payloadByKey = associate { it.first.key to it.second }.toImmutableMap(),
    )
