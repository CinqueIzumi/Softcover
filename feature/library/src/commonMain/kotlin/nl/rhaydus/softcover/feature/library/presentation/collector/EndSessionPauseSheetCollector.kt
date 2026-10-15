package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.core.uibinding.session.endSessionPauseSheetScaffold
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class EndSessionPauseSheetCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state ->
                val title = state.activeSessionBookId?.let { id ->
                    state.resolveSelectedBooks(bookIds = setOf(id)).firstOrNull()?.title
                }

                Pair(
                    state.endSessionPauseBookIds.isNotEmpty(),
                    title,
                )
            }
            .distinctUntilChanged()
            .collectLatest { (requested, title) ->
                scope.setState {
                    it.copy(
                        endSessionPauseSheet = if (requested && it.endSessionPauseBookIds.isNotEmpty()) {
                            it.endSessionPauseSheet.openOrUpdate(
                                model = endSessionPauseSheetScaffold(bookTitle = title),
                            )
                        } else {
                            it.endSessionPauseSheet.requestClose()
                        },
                    )
                }
            }
    }
}
