package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.share.ShareCardUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toBookShareCardUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toReadingUpdateShareCardUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardVariant
import nl.rhaydus.softcover.feature.book_detail.presentation.state.shareSheetFor
import nl.rhaydus.toad.ActionScope

/**
 * Maps the book, the currently-displayed edition, the reader's identity and the chosen variant to the
 * share sheet.
 *
 * The reading-update card is offered only when the reader has something personal to share; without it
 * the book card is the only variant and no toggle is shown.
 */
internal class ShareCardsCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map {
                ShareCardsSnapshot(
                    book = it.book,
                    edition = it.displayedEdition,
                    username = it.currentUsername,
                    avatarUrl = it.currentUserAvatarUrl,
                    userTags = it.userTags,
                    chosenVariant = it.chosenShareVariant,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val book = snapshot.book

                if (book == null) {
                    scope.setState {
                        it.copy(shareSheet = null)
                    }

                    return@collectLatest
                }

                val updateCard = book.toReadingUpdateShareCardUiModel(
                    edition = snapshot.edition,
                    username = snapshot.username,
                    avatarUrl = snapshot.avatarUrl,
                    userTags = snapshot.userTags,
                )

                val variant = if (updateCard != null) {
                    snapshot.chosenVariant ?: ShareCardVariant.MY_UPDATE
                } else {
                    ShareCardVariant.BOOK
                }

                val card: ShareCardUiModel = updateCard.takeIf { variant == ShareCardVariant.MY_UPDATE }
                    ?: book.toBookShareCardUiModel(edition = snapshot.edition)

                scope.setState {
                    it.copy(
                        shareSheet = shareSheetFor(
                            card = card,
                            bookTitle = book.title,
                            variant = variant,
                            hasUpdateCard = updateCard != null,
                            progress = it.shareCapture.progress,
                            closeRequested = it.shareCapture.closeRequested,
                        ),
                    )
                }
            }
    }
}
