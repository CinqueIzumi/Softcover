package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.tagEditorSheetScaffoldFor
import nl.rhaydus.toad.ActionScope

internal class TagEditorSheetCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map {
                TagEditorSheetSnapshot(
                    bookTitle = it.book?.title,
                    cover = it.tagEditorCover,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState {
                    it.copy(
                        tagEditorSheet = snapshot.bookTitle?.let { title ->
                            tagEditorSheetScaffoldFor(
                                bookTitle = title,
                                cover = snapshot.cover,
                            )
                        },
                    )
                }
            }
    }

    private data class TagEditorSheetSnapshot(
        val bookTitle: String?,
        val cover: CoverUiModel?,
    )
}
