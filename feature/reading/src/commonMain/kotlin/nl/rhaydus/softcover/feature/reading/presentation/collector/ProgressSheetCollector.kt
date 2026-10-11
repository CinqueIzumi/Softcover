package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.core.uibinding.progress.toProgressSheetUiModel
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the book the sheet is open for, plus the reader's stored progress unit, to the sheet's UI
 * model off the composition.
 *
 * A collector rather than a patch to each action: five actions set or clear `bookToUpdate`, and the
 * three that clear it on a successful update would each have had to remember to clear the derived
 * model too. Deriving it here means the open `progressSheet` always shows the current `bookToUpdate`.
 */
internal class ProgressSheetCollector : ReadingCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
        dependencies: ReadingScreenDependencies,
    ) {
        scope.state
            .map {
                ProgressSheetSnapshot(
                    bookToUpdate = it.bookToUpdate,
                    selectedTab = it.progressSheetTab,
                    timeDraft = it.progressTimeDraft,
                    readAt = it.readAt,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val book = snapshot.bookToUpdate

                scope.setState {
                    it.copy(
                        progressSheet = if (book == null || it.bookToUpdate == null) {
                            it.progressSheet.requestClose()
                        } else {
                            it.progressSheet.openOrUpdate(
                                model = book.toProgressSheetUiModel(
                                    selectedTab = snapshot.selectedTab,
                                    timeDraft = snapshot.timeDraft,
                                    readAt = snapshot.readAt,
                                ),
                            )
                        },
                    )
                }
            }
    }
}
