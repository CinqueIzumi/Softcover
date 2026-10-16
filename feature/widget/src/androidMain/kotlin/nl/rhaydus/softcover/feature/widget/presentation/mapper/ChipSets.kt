package nl.rhaydus.softcover.feature.widget.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel

internal fun <P : Any> List<Pair<String, P>>.toChipSet(selected: P): ChipSet<P> {
    val keyed = mapIndexed { index, (label, payload) ->
        "$index" to (label to payload)
    }

    return ChipSet(
        chips = keyed.map { (key, choice) ->
            ChipUiModel(
                key = key,
                label = choice.first,
                tone = ChipTone.Choice,
                selected = choice.second == selected,
            )
        }.toImmutableList(),
        payloadByKey = keyed.associate { (key, choice) -> key to choice.second }.toImmutableMap(),
    )
}
