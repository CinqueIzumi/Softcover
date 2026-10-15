package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.TagCategory

internal const val SHARE_TAG_PICKER_KEY = "share-tag-picker"

internal fun shareTagPickerFor(
    variant: ShareCardVariant,
    candidates: Map<TagCategory, List<String>>,
    selected: Set<String>,
    expanded: Boolean,
): ShareTagPickerUiModel? {
    if (candidates.isEmpty()) return null

    val selectedCount = candidates.values.flatten().distinct().count { it in selected }

    return ShareTagPickerUiModel(
        variant = variant,
        header = ChipUiModel(
            key = SHARE_TAG_PICKER_KEY,
            label = if (selectedCount == 0) "Tags" else "Tags · $selectedCount",
            tone = ChipTone.Choice,
            trailing = ChipTrailing.Disclosure(
                expanded = expanded,
                label = if (expanded) "Hide tags" else "Show tags",
            ),
        ),
        expanded = expanded,
        groups = candidates
            .map { (category, names) ->
                ShareTagGroup(
                    header = SectionHeaderUiModel.Label(eyebrow = category.label),
                    tags = names
                        .map { name ->
                            ChipUiModel(
                                key = name,
                                label = name,
                                selected = name in selected,
                            ) to name
                        }
                        .toChipSet(),
                )
            }
            .toImmutableList(),
    )
}
