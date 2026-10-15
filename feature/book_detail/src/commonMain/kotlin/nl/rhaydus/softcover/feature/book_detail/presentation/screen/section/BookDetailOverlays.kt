package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.dialog.DatePickerEvent
import nl.rhaydus.softcover.core.component.dialog.SoftcoverDatePickerDialog
import nl.rhaydus.softcover.core.component.lists.ChooseListsBottomSheet
import nl.rhaydus.softcover.core.component.lists.ChooseListsEvent
import nl.rhaydus.softcover.core.component.lists.ListMembership
import nl.rhaydus.softcover.core.component.progress.ProgressSheetEvent
import nl.rhaydus.softcover.core.component.progress.UpdateProgressBottomSheet
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.component.verdict.VerdictDraftEvent
import nl.rhaydus.softcover.core.component.verdict.VerdictSheet
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetEvent
import nl.rhaydus.softcover.core.uibinding.progress.toActionAt
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDeadlinePickedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDeleteReviewAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissChooseListsSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissDeadlinePickerAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissEditEditionSheetClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissProgressSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissShareSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissTagEditorAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissVerdictSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnEditVerdictDraftAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnEditionSearchQueryChangeAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnEditionSelectedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnEndSessionPauseClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnEndSessionPauseSheetDismissedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnLookUpEditionByIsbnClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsReadClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnNewEditionSaveClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnProgressMinutesSteppedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnProgressTabClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnProgressTimeChangedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnRemoveUserTagAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnRequestCreateListAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnSaveVerdictAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShareCaptureEventAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShareVariantSelectedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShowEditEditionSheetClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnSubmitTagDraftAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnTagDraftChangeAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnTagEditorCategoryChangeAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnTagSuggestionSelectedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnToggleListMembershipAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnToggleUserTagSpoilerAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnUpdatePageProgressClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnUpdatePercentageProgressClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnUpdateTimeProgressClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnWhenReadResetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnWhenReadShiftedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.component.EditionBottomSheetSelector
import nl.rhaydus.softcover.feature.book_detail.presentation.component.ShareBookBottomSheet
import nl.rhaydus.softcover.feature.book_detail.presentation.component.TagEditorBottomSheet
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.EditionSheetEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareSheetEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.state.TagEditorSheetEvent

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
) {
    state.endSessionPauseSheet.modelOrNull?.let { scaffold ->
        SheetScaffold(
            model = scaffold,
            onEvent = { event ->
                when (event) {
                    SheetScaffoldEvent.PrimaryClicked -> runAction(OnEndSessionPauseClickAction())

                    SheetScaffoldEvent.Dismissed -> runAction(OnEndSessionPauseSheetDismissedAction())

                    SheetScaffoldEvent.SecondaryClicked,
                    SheetScaffoldEvent.HeaderActionClicked -> Unit
                }
            },
            content = {},
        )
    }

    state.shareSheet.modelOrNull?.let { shareSheet ->
        ShareBookBottomSheet(
            model = shareSheet,
            onEvent = { event ->
                when (event) {
                    is ShareSheetEvent.VariantSelected -> runAction(
                        OnShareVariantSelectedAction(variant = event.variant),
                    )

                    is ShareSheetEvent.CaptureReported -> runAction(
                        OnShareCaptureEventAction(captureEvent = event.captureEvent),
                    )

                    ShareSheetEvent.Dismissed -> runAction(OnDismissShareSheetAction())
                }
            },
        )
    }

    val tagEditorSheet = state.tagEditorSheet
    if (state.showTagEditorSheet && tagEditorSheet != null) {
        TagEditorBottomSheet(
            model = tagEditorSheet,
            onEvent = { event ->
                when (event) {
                    is TagEditorSheetEvent.CategorySelected -> runAction(
                        OnTagEditorCategoryChangeAction(categoryKey = event.categoryKey),
                    )

                    is TagEditorSheetEvent.DraftChanged -> runAction(OnTagDraftChangeAction(input = event.text))

                    TagEditorSheetEvent.DraftSubmitted -> runAction(OnSubmitTagDraftAction())

                    is TagEditorSheetEvent.SuggestionSelected -> runAction(
                        OnTagSuggestionSelectedAction(chipKey = event.chipKey),
                    )

                    is TagEditorSheetEvent.TagRemoved -> runAction(OnRemoveUserTagAction(chipKey = event.chipKey))

                    is TagEditorSheetEvent.TagSpoilerToggled -> runAction(
                        OnToggleUserTagSpoilerAction(chipKey = event.chipKey),
                    )

                    TagEditorSheetEvent.Dismissed -> runAction(OnDismissTagEditorAction())
                }
            },
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

                    ChooseListsEvent.NewListRequested -> runAction(OnRequestCreateListAction())

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
                    VerdictSheetEvent.Saved -> runAction(OnSaveVerdictAction(book = verdictBook))

                    is VerdictDraftEvent -> runAction(OnEditVerdictDraftAction(event = event))

                    VerdictSheetEvent.DeleteRequested -> runAction(OnDeleteReviewAction(book = verdictBook))

                    VerdictSheetEvent.Dismissed -> runAction(OnDismissVerdictSheetAction())
                }
            },
        )
    }

    val editionSheet = state.editionSheet.modelOrNull
    if (editionSheet != null) {
        EditionBottomSheetSelector(
            model = editionSheet,
            onEvent = { event ->
                when (event) {
                    is EditionSheetEvent.SearchChanged -> runAction(
                        OnEditionSearchQueryChangeAction(query = event.query),
                    )

                    is EditionSheetEvent.EditionSelected -> runAction(
                        OnEditionSelectedAction(editionId = event.editionId),
                    )

                    EditionSheetEvent.Confirmed -> runAction(OnNewEditionSaveClickAction())

                    EditionSheetEvent.Dismissed -> runAction(OnDismissEditEditionSheetClickAction())

                    EditionSheetEvent.LookUpByIsbnClicked -> runAction(OnLookUpEditionByIsbnClickAction())

                    EditionSheetEvent.RetryClicked -> runAction(OnShowEditEditionSheetClickAction())
                }
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
    val progressSheet = state.progressSheet.modelOrNull

    if (bookToUpdate != null && progressSheet != null) {
        UpdateProgressBottomSheet(
            model = progressSheet,
            onEvent = { event ->
                when (event) {
                    is ProgressSheetEvent.TabSelected -> runAction(OnProgressTabClickAction(tab = event.tab))

                    is ProgressSheetEvent.PagesSubmitted -> runAction(
                        OnUpdatePageProgressClickAction(
                            newPage = event.page,
                        ),
                    )

                    is ProgressSheetEvent.PercentageSubmitted -> runAction(
                        OnUpdatePercentageProgressClickAction(
                            newPercentage = event.percentage,
                        ),
                    )

                    is ProgressSheetEvent.TimeChanged -> runAction(
                        OnProgressTimeChangedAction(
                            unit = event.unit,
                            text = event.text,
                        ),
                    )

                    is ProgressSheetEvent.MinutesStepped -> runAction(OnProgressMinutesSteppedAction(delta = event.delta))

                    is ProgressSheetEvent.TimeSubmitted -> runAction(
                        OnUpdateTimeProgressClickAction(
                            hours = event.hours,
                            minutes = event.minutes,
                            seconds = event.seconds,
                        ),
                    )

                    ProgressSheetEvent.MarkAsReadRequested -> {
                        runAction(
                            OnMarkBookAsReadClickAction(
                                book = bookToUpdate,
                                actionAt = state.readAt.toActionAt(),
                            ),
                        )
                    }

                    is ProgressSheetEvent.WhenReadShifted -> runAction(OnWhenReadShiftedAction(minutes = event.minutes))

                    ProgressSheetEvent.WhenReadReset -> runAction(OnWhenReadResetAction())

                    ProgressSheetEvent.Dismissed -> runAction(OnDismissProgressSheetAction())
                }
            },
        )
    }
}
