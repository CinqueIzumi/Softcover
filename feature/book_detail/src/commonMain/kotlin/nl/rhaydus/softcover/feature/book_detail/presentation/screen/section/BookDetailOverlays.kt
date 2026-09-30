package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.dialog.DatePickerEvent
import nl.rhaydus.softcover.core.component.dialog.SoftcoverDatePickerDialog
import nl.rhaydus.softcover.core.component.lists.ChooseListsBottomSheet
import nl.rhaydus.softcover.core.component.lists.ChooseListsEvent
import nl.rhaydus.softcover.core.component.lists.ListMembership
import nl.rhaydus.softcover.core.component.progress.ProgressSheetEvent
import nl.rhaydus.softcover.core.component.progress.UpdateProgressBottomSheet
import nl.rhaydus.softcover.core.component.verdict.VerdictSheet
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetEvent
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
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnEditionSelectedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsReadClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnNewEditionSaveClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnProgressTabClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnRemoveUserTagAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnSaveToGalleryFinishedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnSaveToGalleryStartedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnSaveVerdictAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShareFinishedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShareStartedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShareVariantSelectedAction
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
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
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
    shareCaptureEvents: Flow<BookDetailEvent.ShareCaptureEvent>,
) {
    val shareBookCard = state.shareBookCard
    val shareSheet = state.shareSheet
    val shareSheetBody = state.shareSheetBody
    if (state.isShareSheetVisible && shareBookCard != null && shareSheet != null && shareSheetBody != null) {
        ShareBookBottomSheet(
            model = shareSheet,
            shareBookCard = shareBookCard,
            shareUpdateCard = state.shareUpdateCard,
            body = shareSheetBody,
            onVariantSelected = { runAction(OnShareVariantSelectedAction(variant = it)) },
            captureEvents = shareCaptureEvents,
            onShareClick = { runAction(OnShareStartedAction()) },
            onShareFinished = { runAction(OnShareFinishedAction()) },
            onSaveToGalleryClick = { runAction(OnSaveToGalleryStartedAction()) },
            onSaveToGalleryFinished = { runAction(OnSaveToGalleryFinishedAction()) },
            onDismissRequest = { runAction(OnDismissShareSheetAction()) },
        )
    }

    val tagEditorSheet = state.tagEditorSheet
    if (state.showTagEditorSheet && tagEditorSheet != null) {
        TagEditorBottomSheet(
            model = tagEditorSheet,
            userTagEditorGroups = state.userTagEditorGroups,
            newlyAddedTagKey = state.newlyAddedTagKey,
            categoryChips = state.tagEditorCategoryChips,
            suggestions = state.tagSuggestionChips,
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
            onTagChipEvent = { event ->
                when (event) {
                    is ChipEvent.Dismissed -> {
                        state.userTagByEditorChipKey[event.key]?.let { tag ->
                            runAction(OnRemoveUserTagAction(tag = tag))
                        }
                    }

                    is ChipEvent.SpoilerToggled -> {
                        state.userTagByEditorChipKey[event.key]?.let { tag ->
                            runAction(OnToggleUserTagSpoilerAction(tag = tag))
                        }
                    }

                    is ChipEvent.Clicked -> Unit
                }
            },
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

    val verdictBook = state.book
    val verdictSheet = state.verdictSheet
    if (state.verdictSheetContext != null && verdictBook != null && verdictSheet != null) {
        VerdictSheet(
            model = verdictSheet,
            onEvent = { event ->
                when (event) {
                    is VerdictSheetEvent.Saved -> runAction(
                        OnSaveVerdictAction(
                            book = verdictBook,
                            rating = event.rating,
                            review = event.review,
                            hasSpoilers = event.hasSpoilers,
                        ),
                    )

                    VerdictSheetEvent.DeleteRequested -> runAction(OnDeleteReviewAction(book = verdictBook))

                    VerdictSheetEvent.Dismissed -> runAction(OnDismissVerdictSheetAction())
                }
            },
        )
    }

    val editionSheet = state.editionSheet
    if (state.showEditEditionSheet && editionSheet != null) {
        EditionBottomSheetSelector(
            model = editionSheet,
            editionCovers = state.editionCovers,
            editionTrailingMarks = state.editionTrailingMarks,
            selectedEditionId = state.selectedEditionId,
            editions = state.filteredEditions,
            isLoading = state.loadingEditions,
            searchQuery = state.editionSearchQuery,
            onSearchQueryChange = {
                runAction(OnEditionSearchQueryChangeAction(query = it))
            },
            onEditionSelected = { editionId ->
                runAction(OnEditionSelectedAction(editionId = editionId))
            },
            onDismissRequest = {
                runAction(OnDismissEditEditionSheetClickAction())
            },
            onConfirmClick = {
                runAction(OnNewEditionSaveClickAction())
            },
        )
    }

    val deadlinePicker = state.deadlinePicker
    if (deadlinePicker != null) {
        SoftcoverDatePickerDialog(
            model = deadlinePicker,
            onEvent = { event ->
                when (event) {
                    is DatePickerEvent.Confirmed -> runAction(OnDeadlinePickedAction(date = event.date))

                    DatePickerEvent.Dismissed -> runAction(OnDismissDeadlinePickerAction())
                }
            },
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
