package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.state.EDITABLE_CATEGORIES

/**
 * The canonical surface for managing the user's own tags on a book (§3.5 modal sheet). The user picks
 * a category and names a tag — there is no search step, as the upsert reuses an existing tag of that
 * name or mints a new one server-side. Tags already on the book render below, grouped by category like
 * a contents page, each removable and with a per-tag spoiler flag. Every change is committed immediately
 * by the caller — the sheet is a dumb renderer that reports intents through its callbacks. The book
 * title and the header's mini jacket [CoverUiModel] are render-only inputs, already mapped off the
 * composition by the caller's `CoverModelsCollector` (R9); the TOAD contract carries neither, both
 * live on the caller's `BookDetailUiState`.
 *
 * Only the top bar and header are pinned; the add block and the collection share **one** scroll
 * region below them. That is what lets the suggestion cloud reveal the user's entire vocabulary for
 * a category in a single tap: an unbounded cloud in a pinned block would squeeze the collection's
 * `weight(1f)` to nothing and clip the sheet, whereas here it simply lengthens the page. Keeping the
 * naming field in the same scroll region as its suggestions also keeps the two adjacent, which a
 * pinned field with a scrolling cloud would not.
 */

/** A tag's identity for grouping / keying / new-entry tracking — `UserTag` carries no stable id. */
internal typealias TagKey = Pair<TagCategory, String>

internal fun UserTag.asKey(): TagKey = category to name

/**
 * Tags grouped by category in the fixed Genre → Mood → Tag → Content warning order; empty groups are
 * omitted. Any tag whose category isn't one of those four (the API can hand back `OTHER` for a null or
 * unrecognized server category) folds into a trailing "Other" group instead of disappearing — the old
 * sheet rendered every tag unconditionally, and this keeps that total coverage.
 */
private fun List<UserTag>.groupedForCollection(): List<TagGroup> {
    val editableGroups = EDITABLE_CATEGORIES.mapNotNull { category ->
        val tagsInCategory = filter { it.category == category }

        tagsInCategory.takeIf { it.isNotEmpty() }?.let {
            TagGroup(
                category = category,
                tags = it,
            )
        }
    }

    val otherTags = filter { it.category !in EDITABLE_CATEGORIES }

    return if (otherTags.isEmpty()) {
        editableGroups
    } else {
        editableGroups + TagGroup(
            category = TagCategory.OTHER,
            tags = otherTags,
        )
    }
}

@Composable
internal fun TagEditorBottomSheet(
    bookTitle: String,
    cover: CoverUiModel?,
    userTags: List<UserTag>,
    categoryChips: List<ChipUiModel>,
    suggestionChips: List<ChipUiModel>,
    suggestionByChipKey: Map<String, UserTag>,
    selectedCategory: TagCategory,
    draft: String,
    onCategorySelected: (TagCategory) -> Unit,
    onDraftChange: (String) -> Unit,
    onAddTag: (String, TagCategory) -> Unit,
    onSuggestionSelected: (UserTag) -> Unit,
    onRemoveTag: (UserTag) -> Unit,
    onToggleSpoiler: (UserTag) -> Unit,
    onDismissRequest: () -> Unit,
) {
    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        val dismiss = LocalModalSheetDismiss.current
        val groups = userTags.groupedForCollection()
        val newlyAddedKeys = rememberNewlyAddedTagKeys(tags = userTags)
        val commitDraft = {
            onAddTag(
                draft,
                selectedCategory,
            )
        }

        val onCategoryChipEvent: (ChipEvent) -> Unit = { event ->
            if (event is ChipEvent.Clicked) onCategorySelected(TagCategory.fromName(event.key))
        }

        val onSuggestionChipEvent: (ChipEvent) -> Unit = { event ->
            if (event is ChipEvent.Clicked) {
                suggestionByChipKey[event.key]?.let(onSuggestionSelected)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
        ) {
            TagEditorTopBar(onDoneClick = dismiss)

            TagEditorHeader(
                bookTitle = bookTitle,
                cover = cover,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(state = rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            ) {
                TagEditorAddBlock(
                    selectedCategory = selectedCategory,
                    categoryChips = categoryChips,
                    draft = draft,
                    suggestionChips = suggestionChips,
                    onCategoryChipEvent = onCategoryChipEvent,
                    onDraftChange = onDraftChange,
                    onCommit = commitDraft,
                    onSuggestionChipEvent = onSuggestionChipEvent,
                    modifier = Modifier.fillMaxWidth(),
                )

                TagEditorCollection(
                    groups = groups,
                    newlyAddedKeys = newlyAddedKeys,
                    onToggleSpoiler = onToggleSpoiler,
                    onRemove = onRemoveTag,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }
        }
    }
}

/**
 * Tracks which tag keys were newly inserted into [tags] since the previous composition, so the just-added
 * chip can play its entry animation while the rest of the collection stays static. The snapshot is only
 * compared once a prior snapshot exists, so the sheet's initial tags never animate in on open — only a
 * tag added during this sheet's lifetime does. Mirrors the foundation `LazyItemMutationAnimator`'s
 * snapshot-via-`SideEffect` idiom, adapted for a plain `FlowRow` rather than a lazy list.
 */
@Composable
private fun rememberNewlyAddedTagKeys(tags: List<UserTag>): Set<TagKey> {
    val playMotion = playDecorativeMotion()
    val currentKeys = tags.map { it.asKey() }.toSet()
    val previousKeys = remember { mutableStateOf<Set<TagKey>?>(null) }

    val newlyAdded = previousKeys.value?.let { previous ->
        if (playMotion) currentKeys - previous else emptySet()
    }.orEmpty()

    SideEffect { previousKeys.value = currentKeys }

    return newlyAdded
}
