package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toHowItFeltSheet
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HowItFeltDraft
import nl.rhaydus.toad.ActionScope

internal class HowItFeltSheetCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        val snapshotFlow = scope.state
            .map {
                HowItFeltSheetSnapshot(
                    bookTitle = it.book?.title,
                    draft = it.howItFeltDraft,
                )
            }
            .distinctUntilChanged()

        combine(snapshotFlow, dependencies.observeOwnMoodWordsUseCase()) { snapshot, ownWords ->
            snapshot to ownWords
        }.collectLatest { (snapshot, ownWords) ->
            val title = snapshot.bookTitle
            val draft = snapshot.draft

            scope.setState { state ->
                state.copy(
                    howItFeltSheet = if (title == null || draft == null) {
                        state.howItFeltSheet.requestClose()
                    } else {
                        state.howItFeltSheet.openOrUpdate(
                            model = draft.toHowItFeltSheet(
                                bookTitle = title,
                                ownWords = ownWords,
                            ),
                        )
                    },
                )
            }
        }
    }
}

private data class HowItFeltSheetSnapshot(
    val bookTitle: String?,
    val draft: HowItFeltDraft?,
)
