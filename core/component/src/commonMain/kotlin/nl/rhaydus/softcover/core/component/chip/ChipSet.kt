package nl.rhaydus.softcover.core.component.chip

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

/**
 * A chip list paired with the payload each [ChipUiModel.key] resolves to, so the two can never
 * drift apart. An action resolves a tapped key back through the set it came from (R9).
 */
@Immutable
data class ChipSet<out P>(
    val chips: ImmutableList<ChipUiModel> = persistentListOf(),
    val payloadByKey: ImmutableMap<String, P> = persistentMapOf(),
) {
    val isEmpty: Boolean get() = chips.isEmpty()

    operator fun get(key: String): P? = payloadByKey[key]
}

fun <P> Iterable<Pair<ChipUiModel, P>>.toChipSet(): ChipSet<P> =
    ChipSet(
        chips = map { it.first }.toImmutableList(),
        payloadByKey = associate { it.first.key to it.second }.toImmutableMap(),
    )
