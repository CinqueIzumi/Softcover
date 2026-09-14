package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.lists.ChooseListsUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ReadingUpdateShareCardUiModel
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetContext
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.personal.domain.model.ReadingPaceForecast
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import nl.rhaydus.softcover.feature.book_detail.presentation.model.BookReviewUiModel
import nl.rhaydus.toad.UiState

internal data class BookDetailUiState(
    /**
     * The screen's own identity and morph surface, seeded from the screen's constructor. They live on
     * the state so `CoverModelsCollector` can resolve the hero's shared-element key off the
     * composition (R7/R9) — a composable must not compute a transition key, and a collector cannot
     * see a nav argument that only exists as a screen parameter.
     */
    val bookId: Int? = null,
    val transitionSurface: String? = null,

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

    /**
     * What the progress sheet renders, mapped off the composition by `ProgressSheetCollector`
     * (R9) from [book] and [selectedProgressSheetTab]. The tab's stored home is
     * [selectedProgressSheetTab]; this is derived from it, so the two cannot disagree.
     */
    val progressSheet: ProgressSheetUiModel? = null,
    val dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
    val selectedLens: BookDetailLens = BookDetailLens.THE_BOOK,

    val deadline: BookDeadline? = null,
    val deadlineProgress: DeadlineProgress? = null,
    val readingPaceForecast: ReadingPaceForecast? = null,
    val showDeadlinePicker: Boolean = false,

    val reviews: ImmutableList<BookReviewUiModel> = persistentListOf(),
    val loadingReviews: Boolean = false,
    val revealedSpoilerReviewIds: Set<Int> = emptySet(),

    val verdictSheetContext: VerdictSheetContext? = null,
    val verdictReview: RichTextUiModel? = null,

    val failedMutationBookIds: Set<Int> = emptySet(),
    val failedMutationEditionIds: Set<Int> = emptySet(),

    val isShareSheetVisible: Boolean = false,
    val currentUsername: String? = null,
    val currentUserAvatarUrl: String? = null,
    val shareBookCard: BookShareCardUiModel? = null,
    val shareUpdateCard: ReadingUpdateShareCardUiModel? = null,

    /**
     * The book's covers, mapped by `CoverModelsCollector` (R9) so no composable ever resolves an
     * edition into a cover. [heroBackdropCover] deliberately carries a null `coverlessTitle`: it is
     * the blurred decorative layer behind the hero, not a cover the user reads, so a typographic
     * jacket there would only be blurred noise.
     */
    val heroCover: CoverUiModel? = null,
    val heroBackdropCover: CoverUiModel? = null,
    val fullScreenCover: CoverUiModel? = null,
    val verdictCover: CoverUiModel? = null,
    val editionSheetHeaderCover: CoverUiModel? = null,

    /** Keyed by [BookEdition.id]. Mapped from [editions], never from the derived `filteredEditions`. */
    val editionCovers: Map<Int, CoverUiModel> = emptyMap(),
    val tagEditorCover: CoverUiModel? = null,

    val showChooseListsSheet: Boolean = false,
    val userLists: List<BookList> = emptyList(),
    val listsBeingMutated: Set<Int> = emptySet(),

    /** The choose-lists sheet's rows and header, mapped by `ChooseListsCollector` (R9). */
    val chooseListsSheet: ChooseListsUiModel? = null,

    val userTags: List<UserTag> = emptyList(),
    val showTagEditorSheet: Boolean = false,
    val tagEditorCategory: TagCategory = TagCategory.TAG,
    val tagEditorInput: String = "",
    val tagSuggestions: List<UserTag> = emptyList(),
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
