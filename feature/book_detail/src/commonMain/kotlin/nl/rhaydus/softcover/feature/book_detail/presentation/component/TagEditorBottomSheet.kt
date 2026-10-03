package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.state.UserTagEditorChipGroup

/**
 * The canonical surface for managing the user's own tags on a book (§3.5 modal sheet). The user picks
 * a category and names a tag — there is no search step, as the upsert reuses an existing tag of that
 * name or mints a new one server-side. Tags already on the book render below, grouped by category like
 * a contents page, each removable and with a per-tag spoiler flag. Every change is committed immediately
 * by the caller — the sheet is a dumb renderer that reports intents through its callbacks.
 *
 * Only the top action and header are pinned; the add block and the collection share **one** scroll
 * region below them. That is what lets the suggestion cloud reveal the user's entire vocabulary for
 * a category in a single tap: an unbounded cloud in a pinned block would squeeze the collection to
 * nothing and clip the sheet, whereas here it simply lengthens the page. Keeping the naming field in
 * the same scroll region as its suggestions also keeps the two adjacent, which a pinned field with a
 * scrolling cloud would not.
 */
@Composable
internal fun TagEditorBottomSheet(
    model: SheetScaffoldUiModel,
    userTagEditorGroups: List<UserTagEditorChipGroup>,
    emptyState: EmptyStateUiModel,
    newlyAddedTagKey: String?,
    categoryChips: List<ChipUiModel>,
    suggestions: ChipSet<UserTag>,
    selectedCategory: TagCategory,
    draft: String,
    onCategorySelected: (TagCategory) -> Unit,
    onDraftChange: (String) -> Unit,
    onAddTag: (String, TagCategory) -> Unit,
    onSuggestionSelected: (UserTag) -> Unit,
    onTagChipEvent: (ChipEvent) -> Unit,
    onDismissRequest: () -> Unit,
) {
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
            suggestions[event.key]?.let(onSuggestionSelected)
        }
    }

    SheetScaffold(
        model = model,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.Dismissed -> onDismissRequest()

                SheetScaffoldEvent.PrimaryClicked,
                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
    ) {
        Column {
            TagEditorAddBlock(
                selectedCategory = selectedCategory,
                categoryChips = categoryChips,
                draft = draft,
                suggestionChips = suggestions.chips,
                onCategoryChipEvent = onCategoryChipEvent,
                onDraftChange = onDraftChange,
                onCommit = commitDraft,
                onSuggestionChipEvent = onSuggestionChipEvent,
                modifier = Modifier.fillMaxWidth(),
            )

            TagEditorCollection(
                groups = userTagEditorGroups,
                emptyState = emptyState,
                newlyAddedTagKey = newlyAddedTagKey,
                onEvent = onTagChipEvent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }
    }
}
