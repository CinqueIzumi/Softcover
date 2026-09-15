package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.TagCategoryChipGroup
import nl.rhaydus.toad.ActionScope

/**
 * Maps the book page's two read-only tag surfaces to [ChipUiModel]s off the composition
 * (`component-contract.md` § 7.2 R9): [BookDetailUiState.userTagChips] ("Your tags", `UserTagsSection`)
 * and [BookDetailUiState.communityTagGroups] (the community tag block, `TagsSection`, grouped by
 * category, top-5 per category, content-warning tags flagged [ChipUiModel.concealed]). Both are inert
 * (`clickable = false`) — neither `PillChip` call site they replace takes an `onClick`; the section's
 * "+ Add tags" / "Edit tags" affordance is a separate, non-chip component.
 *
 * The top-5-per-category cap and category order mirror `TagsSection`'s own `remember(tags)` block
 * exactly, moved here so the grouping/sorting work runs once per [BookDetailUiState.book] change
 * rather than being recomputed (memoized, but still composition-bound) on every recomposition.
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
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val userTagChips = snapshot.userTags.map { it.toChipUiModel() }
                val communityTagGroups = snapshot.communityTags.toTagCategoryChipGroups()

                scope.setState {
                    it.copy(
                        userTagChips = userTagChips,
                        communityTagGroups = communityTagGroups,
                    )
                }
            }
    }
}

private val COMMUNITY_TAG_CATEGORIES: List<TagCategory> = listOf(
    TagCategory.GENRE,
    TagCategory.MOOD,
    TagCategory.CONTENT_WARNING,
)

private const val COMMUNITY_TAGS_PER_CATEGORY = 5

private fun UserTag.toChipUiModel(): ChipUiModel = ChipUiModel(
    key = "${category.name}:$name",
    label = name,
    clickable = false,
)

private fun List<Tag>.toTagCategoryChipGroups(): List<TagCategoryChipGroup> =
    COMMUNITY_TAG_CATEGORIES.mapNotNull { category ->
        val topTags = filter { it.category == category }
            .sortedByDescending { it.count }
            .take(COMMUNITY_TAGS_PER_CATEGORY)

        if (topTags.isEmpty()) return@mapNotNull null

        TagCategoryChipGroup(
            category = category,
            chips = topTags.map { tag ->
                ChipUiModel(
                    key = tag.id.toString(),
                    label = tag.name,
                    concealed = category == TagCategory.CONTENT_WARNING,
                    clickable = false,
                )
            },
        )
    }
