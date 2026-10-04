package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderTrailing
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.core.uibinding.lists.toBulkChooseListsUiModel
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

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
                    jacketBooks = state.resolveSelectedBooks().take(SheetHeaderTrailing.JacketStack.MAX_COVERS),
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
                    val covers = snapshot.jacketBooks.map { book ->
                        book.toCoverUiModel(variant = CoverVariant.StackedJacket)
                    }

                    snapshot.customLists.toBulkChooseListsUiModel(
                        bookIds = snapshot.selectedBookIds,
                        covers = covers,
                        listsBeingMutated = snapshot.listsBeingMutated,
                    )
                }

                scope.setState { it.copy(chooseListsSheet = model) }
            }
    }
}
