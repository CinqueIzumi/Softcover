package nl.rhaydus.softcover.feature.widget.progress

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.core.uibinding.progress.toProgressSheetUiModel
import nl.rhaydus.toad.ActionScope

internal class ProgressSheetCollector : UpdateProgressCollector {
    override suspend fun onLaunch(
        scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>,
        dependencies: UpdateProgressDependencies,
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
