package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.uibinding.lists.toBulkChooseListsUiModel
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/** How many covers the bulk header's rotated jacket stack shows. */
private const val STACKED_COVER_COUNT: Int = 3

/**
 * Maps the bulk selection and the reader's custom lists to the choose-lists sheet off the
 * composition (R9), and resolves the header stack's covers in the same pass.
 *
 * Both fields are written in one `setState` so the model's jacket count and the editions the render
 * indexes into can never describe different selections.
 */
internal class ChooseListsCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state ->
                ChooseListsSnapshot(
                    selectedBookIds = state.selectedBookIds,
                    customLists = state.customLists,
                    listsBeingMutated = state.listsBeingMutated,
                    jacketEditions = state.resolveSelectedBooks()
                        .take(STACKED_COVER_COUNT)
                        .mapNotNull { it.currentEdition },
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                // Nothing selected means the sheet cannot be open, so there is no model to build.
                // Selection mode itself is what prepares the model, so it is always ready by the
                // time the sheet is raised.
                val model = if (snapshot.selectedBookIds.isEmpty()) {
                    null
                } else {
                    snapshot.customLists
                        .filter { it.isOwned.not() }
                        .toBulkChooseListsUiModel(
                            bookIds = snapshot.selectedBookIds,
                            coverCount = snapshot.jacketEditions.size,
                            listsBeingMutated = snapshot.listsBeingMutated,
                        )
                }

                scope.setState {
                    it.copy(
                        chooseListsSheet = model,
                        chooseListsJacketEditions = snapshot.jacketEditions,
                    )
                }
            }
    }
}
