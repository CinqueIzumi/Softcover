package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
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
            .map { state -> state.isChooseListsSheetRequested to state.chooseListsSnapshot() }
            .distinctUntilChanged()
            .collectLatest { (_, snapshot) ->
                scope.setState {
                    val sheet = it.chooseListsSheet
                    val model = snapshot.toUiModel(previous = sheet.modelOrNull.takeUnless { sheet.isClosing })

                    when {
                        it.isChooseListsSheetRequested.not() -> it.copy(chooseListsSheet = sheet.requestClose())
                        model == null -> it
                        else -> it.copy(chooseListsSheet = sheet.openOrUpdate(model = model))
                    }
                }
            }
    }
}
