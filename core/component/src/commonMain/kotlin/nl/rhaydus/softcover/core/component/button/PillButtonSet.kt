package nl.rhaydus.softcover.core.component.button

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

/**
 * A pill list paired with the payload each [PillButtonUiModel.key] resolves to, so the two can never
 * drift apart. An action resolves a tapped key back through the set it came from.
 */
@Immutable
data class PillButtonSet<out P>(
    val pills: ImmutableList<PillButtonUiModel> = persistentListOf(),
    val payloadByKey: ImmutableMap<String, P> = persistentMapOf(),
) {
    val isEmpty: Boolean get() = pills.isEmpty()

    operator fun get(key: String): P? = payloadByKey[key]
}

fun <P> Iterable<Pair<PillButtonUiModel, P>>.toPillButtonSet(): PillButtonSet<P> =
    PillButtonSet(
        pills = map { it.first }.toImmutableList(),
        payloadByKey = associate { it.first.key to it.second }.toImmutableMap(),
    )
