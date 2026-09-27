package nl.rhaydus.softcover.core.component.progress

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.LocalModalSheetForm
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.model.ModalSheetForm
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme

/**
 * The modal sheet for editing reading progress: a `pages | percentage | time` segmented switcher over
 * a hero numeric input, an editorial suffix line and a wavy progress bar, plus the "when did you read
 * this?" backdating row and a "Mark as read" action ([ProgressSheetEvent.MarkAsReadRequested]).
 * Percentage is offered only when [ProgressSheetMedium.hasKnownTotal] is true — without a total a
 * fraction can only ever resolve to 0, so the tab is hidden rather than shown broken.
 *
 * With no known total, the sheet drops to its primary unit alone — hiding the "of N …" suffix and the
 * progress bar — and accepts free numeric/time entry with no clamp to a total. Reach for this sheet
 * before inventing a new "edit a single number on a book" surface.
 *
 * "Mark as read" is a standard secondary action present on every progress sheet: an outlined button
 * below the tab content's own filled "Update progress" action.
 */
@Composable
fun UpdateProgressBottomSheet(
    model: ProgressSheetUiModel,
    onEvent: (ProgressSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    AdaptiveModalSheet(
        onDismissRequest = { onEvent(ProgressSheetEvent.Dismissed) },
        modifier = modifier,
    ) {
        ProgressBottomSheetContent(
            model = model,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun ProgressBottomSheetContent(
    model: ProgressSheetUiModel,
    onEvent: (ProgressSheetEvent) -> Unit,
) {
    val medium = model.medium

    // Percentage entry needs a known total to convert a fraction into pages (or seconds); without one
    // it can only ever record 0, so it's offered only when that total is known. The primary unit —
    // time for an audiobook, pages otherwise — always stays, allowing free entry even with no total.
    val primaryTab = when (medium) {
        is ProgressSheetMedium.Timed -> ProgressSheetTab.TIME
        is ProgressSheetMedium.Paged -> ProgressSheetTab.PAGE
    }

    val visibleTabs = if (medium.hasKnownTotal) {
        listOf(primaryTab, ProgressSheetTab.PERCENTAGE)
    } else {
        listOf(primaryTab)
    }

    // A stored unit that isn't valid for this book (e.g. PAGE on an audiobook) falls back to the
    // primary tab, which is always the first visible one.
    val activeTab = if (model.selectedTab in visibleTabs) model.selectedTab else primaryTab

    val focusManager = LocalFocusManager.current

    // Shared across all three tabs (patterns/reading.md, "Backdate a logged action") — whichever
    // tab's "Update progress" button fires carries this value. Null means "just now": the server stamps
    // the mutation with its own current time, reproducing today's behaviour exactly. Once the reader
    // backdates it, the picked instant travels with every tab's submit until cleared.
    var readAt by remember { mutableStateOf<LocalDateTime?>(null) }

    val actionAt = readAt?.toInstant(TimeZone.currentSystemDefault())?.toString()

    // A moderate 56dp M button on both forms — the sheet form previously used a 96dp L button, but
    // that read as oversized against the redline spec's ~52/48dp actions, so both the phone sheet
    // and the desktop panel now share the same size. Still read through LocalModalSheetForm (rather
    // than hardcoding M directly) so a future form-specific need has a seam to hook into.
    val actionButtonSize = when (LocalModalSheetForm.current) {
        ModalSheetForm.PANEL, ModalSheetForm.SHEET -> ButtonSize.M
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(state = rememberScrollState())
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
    ) {
        EditorialHeader(title = model.bookTitle)

        Spacer(modifier = Modifier.height(32.dp))

        TabSwitcher(
            activeTab = activeTab,
            visibleTabs = visibleTabs,
            onEvent = onEvent,
        )

        Spacer(modifier = Modifier.height(24.dp))

        WhenReadRow(
            pickedDateTime = readAt,
            onPickedDateTimeChange = { readAt = it },
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Percentage is the one tab both media share; the other two are the medium's own, so the
        // `when` on the sealed medium carries the compiler's exhaustiveness check rather than an
        // `activeTab` branch that would need an unreachable else.
        if (activeTab == ProgressSheetTab.PERCENTAGE) {
            ProgressBottomSheetPercentageContent(
                progressPercent = model.progressPercent,
                buttonSize = actionButtonSize,
                actionAt = actionAt,
                onEvent = onEvent,
            )
        } else {
            when (medium) {
                is ProgressSheetMedium.Paged -> ProgressBottomSheetPageContent(
                    medium = medium,
                    buttonSize = actionButtonSize,
                    actionAt = actionAt,
                    onEvent = onEvent,
                )

                is ProgressSheetMedium.Timed -> ProgressBottomSheetTimeContent(
                    medium = medium,
                    buttonSize = actionButtonSize,
                    actionAt = actionAt,
                    onEvent = onEvent,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        RhaydusButton(
            label = "Mark as read",
            style = ButtonStyle.OUTLINED,
            size = actionButtonSize,
            icon = drawableIconResource(
                icon = SoftcoverIcon.Check,
                contentDescription = "Mark as read icon",
            ),
            onClick = { onEvent(ProgressSheetEvent.MarkAsReadRequested(actionAt = actionAt)) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * The previews render `ProgressSheetUiModel.previews` rather than re-declaring their own values, so
 * the preview set and the Component Gallery's fixture set are one list and cannot drift (R5). The
 * fixtures are selected by what they demonstrate rather than by index, so reordering `previews`
 * cannot silently repoint a preview at a different case.
 */
@StandardPreview
@Composable
private fun ProgressSheetContentPagePreview() {
    SoftcoverTheme {
        ProgressBottomSheetContent(
            model = ProgressSheetUiModel.previews.first {
                it.selectedTab == ProgressSheetTab.PAGE && it.medium.hasKnownTotal
            },
            onEvent = {},
        )
    }
}

@StandardPreview
@Composable
private fun ProgressSheetContentPercentagePreview() {
    SoftcoverTheme {
        ProgressBottomSheetContent(
            model = ProgressSheetUiModel.previews.first { it.selectedTab == ProgressSheetTab.PERCENTAGE },
            onEvent = {},
        )
    }
}

/** The audiobook layout — three time fields and an hh:mm:ss suffix — which had no preview before. */
@StandardPreview
@Composable
private fun ProgressSheetContentTimePreview() {
    SoftcoverTheme {
        ProgressBottomSheetContent(
            model = ProgressSheetUiModel.previews.first {
                it.selectedTab == ProgressSheetTab.TIME && it.medium.hasKnownTotal
            },
            onEvent = {},
        )
    }
}

/** No page count known: the Percentage tab disappears and the suffix + indicator drop out. */
@StandardPreview
@Composable
private fun ProgressSheetContentNoTotalPreview() {
    SoftcoverTheme {
        ProgressBottomSheetContent(
            model = ProgressSheetUiModel.previews.first {
                it.medium is ProgressSheetMedium.Paged && it.medium.hasKnownTotal.not()
            },
            onEvent = {},
        )
    }
}
