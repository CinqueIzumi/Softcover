package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel

/** [emptyState] shows when the book carries no tags; [newlyAddedTagKey] names the one chip that plays its entry animation. */
internal data class TagEditorSheetUiModel(
    val scaffold: SheetScaffoldUiModel,
    val categoryChips: ImmutableList<ChipUiModel>,
    val selectedCategoryKey: String,
    val namingField: TextFieldUiModel,
    val suggestionChips: ImmutableList<ChipUiModel>,
    val groups: ImmutableList<UserTagEditorChipGroup>,
    val emptyState: EmptyStateUiModel,
    val newlyAddedTagKey: String?,
)
