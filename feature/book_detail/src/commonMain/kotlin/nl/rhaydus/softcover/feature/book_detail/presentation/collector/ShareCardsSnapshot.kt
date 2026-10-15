package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ReadingUpdateShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import nl.rhaydus.softcover.core.component.share.ShareCardFormat
import nl.rhaydus.softcover.core.component.share.ShareCardUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.shareTagCandidates
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toBookShareCardUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toReadingUpdateShareCardUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardSize
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardVariant
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.UserTagsLoad
import nl.rhaydus.softcover.feature.book_detail.presentation.state.shareSheetFor
import nl.rhaydus.softcover.feature.book_detail.presentation.state.shareTagPickerFor
import nl.rhaydus.softcover.feature.book_detail.presentation.state.tagsOrEmpty

internal data class ShareCardsSnapshot(
    val book: Book?,
    val edition: BookEdition?,
    val username: String?,
    val avatarUrl: String?,
    val userTags: List<UserTag>,
    val chosenVariant: ShareCardVariant?,
    val chosenSize: ShareCardSize,
    val selectedBookTags: Set<String>,
    val deselectedUpdateTags: Set<String>,
    val tagPickerExpanded: Boolean,
)

internal fun BookDetailUiState.shareCardsSnapshot(): ShareCardsSnapshot = ShareCardsSnapshot(
    book = book.takeIf { shareSheetRequested && hasResolvedShareInputs() },
    edition = displayedEdition,
    username = currentUser?.username,
    avatarUrl = currentUser?.avatarUrl,
    userTags = userTags.tagsOrEmpty,
    chosenVariant = chosenShareVariant,
    chosenSize = chosenShareSize,
    selectedBookTags = selectedBookShareTags,
    deselectedUpdateTags = deselectedUpdateShareTags,
    tagPickerExpanded = shareTagPickerExpanded,
)

private fun BookDetailUiState.hasResolvedShareInputs(): Boolean {
    val tagsResolved = book?.userBook == null || userTags != UserTagsLoad.Loading

    return currentUser != null && tagsResolved
}

internal fun ShareCardsSnapshot.toShareSheetUiModel(progress: ShareCaptureProgress): ShareSheetUiModel? {
    val book = book ?: return null

    fun updateCardAt(format: ShareCardFormat) = book.toReadingUpdateShareCardUiModel(
        edition = edition,
        username = username,
        avatarUrl = avatarUrl,
        userTags = userTags.filterNot { it.name in deselectedUpdateTags },
        format = format,
    )

    val hasUpdateCard = updateCardAt(ShareCardFormat.Story) != null

    val variant = if (hasUpdateCard) {
        chosenVariant ?: ShareCardVariant.MY_UPDATE
    } else {
        ShareCardVariant.BOOK
    }

    fun cardAt(format: ShareCardFormat): ShareCardUiModel =
        updateCardAt(format).takeIf { variant == ShareCardVariant.MY_UPDATE }
            ?: book.toBookShareCardUiModel(
                edition = edition,
                format = format,
                selectedTags = selectedBookTags,
            )

    val storyCard = cardAt(ShareCardFormat.Story)

    val hasExpandableText = storyCard.hasExpandableText()

    val size = if (hasExpandableText) chosenSize else ShareCardSize.STORY

    return shareSheetFor(
        card = if (size == ShareCardSize.STORY) storyCard else cardAt(size.format),
        bookTitle = book.title,
        variant = variant,
        hasUpdateCard = hasUpdateCard,
        size = size,
        hasExpandableText = hasExpandableText,
        tagPicker = when (variant) {
            ShareCardVariant.MY_UPDATE -> shareTagPickerFor(
                variant = variant,
                candidates = userTags.shareTagCandidates(),
                selected = userTags.mapTo(mutableSetOf()) { it.name } - deselectedUpdateTags,
                expanded = tagPickerExpanded,
            )

            ShareCardVariant.BOOK -> shareTagPickerFor(
                variant = variant,
                candidates = book.shareTagCandidates(),
                selected = selectedBookTags,
                expanded = tagPickerExpanded,
            )
        },
        progress = progress,
    )
}

private fun ShareCardUiModel.hasExpandableText(): Boolean = when (this) {
    is BookShareCardUiModel -> description != null
    is ReadingUpdateShareCardUiModel.Finished -> review != null
    else -> false
}
