package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipLeading
import nl.rhaydus.softcover.core.component.chip.ChipSize
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.EDITABLE_CATEGORIES
import nl.rhaydus.softcover.feature.book_detail.presentation.state.TagCategoryChipGroup
import nl.rhaydus.softcover.feature.book_detail.presentation.state.UserTagEditorChipGroup
import nl.rhaydus.toad.ActionScope

/**
 * Maps every tag surface on the book page to [ChipUiModel]s off the composition
 * (`component-contract.md` § 7.2 R9): [BookDetailUiState.userTagChips] ("Your tags", read-only,
 * `UserTagsSection`), [BookDetailUiState.communityTagGroups] (the community tag block, `TagsSection`,
 * grouped by category, top-5 per category, content-warning tags [ChipTone.Spoiler] until their key
 * reaches [BookDetailUiState.revealedTagKeys]), [BookDetailUiState.tagEditorOpenerChip] (the
 * "Add tags" / "Edit tags" opener) and [BookDetailUiState.userTagEditorGroups] (the tag editor's own
 * collection, `TagEditorCollection`).
 */
internal class TagChipModelsCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { state ->
                TagChipModelsSnapshot(
                    userTags = state.userTags,
                    communityTags = state.book?.tags.orEmpty(),
                    revealedTagKeys = state.revealedTagKeys,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val userTagChips = snapshot.userTags.map { it.toChipUiModel() }
                val communityTagGroups = snapshot.communityTags.toTagCategoryChipGroups(
                    revealedTagKeys = snapshot.revealedTagKeys,
                )

                scope.setState {
                    it.copy(
                        userTagChips = userTagChips.toImmutableList(),
                        communityTagGroups = communityTagGroups.toImmutableList(),
                        tagEditorOpenerChip = tagEditorOpenerChip(hasTags = snapshot.userTags.isNotEmpty()),
                        userTagEditorGroups = snapshot.userTags.toUserTagEditorChipGroups(),
                        userTagByEditorChipKey = snapshot.userTags.associateBy { it.chipKey }.toImmutableMap(),
                    )
                }
            }
    }
}

private val COMMUNITY_TAG_CATEGORIES: List<TagCategory> = listOf(
    TagCategory.GENRE,
    TagCategory.MOOD,
    TagCategory.TAG,
    TagCategory.CONTENT_WARNING,
)

private const val COMMUNITY_TAGS_PER_CATEGORY = 5

private val UserTag.chipKey: String
    get() = "${category.name}:$name"

private fun UserTag.toChipUiModel(): ChipUiModel = ChipUiModel(
    key = chipKey,
    label = name,
    interaction = ChipInteraction.Inert,
)

private fun UserTag.toEditorChipUiModel(): ChipUiModel = ChipUiModel(
    key = chipKey,
    label = name,
    interaction = ChipInteraction.Inert,
    size = ChipSize.Compact,
    leading = ChipLeading.SpoilerToggle(
        marked = spoiler,
        label = if (spoiler) "Marked as spoiler. Tap to unmark" else "Mark as spoiler",
    ),
    trailing = ChipTrailing.Dismiss(label = "Remove $name"),
)

private fun tagEditorOpenerChip(hasTags: Boolean): ChipUiModel = ChipUiModel(
    key = "tag-editor-opener",
    label = if (hasTags) "Edit tags" else "Add tags",
    tone = ChipTone.Dashed,
    leading = if (hasTags) null else ChipLeading.Icon(icon = SoftcoverIcon.Add),
)

private fun TagCategory.communityGroupLabel(): String = when (this) {
    TagCategory.TAG -> "Other tags"
    else -> label
}

private fun List<Tag>.toTagCategoryChipGroups(revealedTagKeys: Set<String>): List<TagCategoryChipGroup> =
    COMMUNITY_TAG_CATEGORIES.mapNotNull { category ->
        val topTags = filter { it.category == category }
            .sortedByDescending { it.count }
            .take(COMMUNITY_TAGS_PER_CATEGORY)

        if (topTags.isEmpty()) return@mapNotNull null

        TagCategoryChipGroup(
            label = category.communityGroupLabel(),
            chips = topTags.map { tag ->
                val key = tag.id.toString()
                val concealed = category == TagCategory.CONTENT_WARNING && key !in revealedTagKeys

                ChipUiModel(
                    key = key,
                    label = tag.name,
                    tone = if (concealed) ChipTone.Spoiler else ChipTone.Tonal,
                    interaction = if (concealed) ChipInteraction.Clickable else ChipInteraction.Inert,
                )
            }.toImmutableList(),
        )
    }

/** Mirrors `TagEditorBottomSheet`'s old `groupedForCollection()`, now emitting chips instead of [UserTag]s. */
private fun List<UserTag>.toUserTagEditorChipGroups(): ImmutableList<UserTagEditorChipGroup> {
    val editableGroups = EDITABLE_CATEGORIES.mapNotNull { category ->
        val tagsInCategory = filter { it.category == category }

        tagsInCategory.takeIf { it.isNotEmpty() }?.let {
            UserTagEditorChipGroup(
                label = category.label,
                chips = it.map { tag -> tag.toEditorChipUiModel() }.toImmutableList(),
            )
        }
    }

    val otherTags = filter { it.category !in EDITABLE_CATEGORIES }

    val otherGroup = otherTags.takeIf { it.isNotEmpty() }?.let {
        UserTagEditorChipGroup(
            label = TagCategory.OTHER.label,
            chips = it.map { tag -> tag.toEditorChipUiModel() }.toImmutableList(),
        )
    }

    return (editableGroups + listOfNotNull(otherGroup)).toImmutableList()
}
