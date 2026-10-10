package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.TagEditorSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.UserTagEditorChipGroup
import nl.rhaydus.softcover.feature.book_detail.presentation.state.tagEditorSheetScaffoldFor
import nl.rhaydus.toad.ActionScope

internal class TagEditorSheetCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map {
                TagEditorSheetSnapshot(
                    bookTitle = it.book?.title,
                    cover = it.tagEditorCover,
                    categoryChips = it.tagEditorCategoryChips,
                    selectedCategoryKey = it.tagEditorCategory.name,
                    namingField = it.tagNamingField,
                    suggestionChips = it.tagSuggestionChips.chips,
                    groups = it.userTagEditorGroups,
                    emptyState = it.tagEditorEmptyState,
                    newlyAddedTagKey = it.newlyAddedTagKey,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState {
                    it.copy(
                        tagEditorSheet = snapshot.bookTitle?.let { title ->
                            TagEditorSheetUiModel(
                                scaffold = tagEditorSheetScaffoldFor(
                                    bookTitle = title,
                                    cover = snapshot.cover,
                                ),
                                categoryChips = snapshot.categoryChips,
                                selectedCategoryKey = snapshot.selectedCategoryKey,
                                namingField = snapshot.namingField,
                                suggestionChips = snapshot.suggestionChips,
                                groups = snapshot.groups,
                                emptyState = snapshot.emptyState,
                                newlyAddedTagKey = snapshot.newlyAddedTagKey,
                            )
                        },
                    )
                }
            }
    }

    private data class TagEditorSheetSnapshot(
        val bookTitle: String?,
        val cover: CoverUiModel?,
        val categoryChips: ImmutableList<ChipUiModel>,
        val selectedCategoryKey: String,
        val namingField: TextFieldUiModel,
        val suggestionChips: ImmutableList<ChipUiModel>,
        val groups: ImmutableList<UserTagEditorChipGroup>,
        val emptyState: EmptyStateUiModel,
        val newlyAddedTagKey: String?,
    )
}
