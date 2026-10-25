package nl.rhaydus.softcover.feature.library.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCardTagStrip
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipSize
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.domain.model.UserTag

internal fun List<UserTag>.toTagStrip(): BookCardTagStrip? {
    if (isEmpty()) return null

    return BookCardTagStrip(
        chips = map { tag ->
            ChipUiModel(
                key = "${tag.category.name}:${tag.name}",
                label = tag.name,
                size = ChipSize.Compact,
                interaction = ChipInteraction.Inert,
            )
        }.toImmutableList(),
        overflowChips = indices.map { index ->
            ChipUiModel(
                key = "more-${index + 1}",
                label = "+${index + 1}",
                tone = ChipTone.Outlined,
                size = ChipSize.Compact,
                interaction = ChipInteraction.Inert,
            )
        }.toImmutableList(),
        contentDescription = "Your tags: ${joinToString(separator = ", ") { it.name }}",
    )
}
