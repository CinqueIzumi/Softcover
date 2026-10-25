package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.TagSource
import nl.rhaydus.softcover.feature.library.presentation.state.withSearchText
import nl.rhaydus.toad.ActionScope

/**
 * A tag section's search field. Writes the text into the draft and straight into the built field too,
 * so the caret never waits on `FilterDraftChipsCollector`'s next pass. A blank search drops the
 * section's category as well.
 */
internal class OnFilterTagSearchChangedAction(
    private val source: TagSource,
    private val query: String,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState { state ->
            val draft = state.filterDraft ?: return@setState state
            val section = draft.sectionFor(source = source)
            val selection = state.filterSheet.selection

            state.copy(
                filterDraft = draft.withSection(
                    source = source,
                    section = section.copy(
                        search = query,
                        category = if (query.isBlank()) null else section.category,
                    ),
                ),
                filterSheet = state.filterSheet.copy(
                    selection = selection?.withSearchText(
                        source = source,
                        text = query,
                    ),
                ),
            )
        }
    }
}
