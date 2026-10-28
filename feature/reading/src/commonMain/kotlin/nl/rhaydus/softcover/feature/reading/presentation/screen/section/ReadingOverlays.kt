package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.core.component.haptics.play
import nl.rhaydus.softcover.core.component.progress.UpdateProgressBottomSheet
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.component.verdict.VerdictDraftEvent
import nl.rhaydus.softcover.core.component.verdict.VerdictSheet
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetEvent
import nl.rhaydus.softcover.feature.reading.presentation.action.DismissStreakSheetAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnDismissVerdictPromptAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnEditVerdictDraftAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnProgressSheetEventAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnSaveVerdictAction
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
) {
    val haptics = LocalHaptics.current

    val updatingBook = state.bookToUpdate
    val progressSheet = state.progressSheet.session.modelOrNull

    if (updatingBook != null && progressSheet != null) {
        UpdateProgressBottomSheet(
            model = progressSheet,
            onEvent = { runAction(OnProgressSheetEventAction(event = it)) },
        )
    }

    val verdictBook = state.verdictPromptBook

    // A finish reached through the progress sheet (any of page/percentage/time) has no synchronous
    // "finished" moment to burst from, so the celebration rides the same Applied outcome that opens
    // this prompt — firing only on a genuine transition, never on a no-op re-record. The explicit
    // "mark as read" affordances already burst at the instant of the gesture, so they flag that they
    // handled this finish and the effect skips it, keeping any single finish to exactly one burst.
    LaunchedEffect(verdictBook?.id) {
        if (verdictBook == null) return@LaunchedEffect

        haptics.play(Feedback.Milestone)

        if (controller.consumeExplicitBurstFired().not()) {
            controller.celebrate()
        }
    }

    val verdictSheet = state.verdictSheet
    if (verdictBook != null && verdictSheet != null) {
        VerdictSheet(
            model = verdictSheet,
            onEvent = { event ->
                when (event) {
                    VerdictSheetEvent.Saved -> runAction(OnSaveVerdictAction(book = verdictBook))

                    is VerdictDraftEvent -> runAction(OnEditVerdictDraftAction(event = event))

                    // Reading never offers Delete, so the sheet cannot raise it.
                    VerdictSheetEvent.DeleteRequested -> Unit

                    VerdictSheetEvent.Dismissed -> runAction(OnDismissVerdictPromptAction())
                }
            },
        )
    }

    if (state.showStreakSheet) {
        StreakStripSheet(
            model = state.streakSheet,
            onDismiss = { runAction(DismissStreakSheetAction) },
        )
    }
}
