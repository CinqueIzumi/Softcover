package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.TagSource
import nl.rhaydus.toad.ActionScope

internal class OnFilterTagCategoryPickedAction(
    private val source: TagSource,
    private val key: String,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState { state ->
            val draft = state.filterDraft ?: return@setState state
            val category = TagCategory.entries.firstOrNull { it.name == key }

            state.copy(
                filterDraft = draft.withSection(
                    source = source,
                    section = draft.sectionFor(source = source).copy(category = category),
                ),
            )
        }
    }
}
