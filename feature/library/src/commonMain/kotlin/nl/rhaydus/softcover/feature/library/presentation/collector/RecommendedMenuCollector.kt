package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.mapper.recommendedMenuSheetFor
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class RecommendedMenuCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state ->
                RecommendedMenuSnapshot(
                    book = state.recommendedBooks.find { it.id == state.recommendedMenuBookId },
                    eyebrow = state.recommendedHeader.eyebrow,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val book = snapshot.book

                scope.setState {
                    it.copy(
                        recommendedMenu = if (book == null) {
                            it.recommendedMenu.requestClose()
                        } else {
                            it.recommendedMenu.openOrUpdate(
                                model = recommendedMenuSheetFor(
                                    book = book,
                                    eyebrow = snapshot.eyebrow,
                                ),
                            )
                        },
                    )
                }
            }
    }
}
