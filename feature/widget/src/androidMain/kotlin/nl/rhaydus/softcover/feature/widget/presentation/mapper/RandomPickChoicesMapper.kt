package nl.rhaydus.softcover.feature.widget.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickCadence
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSource
import nl.rhaydus.softcover.feature.widget.domain.util.resolveSource

internal fun sourceChipsFor(
    selected: RandomPickSource,
    lists: List<BookList>,
): ChipSet<RandomPickSource> {
    val shown = resolveSource(
        source = selected,
        lists = lists,
    )

    val choices = buildList {
        add(WANT_TO_READ_LABEL to RandomPickSource.WantToRead)
        add(OWNED_LABEL to RandomPickSource.Owned)

        lists
            .filterNot { it.isOwned }
            .sortedBy { it.name.lowercase() }
            .forEach { add(it.name to RandomPickSource.UserList(listId = it.id)) }
    }

    return choices.toChipSet(selected = shown)
}

internal fun cadenceChipsFor(selected: RandomPickCadence): ChipSet<RandomPickCadence> = listOf(
    RandomPickCadence.Daily,
    RandomPickCadence.Weekly,
    RandomPickCadence.OnShuffleOnly,
).map { it.label() to it }.toChipSet(selected = selected)

internal fun RandomPickCadence.label(): String = when (this) {
    RandomPickCadence.Daily -> "Daily"
    RandomPickCadence.Weekly -> "Weekly"
    RandomPickCadence.OnShuffleOnly -> "Only when I shuffle"
}

internal fun RandomPickSource.label(lists: List<BookList>): String = when (this) {
    RandomPickSource.WantToRead -> WANT_TO_READ_LABEL
    RandomPickSource.Owned -> OWNED_LABEL
    is RandomPickSource.UserList -> lists.firstOrNull { it.id == listId }?.name ?: WANT_TO_READ_LABEL
}

private const val WANT_TO_READ_LABEL = "Want to Read"
private const val OWNED_LABEL = "Owned"

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
