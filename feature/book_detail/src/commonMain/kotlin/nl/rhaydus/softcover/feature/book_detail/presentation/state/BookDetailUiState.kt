package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.callout.CalloutUiModel
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurstScale
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurstUiModel
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.deadline.DeadlineRowUiModel
import nl.rhaydus.softcover.core.component.dialog.DatePickerUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.lists.ChooseListsUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.share.ShareCaptureState
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarSurface
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.component.transition.BookTransitionKey
import nl.rhaydus.softcover.core.component.verdict.VerdictBlockUiModel
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.personal.domain.model.ReadingPaceForecast
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.uibinding.progress.ProgressTimeDraft
import nl.rhaydus.softcover.core.uibinding.verdict.VerdictSheetContext
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.BOOK_REVIEW_STAND_INS
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.editionCountHeaderFor
import nl.rhaydus.softcover.feature.book_detail.presentation.model.BookReviewUiModel
import nl.rhaydus.toad.UiState

internal data class BookDetailUiState(
    /**
     * The screen's own identity and morph surface, seeded from the screen's constructor. They live on
     * the state because the hero's shared-element key is resolved by a collector, which cannot see a
     * nav argument that only exists as a screen parameter.
     */
    val bookId: Int,
    val transitionSurface: String? = null,

    val loadingBookDetails: Boolean = true,
    val book: Book? = null,

    /**
     * The shelf a not-yet-shelved [book] shows as selected from the tap until the stored row arrives;
     * `null` otherwise. A book that already has a user book is driven by the stored status instead.
     */
    val pendingShelfStatus: BookStatus? = null,

    val markAsReadBurst: MarkAsReadBurstUiModel = MarkAsReadBurstUiModel(
        triggerKey = 0,
        scale = MarkAsReadBurstScale.Card,
    ),

    /**
     * Opaque and titled with the book. Desktop's bar, mobile's bar once the hero has scrolled away,
     * and the offline placeholder's bar, which has no hero to sit over.
     */
    val topBar: TopBarUiModel = topBarFor(book = null),
    val initialCover: BookInitialCover? = null,
    val fabMenuExpanded: Boolean = false,
    val showEditEditionSheet: Boolean = false,
    val editions: ImmutableList<BookEdition> = persistentListOf(),
    val loadingEditions: Boolean = false,

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
    val selectedProgressSheetTab: ProgressSheetTab = ProgressSheetTab.PAGE,

    /** The reader's uncommitted time entry; `null` seeds the sheet's fields from the book. */
    val progressTimeDraft: ProgressTimeDraft? = null,

    /** The reader's backdate for the next logged action; `null` is "just now". */
    val readAt: LocalDateTime? = null,

    /**
     * What the progress sheet renders, derived from [book] and [selectedProgressSheetTab]. The tab's
     * stored home is [selectedProgressSheetTab], so the two cannot disagree.
     */
    val progressSheet: SheetSession<ProgressSheetUiModel, Nothing> = SheetSession.Closed,

    val progressSheetRequested: Boolean = false,
    val dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
    val selectedLens: BookDetailLens = BookDetailLens.THE_BOOK,

    val aboutSectionHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(eyebrow = "About"),
    val inProgressSectionHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(eyebrow = "In progress"),
    val reviewsSectionHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Reviews",
        headline = "What readers think",
    ),
    val findItLabel: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Find it"),
    val tagsLabel: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Tags"),
    val yourTagsLabel: SectionHeaderUiModel = SectionHeaderUiModel.Label(eyebrow = "Your tags"),

    /** The "Shelve this book" opener; the retry label when the last shelve write for this book failed. */
    val shelveControlHeader: SectionHeaderUiModel = SectionHeaderUiModel.Inline(eyebrow = "Shelve this book"),

    /**
     * The sticky lens switch, mapped by `LensToggleCollector` off [selectedLens] and whether [book] has a user
     * copy.
     */
    val lensToggle: SegmentedControlUiModel<BookDetailLens> = lensToggleFor(
        selectedLens = BookDetailLens.THE_BOOK,
        yoursEnabled = false,
    ),

    val deadline: BookDeadline? = null,

    /** The deadline card, mapped by `BookDeadlineCollector`; null while the book has no computable deadline. */
    val deadlineRow: DeadlineRowUiModel? = null,
    val readingPaceForecast: ReadingPaceForecast? = null,

    /** [book]'s reading progress, mapped by `InProgressCollector`; null while the book has no read. */
    val inProgress: InProgressUiModel? = null,

    /** Null while the book has no read. */
    val shelveReadingStatus: ShelveReadingStatusUiModel? = null,

    /** Non-null exactly while the deadline picker is open; built by `OnOpenDeadlinePickerAction`. */
    val deadlinePicker: DatePickerUiModel? = null,

    val unreleasedBadge: BadgeUiModel? = null,

    /** The "Find it" pills, mapped by `ExternalLinksCollector` off [displayedEdition]; null without an ISBN. */
    val externalLinks: ImmutableList<PillButtonUiModel>? = null,

    val updateProgressPill: PillButtonUiModel = PillButtonUiModel(
        key = "update-progress",
        content = PillButtonContent.Label(
            text = "Update",
            leadingIcon = SoftcoverIcon.Edit,
        ),
        tone = PillButtonTone.Accent,
    ),
    val tagAddPill: PillButtonUiModel = PillButtonUiModel(
        key = "add-tag",
        content = PillButtonContent.Label(text = "Add"),
        tone = PillButtonTone.Filled,
    ),

    val statusCallout: CalloutUiModel? = null,

    val scanEditionCallout: CalloutUiModel? = null,

    /** Null while the edition is unknown. */
    val heroEdition: HeroEditionUiModel? = null,

    /** Non-null only while `isEditionOwned(displayedEdition)`. */
    val ownedCoverBadge: BadgeUiModel? = null,

    val reviews: ImmutableList<BookReviewUiModel> = BOOK_REVIEW_STAND_INS,

    /** The reviews footer's "avg across N ratings" line; `null` without ratings. */
    val reviewsSummary: String? = null,

    /** Which caller opened the verdict sheet — the open flag; `null` while it is closed. */
    val verdictSheetContext: VerdictSheetContext? = null,

    val verdictBlock: VerdictBlockUiModel? = null,

    /** `null` whenever [verdictSheetContext] is. */
    val verdictSheet: VerdictSheetUiModel? = null,

    val failedMutationBookIds: ImmutableSet<Int> = persistentSetOf(),
    val failedMutationEditionIds: ImmutableSet<Int> = persistentSetOf(),

    val currentUsername: String? = null,
    val currentUserAvatarUrl: String? = null,

    /** The share sheet for the chosen variant. */
    val shareSheet: SheetSession<ShareSheetUiModel, Nothing> = SheetSession.Closed,

    val shareSheetRequested: Boolean = false,

    val chosenShareVariant: ShareCardVariant? = null,

    val shareCapture: ShareCaptureState = ShareCaptureState(),

    /**
     * [heroBackdropCover] deliberately carries a null `coverlessTitle`: it is
     * the blurred decorative layer behind the hero, not a cover the user reads, so a typographic
     * jacket there would only be blurred noise.
     */
    val heroCover: CoverUiModel? = null,
    val heroBackdropCover: CoverUiModel? = null,
    val fullScreenCover: CoverUiModel? = null,
    val heroTitleTransitionKey: BookTransitionKey? = null,
    val heroRatingTransitionKey: BookTransitionKey? = null,
    val verdictCover: CoverUiModel? = null,
    val editionSheetHeaderCover: CoverUiModel? = null,

    /** Ordered like `filteredEditions`. */
    val editionCards: ImmutableList<BookCardUiModel> = persistentListOf(),

    val editionCountHeader: SectionHeaderUiModel = editionCountHeaderFor(count = 0),

    /** Mobile's bar while it sits over the hero's cover art: untitled and transparent. */
    val overMediaTopBar: TopBarUiModel = TopBarUiModel(surface = TopBarSurface.OverMedia),

    /** `null` until the library's offline copy has resolved. */
    val offlineEmptyState: EmptyStateUiModel? = null,
    val tagEditorCover: CoverUiModel? = null,

    /** `null` until [book] exists. */
    val tagEditorSheet: TagEditorSheetUiModel? = null,

    /** `null` until [book] and [displayedEdition] exist. */
    val editionSheet: EditionSheetUiModel? = null,

    val showChooseListsSheet: Boolean = false,

    val createListPending: Boolean = false,
    val userLists: ImmutableList<BookList> = persistentListOf(),
    val listsBeingMutated: ImmutableSet<Int> = persistentSetOf(),

    val chooseListsSheet: ChooseListsUiModel? = null,

    val userTags: ImmutableList<UserTag> = persistentListOf(),
    val showTagEditorSheet: Boolean = false,
    val tagEditorCategory: TagCategory = TagCategory.TAG,
    val tagEditorInput: String = "",

    val tagNamingField: TextFieldUiModel = tagNamingFieldFor(
        input = "",
        category = TagCategory.TAG,
        addPill = tagAddPill,
    ),
    val tagSuggestions: ImmutableList<UserTag> = persistentListOf(),

    val userTagChips: ImmutableList<ChipUiModel> = persistentListOf(),

    val communityTagGroups: ImmutableList<TagCategoryChipGroup> = persistentListOf(),

    /**
     * Community content-warning tag keys `OnRevealTagSpoilerAction` has revealed, for the rest of this screen
     * instance.
     */
    val revealedTagKeys: ImmutableSet<String> = persistentSetOf(),

    val tagEditorOpenerChip: ChipUiModel? = null,
    val communityTagsShowMoreChip: ChipUiModel = ChipUiModel(
        key = "community-tags-show-more",
        label = "Show more",
        tone = ChipTone.Dashed,
        interaction = ChipInteraction.Clickable,
    ),
    val communityTagsShowLessChip: ChipUiModel = ChipUiModel(
        key = "community-tags-show-less",
        label = "Show less",
        tone = ChipTone.Dashed,
        interaction = ChipInteraction.Clickable,
    ),

    val userTagEditorGroups: ImmutableList<UserTagEditorChipGroup> = persistentListOf(),
    val tagEditorEmptyState: EmptyStateUiModel = EmptyStateUiModel(
        title = "No tags yet",
        body = "What would you file this under?",
    ),

    val userTagByEditorChipKey: ImmutableMap<String, UserTag> = persistentMapOf(),

    /**
     * The chip key of the [UserTag] `OnAddUserTagAction` most recently inserted, so `TagEditorCollection`
     * plays its entry animation on that one chip only. `OnOpenTagEditorAction` clears it, so a tag added
     * in an earlier sheet visit never replays the animation.
     */
    val newlyAddedTagKey: String? = null,

    val tagEditorCategoryChips: ImmutableList<ChipUiModel> = persistentListOf(),

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

    val heroTitle: String?
        get() = book?.title ?: initialCover?.title

    val heroReleaseYear: Int?
        get() = displayedEdition?.releaseYear.takeIf { it != -1 } ?: book?.releaseYear

    val failedHeroEditionId: Int?
        get() = book?.currentEdition?.id?.takeIf { it in failedMutationEditionIds }

    /** Null while unknown; blank when the book has no series. */
    val heroSeriesText: String?
        get() = book?.let { it.seriesText.orEmpty() } ?: initialCover?.seriesText

    val heroRating: Double?
        get() = book?.rating ?: initialCover?.rating

    val shelfStatus: BookStatus?
        get() = when {
            book == null -> null
            book.userBook == null -> pendingShelfStatus ?: book.status
            else -> book.status
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
        get() = editions.filterByEditionSearch(editionSheet?.searchQuery.orEmpty())
}
