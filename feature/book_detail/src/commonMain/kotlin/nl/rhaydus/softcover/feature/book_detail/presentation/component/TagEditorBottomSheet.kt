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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.state.UserTagEditorChipGroup

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

@Composable
internal fun TagEditorBottomSheet(
    bookTitle: String,
    cover: CoverUiModel?,
    userTagEditorGroups: List<UserTagEditorChipGroup>,
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
    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        val dismiss = LocalModalSheetDismiss.current
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
                    suggestionChips = suggestions.chips,
                    onCategoryChipEvent = onCategoryChipEvent,
                    onDraftChange = onDraftChange,
                    onCommit = commitDraft,
                    onSuggestionChipEvent = onSuggestionChipEvent,
                    modifier = Modifier.fillMaxWidth(),
                )

                TagEditorCollection(
                    groups = userTagEditorGroups,
                    newlyAddedTagKey = newlyAddedTagKey,
                    onEvent = onTagChipEvent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }
        }
    }
}
