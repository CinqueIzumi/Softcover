package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterValue
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.toggle

class OnToggleFilterValueAction(
    private val tabId: String,
    private val value: LibraryFilterValue,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState { state ->
            val current = state.filtersFor(tabId = tabId)
            val next = current.toggle(value = value)

            val newFilters = if (next.isEmpty) {
                state.filtersByTab - tabId
            } else {
                state.filtersByTab + (tabId to next)
            }

            state.copy(filtersByTab = newFilters)
        }
    }
}
