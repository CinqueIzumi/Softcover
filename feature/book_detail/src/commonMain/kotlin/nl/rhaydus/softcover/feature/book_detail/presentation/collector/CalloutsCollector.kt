package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.scanEditionCallout
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toStatusCallout
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class CalloutsCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { state ->
                CalloutInputs(
                    book = state.book,
                    dateStyle = state.dateStyle,
                    scannedEditionId = state.scannedEditionId,
                    scannedEditionBannerDismissed = state.scannedEditionBannerDismissed,
                    isUpdatingScannedEdition = state.isUpdatingScannedEdition,
                )
            }
            .distinctUntilChanged()
            .collectLatest { inputs ->
                val statusCallout = inputs.book?.toStatusCallout(dateStyle = inputs.dateStyle)

                val scanEditionCallout = scanEditionCallout(
                    scannedEditionId = inputs.scannedEditionId,
                    shelvedEditionId = inputs.book?.userBook?.editionId,
                    dismissed = inputs.scannedEditionBannerDismissed,
                    isUpdating = inputs.isUpdatingScannedEdition,
                )

                val shelveReadDate = inputs.book?.userBook?.getReadDateString(style = inputs.dateStyle)

                scope.setState { state ->
                    state.copy(
                        statusCallout = statusCallout,
                        shelveReadDate = shelveReadDate,
                        scanEditionCallout = scanEditionCallout,
                    )
                }
            }
    }
}
