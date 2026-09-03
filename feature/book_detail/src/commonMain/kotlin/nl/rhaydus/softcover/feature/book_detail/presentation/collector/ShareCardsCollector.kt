package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toBookShareCardUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toReadingUpdateShareCardUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the book, the currently-displayed edition, and the reader's identity to the two share cards
 * off the composition (R9), so `ShareBookBottomSheet` only ever forwards
 * `BookDetailUiState.shareBookCard` / `BookDetailUiState.shareUpdateCard`.
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
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState {
                    it.copy(
                        shareBookCard = snapshot.book?.toBookShareCardUiModel(edition = snapshot.edition),
                        shareUpdateCard = snapshot.book?.toReadingUpdateShareCardUiModel(
                            edition = snapshot.edition,
                            username = snapshot.username,
                            avatarUrl = snapshot.avatarUrl,
                            userTags = snapshot.userTags,
                        ),
                    )
                }
            }
    }
}
