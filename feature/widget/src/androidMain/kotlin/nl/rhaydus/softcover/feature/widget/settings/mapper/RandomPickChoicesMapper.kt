package nl.rhaydus.softcover.feature.widget.settings.mapper

import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.widget.randompick.RandomPickCadence
import nl.rhaydus.softcover.feature.widget.randompick.RandomPickSource
import nl.rhaydus.softcover.feature.widget.randompick.resolveSource

internal fun sourceChipsFor(
    selected: RandomPickSource,
    lists: List<BookList>,
): ChipSet<RandomPickSource> {
    val shown = resolveSource(
        source = selected,
        lists = lists,
    )

    val choices = buildList {
        add("Want to Read" to RandomPickSource.WantToRead)
        add("Owned" to RandomPickSource.Owned)

        lists
            .filterNot { it.isOwned }
            .sortedBy { it.name.lowercase() }
            .forEach { add(it.name to RandomPickSource.UserList(listId = it.id)) }
    }

    return choices.toChipSet(selected = shown)
}

internal fun cadenceChipsFor(selected: RandomPickCadence): ChipSet<RandomPickCadence> = listOf(
    "Daily" to RandomPickCadence.Daily,
    "Weekly" to RandomPickCadence.Weekly,
    "Only when I shuffle" to RandomPickCadence.OnShuffleOnly,
).toChipSet(selected = selected)

private fun <P : Any> List<Pair<String, P>>.toChipSet(selected: P): ChipSet<P> {
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
