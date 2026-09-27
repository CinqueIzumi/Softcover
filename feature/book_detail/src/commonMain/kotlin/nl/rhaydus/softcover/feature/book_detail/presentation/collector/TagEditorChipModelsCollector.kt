package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.EDITABLE_CATEGORIES
import nl.rhaydus.toad.ActionScope

/**
 * Maps the tag editor's category picker and suggestion cloud to [ChipUiModel]s off the composition
 * (`component-contract.md` § 7.2 R9).
 *
 * [EDITABLE_CATEGORIES] is the single copy `TagEditorBottomSheet` shares — see its own KDoc.
 *
 * [BookDetailUiState.tagSuggestionByChipKey] resolves a tapped suggestion chip back to the [UserTag]
 * `OnAddUserTagAction`'s call site needs, keyed the same way as [BookDetailUiState.userTagChips]
 * (`UserTag` has no id — the category + name pair is its identity, per its own KDoc).
 */
internal class TagEditorChipModelsCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { state ->
                TagEditorChipModelsSnapshot(
                    selectedCategory = state.tagEditorCategory,
                    suggestions = state.tagSuggestions,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val categoryChips = EDITABLE_CATEGORIES.map { category ->
                    ChipUiModel(
                        key = category.name,
                        label = category.label,
                        selected = category == snapshot.selectedCategory,
                    )
                }

                val suggestionChips = snapshot.suggestions.map { it.toChipUiModel() }
                val suggestionByKey = snapshot.suggestions.associateBy { it.chipKey }

                scope.setState {
                    it.copy(
                        tagEditorCategoryChips = categoryChips,
                        tagSuggestionChips = suggestionChips,
                        tagSuggestionByChipKey = suggestionByKey,
                    )
                }
            }
    }
}

private val UserTag.chipKey: String
    get() = "${category.name}:$name"

private fun UserTag.toChipUiModel(): ChipUiModel = ChipUiModel(
    key = chipKey,
    label = name,
)
