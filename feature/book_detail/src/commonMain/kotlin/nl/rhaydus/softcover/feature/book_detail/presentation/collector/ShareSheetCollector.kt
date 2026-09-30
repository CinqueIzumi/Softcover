package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardVariant
import nl.rhaydus.softcover.feature.book_detail.presentation.state.shareSheetBodyFor
import nl.rhaydus.softcover.feature.book_detail.presentation.state.shareSheetScaffoldFor
import nl.rhaydus.toad.ActionScope

internal class ShareSheetCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map {
                ShareSheetSnapshot(
                    bookTitle = it.shareBookCard?.title,
                    hasUpdateCard = it.shareUpdateCard != null,
                    chosenVariant = it.chosenShareVariant,
                    isSharing = it.isSharing,
                    isSavingToGallery = it.isSavingToGallery,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val variant = if (snapshot.hasUpdateCard) {
                    snapshot.chosenVariant ?: ShareCardVariant.MY_UPDATE
                } else {
                    ShareCardVariant.BOOK
                }
                val title = snapshot.bookTitle

                scope.setState {
                    if (title == null) {
                        it.copy(
                            shareSheet = null,
                            shareSheetBody = null,
                        )
                    } else {
                        it.copy(
                            shareSheet = shareSheetScaffoldFor(
                                bookTitle = title,
                                variant = variant,
                            ),
                            shareSheetBody = shareSheetBodyFor(
                                variant = variant,
                                hasUpdateCard = snapshot.hasUpdateCard,
                                isSharing = snapshot.isSharing,
                                isSavingToGallery = snapshot.isSavingToGallery,
                            ),
                        )
                    }
                }
            }
    }

    private data class ShareSheetSnapshot(
        val bookTitle: String?,
        val hasUpdateCard: Boolean,
        val chosenVariant: ShareCardVariant?,
        val isSharing: Boolean,
        val isSavingToGallery: Boolean,
    )
}
