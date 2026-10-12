package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.header.SectionHeaderTone
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class ShelveControlHeaderCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { it.book?.id to it.failedMutationBookIds }
            .distinctUntilChanged()
            .collectLatest { (bookId, failedMutationBookIds) ->
                val mutationFailed = bookId != null && bookId in failedMutationBookIds

                val header = if (mutationFailed) {
                    SectionHeaderUiModel.Inline(
                        eyebrow = "Couldn't save. Tap to retry",
                        tone = SectionHeaderTone.Error,
                    )
                } else {
                    SectionHeaderUiModel.Inline(eyebrow = "Shelve this book")
                }

                scope.setState { state -> state.copy(shelveControlHeader = header) }
            }
    }
}
