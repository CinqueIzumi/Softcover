package nl.rhaydus.softcover.core.component.progress

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.control.SegmentedControl
import nl.rhaydus.softcover.core.component.control.SegmentedControlEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldContent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
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
    SheetScaffold(
        model = model.scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.Dismissed -> onEvent(ProgressSheetEvent.Dismissed)

                SheetScaffoldEvent.PrimaryClicked,
                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
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

    val activeTab = model.tabs.selected

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
    ) {
        SegmentedControl(
            model = model.tabs,
            onEvent = { event ->
                when (event) {
                    is SegmentedControlEvent.Selected -> onEvent(ProgressSheetEvent.TabSelected(tab = event.key))
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        WhenReadRow(
            model = model.whenRead,
            onEvent = onEvent,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Percentage is the one tab both media share; the other two are the medium's own, so the
        // `when` on the sealed medium carries the compiler's exhaustiveness check rather than an
        // `activeTab` branch that would need an unreachable else.
        if (activeTab == ProgressSheetTab.PERCENTAGE) {
            ProgressBottomSheetPercentageContent(
                progressPercent = model.progressPercent,
                onEvent = onEvent,
            )
        } else {
            when (medium) {
                is ProgressSheetMedium.Paged -> ProgressBottomSheetPageContent(
                    medium = medium,
                    onEvent = onEvent,
                )

                is ProgressSheetMedium.Timed -> ProgressBottomSheetTimeContent(
                    medium = medium,
                    onEvent = onEvent,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        RhaydusButton(
            label = "Mark as read",
            style = ButtonStyle.OUTLINED,
            size = ButtonSize.M,
            icon = drawableIconResource(
                icon = SoftcoverIcon.Check,
                contentDescription = "Mark as read icon",
            ),
            onClick = { onEvent(ProgressSheetEvent.MarkAsReadRequested) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * The previews render `ProgressSheetUiModel.previews` rather than re-declaring their own values, so
 * the preview set and the Component Gallery's fixture set are one list and cannot drift. The
 * fixtures are selected by what they demonstrate rather than by index, so reordering `previews`
 * cannot silently repoint a preview at a different case.
 */
@StandardPreview
@Composable
private fun ProgressSheetContentPagePreview() {
    ProgressSheetPreview(
        model = ProgressSheetUiModel.previews.first {
            it.tabs.selected == ProgressSheetTab.PAGE && it.medium.hasKnownTotal
        },
    )
}

@StandardPreview
@Composable
private fun ProgressSheetContentPercentagePreview() {
    ProgressSheetPreview(
        model = ProgressSheetUiModel.previews.first { it.tabs.selected == ProgressSheetTab.PERCENTAGE },
    )
}

/** The audiobook layout — three time fields and an hh:mm:ss suffix — which had no preview before. */
@StandardPreview
@Composable
private fun ProgressSheetContentTimePreview() {
    ProgressSheetPreview(
        model = ProgressSheetUiModel.previews.first {
            it.tabs.selected == ProgressSheetTab.TIME && it.medium.hasKnownTotal
        },
    )
}

/** No page count known: the Percentage tab disappears and the suffix + indicator drop out. */
@StandardPreview
@Composable
private fun ProgressSheetContentNoTotalPreview() {
    ProgressSheetPreview(
        model = ProgressSheetUiModel.previews.first {
            it.medium is ProgressSheetMedium.Paged && it.medium.hasKnownTotal.not()
        },
    )
}

@Composable
private fun ProgressSheetPreview(model: ProgressSheetUiModel) {
    SoftcoverTheme {
        SheetScaffoldContent(
            model = model.scaffold,
            onEvent = {},
        ) {
            ProgressBottomSheetContent(
                model = model,
                onEvent = {},
            )
        }
    }
}
