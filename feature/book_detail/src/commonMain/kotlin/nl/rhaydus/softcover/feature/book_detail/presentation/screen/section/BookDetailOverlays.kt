package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.dialog.SoftcoverDatePickerDialog
import nl.rhaydus.softcover.core.component.lists.ChooseListsBottomSheet
import nl.rhaydus.softcover.core.component.lists.ChooseListsEvent
import nl.rhaydus.softcover.core.component.lists.ListMembership
import nl.rhaydus.softcover.core.component.progress.ProgressSheetEvent
import nl.rhaydus.softcover.core.component.progress.UpdateProgressBottomSheet
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.component.richtext.isBlank
import nl.rhaydus.softcover.core.component.verdict.VerdictSheet
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetContext
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnAddUserTagAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDeadlinePickedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDeleteReviewAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissChooseListsSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissDeadlinePickerAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissEditEditionSheetClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissProgressSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissShareSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissTagEditorAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissVerdictSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnEditionSearchQueryChangeAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsReadClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnNewEditionSaveClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnProgressTabClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnRemoveUserTagAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnSaveVerdictAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnTagDraftChangeAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnTagEditorCategoryChangeAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnToggleListMembershipAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnToggleUserTagSpoilerAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnUpdatePageProgressClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnUpdatePercentageProgressClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnUpdateTimeProgressClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.component.EditionBottomSheetSelector
import nl.rhaydus.softcover.feature.book_detail.presentation.component.ShareBookBottomSheet
import nl.rhaydus.softcover.feature.book_detail.presentation.component.TagEditorBottomSheet
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

