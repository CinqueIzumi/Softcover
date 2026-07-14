package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.designsystem.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.designsystem.presentation.model.ProgressSheetTab
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview
import nl.rhaydus.softcover.feature.book_detail.domain.model.ReadingPaceForecast
import nl.rhaydus.toad.UiState

internal data class BookDetailUiState(
    val loadingBookDetails: Boolean = true,
    val book: Book? = null,
    val initialCover: BookInitialCover? = null,
    val fabMenuExpanded: Boolean = false,
    val showEditEditionSheet: Boolean = false,
    val editions: List<BookEdition> = emptyList(),
    val loadingEditions: Boolean = false,
    val editionSearchQuery: String = "",
    val previewEdition: BookEdition? = null,
    val scannedEditionId: Int? = null,
    val scannedEditionBannerDismissed: Boolean = false,
    val isUpdatingScannedEdition: Boolean = false,
    val showUpdateProgressSheet: Boolean = false,
    val selectedProgressSheetTab: ProgressSheetTab = ProgressSheetTab.PAGE,
    val dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
    val selectedLens: BookDetailLens = BookDetailLens.THE_BOOK,

    val deadline: BookDeadline? = null,
    val deadlineProgress: DeadlineProgress? = null,
    val readingPaceForecast: ReadingPaceForecast? = null,
    val showDeadlinePicker: Boolean = false,

    val reviews: List<BookReview> = emptyList(),
    val loadingReviews: Boolean = false,
    val revealedSpoilerReviewIds: Set<Int> = emptySet(),

    val showReviewSheet: Boolean = false,
    val reviewEditorDocument: ReviewDocument = ReviewDocument.EMPTY,
    val reviewEditorHasSpoilers: Boolean = false,

    val failedMutationBookIds: Set<Int> = emptySet(),
    val failedMutationEditionIds: Set<Int> = emptySet(),

    val isShareSheetVisible: Boolean = false,
    val currentUsername: String? = null,
    val currentUserAvatarUrl: String? = null,

    val showChooseListsSheet: Boolean = false,
    val userLists: List<BookList> = emptyList(),
    val listsBeingMutated: Set<Int> = emptySet(),

    val userTags: List<UserTag> = emptyList(),
    val showTagEditorSheet: Boolean = false,
    val tagEditorCategory: TagCategory = TagCategory.TAG,
    val tagEditorInput: String = "",
) : UiState {
    /**
     * The edition pinned by an external entry point (a barcode scan), if any. It wins over every
     * other edition — including the persisted on-shelf edition — so a scan always shows the exact
     * edition that was scanned. Resolved from [book]'s editions (so it carries the locally-overlaid
     * `owned` flag — e.g. the owned cover badge shows for a scanned edition the user owns); falls
     * back to [initialCover] for the pre-load frame before the book has loaded.
     */
    val scannedEdition: BookEdition?
        get() = scannedEditionId?.let { id ->
            book?.editions?.firstOrNull { it.id == id } ?: initialCover?.currentEdition?.takeIf { it.id == id }
        }

    /**
     * The edition the screen currently shows. A [scannedEdition] always wins. Otherwise a preview
     * ([previewEdition]) only applies to an off-shelf book — an ephemeral selection that is never
     * persisted or cached. Once the book has a user book, the persisted edition wins (any leftover
     * preview is ignored), so previewing then adding to the shelf transitions without flicker.
     */
    val displayedEdition: BookEdition?
        get() {
            scannedEdition?.let { return it }

            return when {
                book?.userBook != null -> book.currentEdition
                else -> previewEdition ?: book?.currentEdition ?: initialCover?.currentEdition
            }
        }

    /**
     * Whether to offer updating the on-shelf edition to the scanned one — shown only when the book
     * is already on a shelf with a *different* edition than the one scanned, and not yet dismissed.
     */
    val showScanEditionUpdateBanner: Boolean
        get() {
            val scannedId = scannedEditionId ?: return false

            if (scannedEditionBannerDismissed) return false

            val shelvedEditionId = book?.userBook?.editionId ?: return false

            return shelvedEditionId != scannedId
        }

    /**
     * Edition ids the user owns, sourced from the local "Owned" list rather than the per-shelf
     * `owned` overlay — so ownership reflects regardless of whether the book is on a reading shelf
     * (e.g. a scanned, un-shelved edition the user owns shows the owned badge and "Unmark as owned").
     */
    val ownedEditionIds: Set<Int>
        get() = userLists.firstOrNull { it.isOwned }
            ?.books
            ?.mapTo(mutableSetOf()) { it.editionId }
            ?: emptySet()

    fun isEditionOwned(edition: BookEdition?): Boolean = edition != null && edition.id in ownedEditionIds

    val filteredEditions: List<BookEdition>
        get() {
            val query = editionSearchQuery.trim()

            if (query.isEmpty()) return editions

            return editions.filter { edition ->
                edition.isbn10?.contains(
                    other = query,
                    ignoreCase = true,
                ) == true ||
                    edition.isbn13?.contains(
                        other = query,
                        ignoreCase = true,
                    ) == true ||
                    edition.publisher?.contains(
                        other = query,
                        ignoreCase = true,
                    ) == true
            }
        }
}
