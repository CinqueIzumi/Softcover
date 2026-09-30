package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.dialog.DatePickerUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.lists.ChooseListsUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ReadingUpdateShareCardUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.verdict.VerdictBlockUiModel
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetUiModel
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
import nl.rhaydus.softcover.core.uibinding.verdict.VerdictSheetContext
import nl.rhaydus.softcover.feature.book_detail.presentation.model.BookReviewUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.model.EditionTrailingUiModel
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

    /**
     * The edition selector's in-progress pick, set to [displayedEdition]'s id when the sheet opens
     * and cleared on dismiss or save. Unlike [previewEdition] and [scannedEditionId], it never
     * outlives the sheet.
     */
    val selectedEditionId: Int? = null,
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

    val aboutSectionHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(eyebrow = "About"),
    val inProgressSectionHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(eyebrow = "In progress"),
    val reviewsSectionHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(eyebrow = "Voices"),
    val findItLabel: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Find it"),
    val tagsLabel: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Tags"),
    val yourTagsLabel: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Your tags"),

    /**
     * The "Shelve this book" opener, mapped by `ShelveControlHeaderCollector` (R9) off [book] and
     * [failedMutationBookIds] — the retry label when the last shelve write for this book failed.
     */
    val shelveControlHeader: SectionHeaderUiModel = SectionHeaderUiModel.Inline(eyebrow = "Shelve this book"),

    val deadline: BookDeadline? = null,
    val deadlineProgress: DeadlineProgress? = null,

    /** [deadlineProgress] mapped to a badge by `BookDeadlineCollector` (R9), so badge and progress share one writer and cannot disagree. */
    val deadlineBadge: BadgeUiModel? = null,
    val readingPaceForecast: ReadingPaceForecast? = null,

    /** Non-null exactly while the deadline picker is open; built by `OnOpenDeadlinePickerAction`. */
    val deadlinePicker: DatePickerUiModel? = null,

    /** [Book.effectiveReleaseDate] mapped to an unreleased-release badge by `UnreleasedBadgeCollector` (R9), off [book]. */
    val unreleasedBadge: BadgeUiModel? = null,

    /** Non-null only while `isEditionOwned(displayedEdition)`, mapped by `EditionSelectionCollector` (R9). */
    val ownedCoverBadge: BadgeUiModel? = null,

    val reviews: ImmutableList<BookReviewUiModel> = persistentListOf(),
    val loadingReviews: Boolean = false,
    val revealedSpoilerReviewIds: Set<Int> = emptySet(),

    /** Which caller opened the verdict sheet — the open flag; `null` while it is closed. */
    val verdictSheetContext: VerdictSheetContext? = null,
    val verdictReview: RichTextUiModel? = null,

    /** The "Your verdict" card, mapped by `VerdictReviewCollector` (R9) off the book's user data. */
    val verdictBlock: VerdictBlockUiModel? = null,

    /**
     * The verdict sheet, mapped by `VerdictSheetCollector` (R9) off [book], [verdictReview],
     * [verdictCover] and [verdictSheetContext]; `null` whenever [verdictSheetContext] is.
     */
    val verdictSheet: VerdictSheetUiModel? = null,

    val failedMutationBookIds: Set<Int> = emptySet(),
    val failedMutationEditionIds: Set<Int> = emptySet(),

    val isShareSheetVisible: Boolean = false,
    val currentUsername: String? = null,
    val currentUserAvatarUrl: String? = null,
    val shareBookCard: BookShareCardUiModel? = null,
    val shareUpdateCard: ReadingUpdateShareCardUiModel? = null,

    val shareSheet: SheetScaffoldUiModel? = null,

    val shareSheetBody: ShareSheetBodyUiModel? = null,

    val chosenShareVariant: ShareCardVariant? = null,
    val isSharing: Boolean = false,
    val isSavingToGallery: Boolean = false,

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

    /** Keyed by [BookEdition.id], mapped from [editions] by `EditionSelectionCollector` (R9). See [EditionTrailingUiModel]. */
    val editionTrailingMarks: ImmutableMap<Int, EditionTrailingUiModel> = persistentMapOf(),
    val tagEditorCover: CoverUiModel? = null,

    val tagEditorSheet: SheetScaffoldUiModel? = null,

    /**
     * The edition selector's chrome, mapped by `EditionSheetCollector` (R9); `null` until [book] and
     * [displayedEdition] exist.
     */
    val editionSheet: SheetScaffoldUiModel? = null,

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

    /** "Your tags" (`UserTagsSection`), mapped by `TagChipModelsCollector` (R9) off [userTags]. Read-only. */
    val userTagChips: List<ChipUiModel> = emptyList(),

    /**
     * The community tag block (`TagsSection`, The Book lens), mapped by `TagChipModelsCollector`
     * (R9) off [book]'s tags and [revealedTagKeys] — grouped by category, top-5 per category,
     * content-warning tags flagged with [nl.rhaydus.softcover.core.component.chip.ChipTone.Spoiler]
     * until their key is in [revealedTagKeys], then [nl.rhaydus.softcover.core.component.chip.ChipTone.Tonal].
     */
    val communityTagGroups: List<TagCategoryChipGroup> = emptyList(),

    /** Community content-warning tag keys `OnRevealTagSpoilerAction` has revealed, for the rest of this screen instance. */
    val revealedTagKeys: ImmutableSet<String> = persistentSetOf(),

    /** The "Add tags" / "Edit tags" opener (`UserTagsSection`), mapped by `TagChipModelsCollector` (R9) off [userTags]. */
    val tagEditorOpenerChip: ChipUiModel? = null,

    /**
     * The tag editor's own collection (`TagEditorBottomSheet` → `TagEditorCollection`), grouped in
     * [EDITABLE_CATEGORIES] order plus a trailing "Other" group, mapped by `TagChipModelsCollector`
     * (R9) off [userTags].
     */
    val userTagEditorGroups: List<UserTagEditorChipGroup> = emptyList(),

    /**
     * Every [userTagEditorGroups] chip key resolved back to the [UserTag] the remove / spoiler-toggle
     * actions need — kept beside the models rather than rebuilt in composition, mapped by
     * `TagChipModelsCollector` (R9).
     */
    val userTagByEditorChipKey: Map<String, UserTag> = emptyMap(),

    /**
     * The chip key of the [UserTag] `OnAddUserTagAction` most recently inserted, so `TagEditorCollection`
     * plays its entry animation on that one chip only. `OnOpenTagEditorAction` clears it, so a tag added
     * in an earlier sheet visit never replays the animation.
     */
    val newlyAddedTagKey: String? = null,

    /**
     * The tag editor's category picker chips, mapped by `TagEditorChipModelsCollector` (R9) — one
     * per editable [TagCategory], `selected` following [tagEditorCategory].
     */
    val tagEditorCategoryChips: List<ChipUiModel> = emptyList(),

    /** [tagSuggestions] mapped to chips by `TagEditorChipModelsCollector` (R9). */
    val tagSuggestionChips: ChipSet<UserTag> = ChipSet(),
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