/**
 * Every modal surface the Book Detail screen can raise — share, tag editor, choose-lists, review
 * editor, edition selector, deadline picker, and the update-progress sheet — hosted in one place so
 * both the mobile and desktop layouts raise an identical set of overlays from a single call. Each is
 * gated on its own state flag; nothing renders until the matching flag is set.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun BookDetailOverlays(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    onCreateNewListClick: () -> Unit,
) {
    val shareBookCard = state.shareBookCard
    if (state.isShareSheetVisible && shareBookCard != null) {
        ShareBookBottomSheet(
            shareBookCard = shareBookCard,
            shareUpdateCard = state.shareUpdateCard,
            onDismissRequest = { runAction(OnDismissShareSheetAction()) },
        )
    }

    if (state.showTagEditorSheet && state.book != null) {
        TagEditorBottomSheet(
            bookTitle = state.book.title,
            cover = state.tagEditorCover,
            userTags = state.userTags,
            categoryChips = state.tagEditorCategoryChips,
            suggestionChips = state.tagSuggestionChips,
            suggestionByChipKey = state.tagSuggestionByChipKey,
            selectedCategory = state.tagEditorCategory,
            draft = state.tagEditorInput,
            onCategorySelected = { runAction(OnTagEditorCategoryChangeAction(category = it)) },
            onDraftChange = { runAction(OnTagDraftChangeAction(input = it)) },
            onAddTag = { name, category ->
                runAction(
                    OnAddUserTagAction(
                        name = name,
                        category = category,
                    ),
                )
            },
            onSuggestionSelected = {
                runAction(
                    OnAddUserTagAction(
                        name = it.name,
                        category = it.category,
                    ),
                )
            },
            onRemoveTag = { runAction(OnRemoveUserTagAction(tag = it)) },
            onToggleSpoiler = { runAction(OnToggleUserTagSpoilerAction(tag = it)) },
            onDismissRequest = { runAction(OnDismissTagEditorAction()) },
        )
    }

    val chooseListsSheet = state.chooseListsSheet

    if (state.showChooseListsSheet && state.book != null && chooseListsSheet != null) {
        ChooseListsBottomSheet(
            model = chooseListsSheet,
            onEvent = { event ->
                when (event) {
                    is ChooseListsEvent.MembershipToggled -> runAction(
                        OnToggleListMembershipAction(
                            listId = event.listId,
                            isMember = event.membership == ListMembership.ALL,
                        ),
                    )

                    ChooseListsEvent.NewListRequested -> onCreateNewListClick()

                    ChooseListsEvent.Dismissed -> runAction(OnDismissChooseListsSheetAction())
                }
            },
        )
    }

    val verdictContext = state.verdictSheetContext
    val verdictBook = state.book
    if (verdictContext != null && verdictBook != null) {
        VerdictSheet(
            context = verdictContext,
            bookTitle = verdictBook.title,
            initialRating = verdictBook.userBook?.rating?.takeIf { it > 0.0 },
            initialReview = state.verdictReview ?: RichTextUiModel.EMPTY,
            initialHasSpoilers = verdictBook.userBook?.reviewHasSpoilers == true,
            canDelete = verdictContext == VerdictSheetContext.EDIT &&
                state.verdictReview?.isBlank() == false,
            onSave = { rating, review, hasSpoilers ->
                runAction(
                    OnSaveVerdictAction(
                        book = verdictBook,
                        rating = rating,
                        review = review,
                        hasSpoilers = hasSpoilers,
                    ),
                )
            },
            onDelete = { runAction(OnDeleteReviewAction(book = verdictBook)) },
            onDismissRequest = { runAction(OnDismissVerdictSheetAction()) },
            cover = {
                state.verdictCover?.let { Cover(model = it) }
            },
        )
    }

    val currentEditionForSheet = state.displayedEdition
    if (state.showEditEditionSheet && state.book != null && currentEditionForSheet != null) {
        EditionBottomSheetSelector(
            bookTitle = state.book.title,
            currentEdition = currentEditionForSheet,
            headerCover = state.editionSheetHeaderCover,
            editionCovers = state.editionCovers,
            editions = state.filteredEditions,
            isLoading = state.loadingEditions,
            searchQuery = state.editionSearchQuery,
            onSearchQueryChange = {
                runAction(OnEditionSearchQueryChangeAction(query = it))
            },
            onDismissRequest = {
                runAction(OnDismissEditEditionSheetClickAction())
            },
            onConfirmClick = {
                runAction(OnNewEditionSaveClickAction(edition = it))
            },
        )
    }

    if (state.showDeadlinePicker) {
        SoftcoverDatePickerDialog(
            initialDate = state.deadline?.deadlineDate,
            onDismiss = { runAction(OnDismissDeadlinePickerAction()) },
            onConfirm = { runAction(OnDeadlinePickedAction(date = it)) },
        )
    }

    val bookToUpdate = state.book
    val progressSheet = state.progressSheet

    if (state.showUpdateProgressSheet && bookToUpdate != null && progressSheet != null) {
        UpdateProgressBottomSheet(
            model = progressSheet,
            onEvent = { event ->
                when (event) {
                    is ProgressSheetEvent.TabSelected -> runAction(OnProgressTabClickAction(tab = event.tab))

                    is ProgressSheetEvent.PagesSubmitted -> runAction(
                        OnUpdatePageProgressClickAction(
                            newPage = event.page,
                            actionAt = event.actionAt,
                        ),
                    )

                    is ProgressSheetEvent.PercentageSubmitted -> runAction(
                        OnUpdatePercentageProgressClickAction(
                            newPercentage = event.percentage,
                            actionAt = event.actionAt,
                        ),
                    )

                    is ProgressSheetEvent.TimeSubmitted -> runAction(
                        OnUpdateTimeProgressClickAction(
                            hours = event.hours,
                            minutes = event.minutes,
                            seconds = event.seconds,
                            actionAt = event.actionAt,
                        ),
                    )

                    is ProgressSheetEvent.MarkAsReadRequested -> {
                        // The same dispatchable action the Shelve control's "Read" row fires. Its celebration
                        // (commit haptic + MarkAsReadBurst) is driven screen-wide off the ScreenModel's
                        // BookMarkedAsReadEvent (BookDetailScreen.Content's ObserveAsEvents), not by anything
                        // local to that row, so dispatching it from the sheet gets the same commit haptic +
                        // burst for free — no new action/state needed. The picked backdate travels with it.
                        runAction(
                            OnMarkBookAsReadClickAction(
                                book = bookToUpdate,
                                actionAt = event.actionAt,
                            ),
                        )

                        runAction(OnDismissProgressSheetAction())
                    }

                    ProgressSheetEvent.Dismissed -> runAction(OnDismissProgressSheetAction())
                }
            },
        )
    }
}
