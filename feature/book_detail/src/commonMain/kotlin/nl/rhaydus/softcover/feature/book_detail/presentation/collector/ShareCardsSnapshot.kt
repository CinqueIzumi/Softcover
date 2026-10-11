package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import nl.rhaydus.softcover.core.component.share.ShareCardUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toBookShareCardUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toReadingUpdateShareCardUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardVariant
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.shareSheetFor

internal data class ShareCardsSnapshot(
    val book: Book?,
    val edition: BookEdition?,
    val username: String?,
    val avatarUrl: String?,
    val userTags: List<UserTag>,
    val chosenVariant: ShareCardVariant?,
)

internal fun BookDetailUiState.shareCardsSnapshot(): ShareCardsSnapshot = ShareCardsSnapshot(
    book = book.takeIf { shareSheetRequested },
    edition = displayedEdition,
    username = currentUsername,
    avatarUrl = currentUserAvatarUrl,
    userTags = userTags,
    chosenVariant = chosenShareVariant,
)

internal fun ShareCardsSnapshot.toShareSheetUiModel(progress: ShareCaptureProgress): ShareSheetUiModel? {
    val book = book ?: return null

    val updateCard = book.toReadingUpdateShareCardUiModel(
        edition = edition,
        username = username,
        avatarUrl = avatarUrl,
        userTags = userTags,
    )

    val variant = if (updateCard != null) {
        chosenVariant ?: ShareCardVariant.MY_UPDATE
    } else {
        ShareCardVariant.BOOK
    }

    val card: ShareCardUiModel = updateCard.takeIf { variant == ShareCardVariant.MY_UPDATE }
        ?: book.toBookShareCardUiModel(edition = edition)

    return shareSheetFor(
        card = card,
        bookTitle = book.title,
        variant = variant,
        hasUpdateCard = updateCard != null,
        progress = progress,
    )
}
