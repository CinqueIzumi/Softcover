package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.ArrangeSheetSnapshot
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.arrangeSheetScaffoldFor
import nl.rhaydus.toad.ActionScope

/**
 * The footer's result count follows the selected shelf's display list, falling back to its stats
 * until that list has landed.
 */
internal class ArrangeSheetCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state -> ArrangeSheetSnapshot.from(state = state) }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState {
                    it.copy(
                        arrangeSheet = arrangeSheetScaffoldFor(resultCount = snapshot.resultCount),
                        arrangeTitlesToggle = snapshot.titlesToggle,
                        arrangeMakeOrderedListId = snapshot.makeOrderedListId,
                    )
                }
            }
    }
}
