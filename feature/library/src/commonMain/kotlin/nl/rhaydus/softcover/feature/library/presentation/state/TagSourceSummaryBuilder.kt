package nl.rhaydus.softcover.feature.library.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipLeading
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldPillSize
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.TagCategory

/** The "All" category chip's key; every other category chip is keyed by [TagCategory.name]. */
internal const val TAG_ALL_CATEGORY_KEY = "all"

private val TAG_SHOW_MORE_CHIP = ChipUiModel(
    key = "tag-show-more",
    label = "Show more",
    tone = ChipTone.Dashed,
    interaction = ChipInteraction.Clickable,
)

private val TAG_SHOW_LESS_CHIP = ChipUiModel(
    key = "tag-show-less",
    label = "Show less",
    tone = ChipTone.Dashed,
    interaction = ChipInteraction.Clickable,
)

internal fun buildTagSourceSummary(
    source: TagSource,
    options: LibraryFilterOptions,
    filters: LibraryFilters,
    section: TagSectionDraft,
): TagSourceSummary? {
    val entries = options.tagEntries(
        source = source,
        filters = filters,
    )

    if (entries.isEmpty()) return null

    val chosen = entries
        .filter { it.direction != null }
        .sortedBy { it.direction }

    val query = section.search.trim()

    val matches = entries
        .filter { it.direction == null }
        .filter { section.category == null || it.category == section.category }
        .filter { entry ->
            entry.name.contains(
                other = query,
                ignoreCase = true,
            )
        }
        .sortedWith(
            compareBy<TagEntry> { entry ->
                val prefixMatch = entry.name.startsWith(
                    prefix = query,
                    ignoreCase = true,
                )

                query.isNotEmpty() && prefixMatch.not()
            }
                .thenByDescending { it.count }
                .thenBy { it.name.lowercase() },
        )

    val emptyLine = when {
        matches.isNotEmpty() || entries.none { it.count > 0 } -> null
        query.isNotEmpty() -> "No tags match “$query”."
        section.category == null -> "No more tags to add."
        else -> "No tags in this category."
    }

    return TagSourceSummary(
        source = source,
        header = SectionHeaderUiModel.Label(eyebrow = source.title),
        chosenLabel = if (chosen.isEmpty()) null else "${chosen.size} chosen",
        gloss = source.gloss,
        chosenChips = chosen.map { it.toChosenChip() }.toImmutableList(),
        searchField = searchField(
            source = source,
            search = section.search,
        ),
        categoryChips = if (query.isEmpty()) {
            persistentListOf()
        } else {
            categoryChips(
                entries = entries,
                selected = section.category,
            )
        },
        matchHeader = if (query.isEmpty()) null else SectionHeaderUiModel.Label(eyebrow = "Matching “$query”"),
        tagChips = matches.map { it.toTagChip() }.toImmutableList(),
        showMoreChip = TAG_SHOW_MORE_CHIP,
        showLessChip = TAG_SHOW_LESS_CHIP,
        emptyLine = emptyLine,
    )
}

private fun searchField(
    source: TagSource,
    search: String,
): TextFieldUiModel = TextFieldUiModel(
    key = "tag-search-${source.name.lowercase()}",
    text = search,
    placeholder = source.searchPlaceholder,
    variant = TextFieldVariant.Pill(
        leadingIcon = SoftcoverIcon.Search,
        trailing = if (search.isEmpty()) {
            null
        } else {
            PillButtonUiModel(
                key = "tag-search-clear",
                content = PillButtonContent.IconOnly(
                    icon = SoftcoverIcon.Close,
                    description = "Clear search",
                ),
            )
        },
        size = TextFieldPillSize.Compact,
    ),
)

private fun categoryChips(
    entries: List<TagEntry>,
    selected: TagCategory?,
): ImmutableList<ChipUiModel> {
    val present = entries.mapNotNull { it.category }.toSet()

    val categories = TagCategory.entries.filter { it in present }

    return (listOf<TagCategory?>(null) + categories).map { category ->
        ChipUiModel(
            key = category?.name ?: TAG_ALL_CATEGORY_KEY,
            label = category?.label ?: "All",
            tone = ChipTone.Choice,
            selected = category == selected,
        )
    }.toImmutableList()
}

private fun TagEntry.toChosenChip(): ChipUiModel = when (direction) {
    TagDirection.EXCLUDE -> ChipUiModel(
        key = includeKey,
        label = name,
        contentDescription = "$name, left out. Tap to include",
        tone = ChipTone.Excluded,
        leading = ChipLeading.Icon(icon = SoftcoverIcon.Remove),
        trailing = ChipTrailing.Dismiss(label = "Remove $name"),
    )

    else -> ChipUiModel(
        key = excludeKey,
        label = name,
        contentDescription = "$name, included. Tap to leave out",
        selected = true,
        leading = ChipLeading.Icon(icon = SoftcoverIcon.Check),
        trailing = ChipTrailing.Dismiss(label = "Remove $name"),
    )
}

private fun TagEntry.toTagChip(): ChipUiModel {
    val noun = if (count == 1) "book" else "books"

    return ChipUiModel(
        key = includeKey,
        label = "$name · $count",
        contentDescription = "Include $name, $count $noun",
    )
}
