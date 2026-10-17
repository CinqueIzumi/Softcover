package nl.rhaydus.softcover.feature.widget.presentation.mapper

import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickCadence
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSource

internal fun sourceChipsFor(
    selected: RandomPickSource,
    lists: List<BookList>,
): ChipSet<RandomPickSource> {
    val choices = buildList {
        add(WANT_TO_READ_LABEL to RandomPickSource.WantToRead)

        if (lists.any { it.isOwned }) add(OWNED_LABEL to RandomPickSource.Owned)

        lists
            .filterNot { it.isOwned }
            .sortedBy { it.name.lowercase() }
            .forEach { add(it.name to RandomPickSource.UserList(listId = it.id)) }
    }

    return choices.toChipSet(selected = selected)
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
