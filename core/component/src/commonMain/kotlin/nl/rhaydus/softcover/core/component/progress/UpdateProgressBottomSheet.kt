package nl.rhaydus.softcover.core.component.progress

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.control.SegmentedControl
import nl.rhaydus.softcover.core.component.control.SegmentedControlEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldContent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

/**
 * The modal sheet for editing reading progress: a `pages | percentage | time` segmented switcher over
 * a hero numeric input, an editorial suffix line and a wavy progress bar, plus the "when did you read
 * this?" backdating row, with the scaffold's footer carrying "Update progress" and "Mark as read"
 * ([ProgressSheetEvent.MarkAsReadRequested]).
 * Percentage is offered only when [ProgressSheetMedium.hasKnownTotal] is true — without a total a
 * fraction can only ever resolve to 0, so the tab is hidden rather than shown broken.
 *
 * With no known total, the sheet drops to its primary unit alone — hiding the "of N …" suffix and the
 * progress bar — and accepts free numeric/time entry with no clamp to a total. Reach for this sheet
 * before inventing a new "edit a single number on a book" surface.
 *
 * Both footer buttons dismiss the sheet through its animated hide after emitting their event; the host
 * only records the result and closes its session on [ProgressSheetEvent.Dismissed].
 */
@Composable
fun UpdateProgressBottomSheet(
    model: ProgressSheetUiModel,
    onEvent: (ProgressSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var enteredNumber by remember(model.tabs.selected) { mutableStateOf("") }

    SheetScaffold(
        model = model.scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> onEvent(model.submitEvent(enteredNumber = enteredNumber))

                SheetScaffoldEvent.SecondaryClicked -> onEvent(ProgressSheetEvent.MarkAsReadRequested)

                SheetScaffoldEvent.Dismissed -> onEvent(ProgressSheetEvent.Dismissed)

                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
        modifier = modifier,
    ) {
        ProgressBottomSheetContent(
            model = model,
            onEvent = onEvent,
            onNumberChange = { enteredNumber = it },
        )
    }
}

private fun ProgressSheetUiModel.submitEvent(enteredNumber: String): ProgressSheetEvent {
    if (tabs.selected == ProgressSheetTab.PERCENTAGE) {
        return ProgressSheetEvent.PercentageSubmitted(percentage = enteredNumber)
    }

    return when (medium) {
        is ProgressSheetMedium.Paged -> ProgressSheetEvent.PagesSubmitted(page = enteredNumber)

        is ProgressSheetMedium.Timed -> ProgressSheetEvent.TimeSubmitted(
            hours = medium.hours.text,
            minutes = medium.minutes.text,
            seconds = medium.seconds.text,
        )
    }
}

@Composable
private fun ProgressBottomSheetContent(
    model: ProgressSheetUiModel,
    onEvent: (ProgressSheetEvent) -> Unit,
    onNumberChange: (String) -> Unit,
) {
    val medium = model.medium

    val activeTab = model.tabs.selected

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(bottom = Spacing.xxs),
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

        Spacer(modifier = Modifier.height(Spacing.xl))

        WhenReadRow(
            model = model.whenRead,
            onEvent = onEvent,
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        // Percentage is the one tab both media share; the other two are the medium's own, so the
        // `when` on the sealed medium carries the compiler's exhaustiveness check rather than an
        // `activeTab` branch that would need an unreachable else.
        if (activeTab == ProgressSheetTab.PERCENTAGE) {
            ProgressBottomSheetPercentageContent(
                progressPercent = model.progressPercent,
                onNumberChange = onNumberChange,
            )
        } else {
            when (medium) {
                is ProgressSheetMedium.Paged -> ProgressBottomSheetPageContent(
                    medium = medium,
                    onNumberChange = onNumberChange,
                )

                is ProgressSheetMedium.Timed -> ProgressBottomSheetTimeContent(
                    medium = medium,
                    onEvent = onEvent,
                )
            }
        }
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
        model = ProgressSheetUiModel.previews.getValue("Pages"),
    )
}

@StandardPreview
@Composable
private fun ProgressSheetContentPercentagePreview() {
    ProgressSheetPreview(
        model = ProgressSheetUiModel.previews.getValue("Percentage"),
    )
}

/** The audiobook layout — three time fields and an hh:mm:ss suffix — which had no preview before. */
@StandardPreview
@Composable
private fun ProgressSheetContentTimePreview() {
    ProgressSheetPreview(
        model = ProgressSheetUiModel.previews.getValue("Audiobook"),
    )
}

/** No page count known: the Percentage tab disappears and the suffix + indicator drop out. */
@StandardPreview
@Composable
private fun ProgressSheetContentNoTotalPreview() {
    ProgressSheetPreview(
        model = ProgressSheetUiModel.previews.getValue("Paged, no total"),
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
                onNumberChange = {},
            )
        }
    }
}
