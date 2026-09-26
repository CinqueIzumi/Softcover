package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import nl.rhaydus.designsystem.haptics.rememberHaptics
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.progress.ProgressSheetEvent
import nl.rhaydus.softcover.core.component.progress.UpdateProgressBottomSheet
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.component.verdict.VerdictSheet
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetContext
import nl.rhaydus.softcover.feature.reading.presentation.action.DismissProgressSheetAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnDismissVerdictPromptAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnProgressTabClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnSaveVerdictAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnUpdatePageProgressClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnUpdatePercentageProgressClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnUpdateTimeProgressClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.component.StreakStripSheet
import nl.rhaydus.softcover.feature.reading.presentation.screen.MarkAsReadController
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState

/**
 * The modal overlays shared by both layouts: the progress-update bottom sheet (a progress entry that
 * finishes the book fires the same celebration as the split-button "Mark as Read", gated on the real
 * finish outcome — see the verdict-prompt effect below), the verdict sheet raised on a finish, and
 * the reading-streak sheet. Confetti is routed through [controller] so every entry point shares one
 * burst.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadingOverlays(
    state: ReadingScreenUiState,
    runAction: (ReadingAction) -> Unit,
    controller: MarkAsReadController,
    showStreakSheet: Boolean,
    onDismissStreakSheet: () -> Unit,
) {
    val haptics = rememberHaptics()

    val progressSheet = state.progressSheet

    if (state.bookToUpdate != null && state.showProgressSheet && progressSheet != null) {
        val updatingBook = state.bookToUpdate

        UpdateProgressBottomSheet(
            model = progressSheet,
            onEvent = { event ->
                when (event) {
                    is ProgressSheetEvent.TabSelected -> runAction(OnProgressTabClickAction(event.tab))

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
                        // Routes through the same controller the row/hero "mark as read" affordances used
                        // to drive directly, so the sheet-triggered path keeps the full commit choreography
                        // (haptic, burst, bottom-bar pulse, and — when motion is enabled — the "slide to
                        // shelf" follow-through, §2.5) rather than only the haptic + burst a bare dispatch
                        // would give it. The picked backdate travels with it, same as the progress tabs.
                        controller.requestMarkAsRead(
                            book = updatingBook,
                            actionAt = event.actionAt,
                        )

                        runAction(DismissProgressSheetAction)
                    }

                    ProgressSheetEvent.Dismissed -> runAction(DismissProgressSheetAction)
                }
            },
        )
    }

    val verdictBook = state.verdictPromptBook
    val verdictCover = state.verdictCover

    // A finish reached through the progress sheet (any of page/percentage/time) has no synchronous
    // "finished" moment to burst from, so the celebration rides the same Applied outcome that opens
    // this prompt — firing only on a genuine transition, never on a no-op re-record. The explicit
    // "mark as read" affordances already burst at the instant of the gesture, so they flag that they
    // handled this finish and the effect skips it, keeping any single finish to exactly one burst.
    LaunchedEffect(verdictBook?.id) {
        if (verdictBook == null) return@LaunchedEffect

        if (controller.consumeExplicitBurstFired().not()) {
            haptics.commit()
            controller.celebrate()
        }
    }

    if (verdictBook != null && verdictCover != null) {
        VerdictSheet(
            context = VerdictSheetContext.FINISHED,
            bookTitle = verdictBook.title,
            initialRating = verdictBook.userBook?.rating?.takeIf { it > 0.0 },
            initialReview = state.verdictReview ?: RichTextUiModel.EMPTY,
            initialHasSpoilers = verdictBook.userBook?.reviewHasSpoilers == true,
            canDelete = false,
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
            onDelete = {},
            onDismissRequest = { runAction(OnDismissVerdictPromptAction()) },
            cover = {
                Cover(model = verdictCover)
            },
        )
    }

    if (showStreakSheet) {
        StreakStripSheet(
            activity = state.recentReadingActivity,
            onDismiss = onDismissStreakSheet,
        )
    }
}
