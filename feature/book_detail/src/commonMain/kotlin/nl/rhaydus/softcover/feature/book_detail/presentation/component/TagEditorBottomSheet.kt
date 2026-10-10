package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.book_detail.presentation.state.TagEditorSheetEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.state.TagEditorSheetUiModel

/**
 * The canonical surface for managing the user's own tags on a book (§3.5 modal sheet). The user picks
 * a category and names a tag — there is no search step, as the upsert reuses an existing tag of that
 * name or mints a new one server-side. Tags already on the book render below, grouped by category like
 * a contents page, each removable and with a per-tag spoiler flag. Every change is committed immediately
 * by the caller.
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
    model: TagEditorSheetUiModel,
    onEvent: (TagEditorSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val onNamingFieldEvent: (TextFieldEvent) -> Unit = { event ->
        when (event) {
            is TextFieldEvent.ValueChanged -> onEvent(TagEditorSheetEvent.DraftChanged(text = event.text))

            is TextFieldEvent.Submitted,
            is TextFieldEvent.TrailingClicked -> onEvent(TagEditorSheetEvent.DraftSubmitted)

            is TextFieldEvent.ShakeEnded -> Unit
        }
    }

    val onCategoryChipEvent: (ChipEvent) -> Unit = { event ->
        if (event is ChipEvent.Clicked) onEvent(TagEditorSheetEvent.CategorySelected(categoryKey = event.key))
    }

    val onSuggestionChipEvent: (ChipEvent) -> Unit = { event ->
        if (event is ChipEvent.Clicked) onEvent(TagEditorSheetEvent.SuggestionSelected(chipKey = event.key))
    }

    val onTagChipEvent: (ChipEvent) -> Unit = { event ->
        when (event) {
            is ChipEvent.Dismissed -> onEvent(TagEditorSheetEvent.TagRemoved(chipKey = event.key))

            is ChipEvent.SpoilerToggled -> onEvent(TagEditorSheetEvent.TagSpoilerToggled(chipKey = event.key))

            is ChipEvent.Clicked -> Unit
        }
    }

    SheetScaffold(
        model = model.scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.Dismissed -> onEvent(TagEditorSheetEvent.Dismissed)

                SheetScaffoldEvent.PrimaryClicked,
                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
        modifier = modifier,
    ) {
        Column {
            TagEditorAddBlock(
                selectedCategoryKey = model.selectedCategoryKey,
                categoryChips = model.categoryChips,
                namingField = model.namingField,
                suggestionChips = model.suggestionChips,
                onCategoryChipEvent = onCategoryChipEvent,
                onNamingFieldEvent = onNamingFieldEvent,
                onSuggestionChipEvent = onSuggestionChipEvent,
                modifier = Modifier.fillMaxWidth(),
            )

            TagEditorCollection(
                groups = model.groups,
                emptyState = model.emptyState,
                newlyAddedTagKey = model.newlyAddedTagKey,
                onEvent = onTagChipEvent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xs),
            )
        }
    }
}
