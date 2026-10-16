package nl.rhaydus.softcover.core.component.quote

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.control.SegmentedControl
import nl.rhaydus.softcover.core.component.control.SegmentedControlEvent
import nl.rhaydus.softcover.core.component.control.ToggleEvent
import nl.rhaydus.softcover.core.component.control.ToggleRow
import nl.rhaydus.softcover.core.component.field.TextField
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import nl.rhaydus.softcover.core.component.progress.WhenReadEvent
import nl.rhaydus.softcover.core.component.progress.WhenReadRow
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldContent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The modal sheet for saving a quote from a book: the passage, where in the book it sits, an optional
 * "move my progress here" row, who can see it, and the shared backdating row.
 *
 * The footer's Save dismisses the sheet through its animated hide after emitting
 * [QuoteSheetEvent.SaveRequested]; the host only records the quote and closes its session on
 * [QuoteSheetEvent.Dismissed]. The host decides whether Save is enabled through the scaffold's footer.
 */
@Composable
fun QuoteSheet(
    model: QuoteSheetUiModel,
    onEvent: (QuoteSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SheetScaffold(
        model = model.scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> onEvent(QuoteSheetEvent.SaveRequested)

                SheetScaffoldEvent.Dismissed -> onEvent(QuoteSheetEvent.Dismissed)

                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
        modifier = modifier,
    ) {
        QuoteSheetBody(
            model = model,
            onEvent = onEvent,
        )
    }
}

/** The sheet's body without the modal container, so the gallery and previews can host it inline. */
@Composable
internal fun QuoteSheetBody(
    model: QuoteSheetUiModel,
    onEvent: (QuoteSheetEvent) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(bottom = Spacing.xxs),
    ) {
        TextField(
            model = model.text,
            onEvent = { event ->
                if (event is TextFieldEvent.ValueChanged) {
                    onEvent(QuoteSheetEvent.TextChanged(text = event.text))
                }
            },
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        QuoteSheetLabel(text = "Where")

        Spacer(modifier = Modifier.height(Spacing.sm))

        if (model.positionTabs.options.size > 1) {
            SegmentedControl(
                model = model.positionTabs,
                onEvent = { event ->
                    when (event) {
                        is SegmentedControlEvent.Selected -> {
                            onEvent(QuoteSheetEvent.PositionTabSelected(tab = event.key))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(Spacing.lg))
        }

        QuotePositionInput(
            position = model.position,
            onEvent = onEvent,
        )

        model.progressAdvance?.let { progressAdvance ->
            Spacer(modifier = Modifier.height(Spacing.lg))

            ToggleRow(
                model = progressAdvance,
                onEvent = { event ->
                    when (event) {
                        is ToggleEvent.Toggled -> {
                            onEvent(QuoteSheetEvent.ProgressAdvanceToggled(enabled = event.checked))
                        }
                    }
                },
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        QuoteSheetLabel(text = "Who can see it")

        Spacer(modifier = Modifier.height(Spacing.sm))

        SegmentedControl(
            model = model.privacy,
            onEvent = { event ->
                when (event) {
                    is SegmentedControlEvent.Selected -> onEvent(QuoteSheetEvent.PrivacySelected(option = event.key))
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        WhenReadRow(
            model = model.whenRead,
            onEvent = { event ->
                when (event) {
                    is WhenReadEvent.Shifted -> onEvent(QuoteSheetEvent.WhenReadShifted(minutes = event.minutes))

                    WhenReadEvent.Reset -> onEvent(QuoteSheetEvent.WhenReadReset)
                }
            },
        )
    }
}

@Composable
private fun QuoteSheetLabel(text: String) {
    SoftcoverText(
        text = text.uppercase(),
        style = MaterialTheme.editorialTypography.eyebrowSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.semantics(mergeDescendants = true) { heading() },
    )
}

@Composable
private fun QuotePositionInput(
    position: QuotePositionInputUiModel,
    onEvent: (QuoteSheetEvent) -> Unit,
) {
    val onPositionEvent: (TextFieldEvent) -> Unit = { event ->
        if (event is TextFieldEvent.ValueChanged) {
            onEvent(QuoteSheetEvent.PositionChanged(text = event.text))
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (position) {
            is QuotePositionInputUiModel.Page -> {
                TextField(
                    model = position.field,
                    onEvent = onPositionEvent,
                )

                QuotePositionSuffix(suffix = position.suffix)
            }

            is QuotePositionInputUiModel.Percentage -> TextField(
                model = position.field,
                onEvent = onPositionEvent,
            )

            is QuotePositionInputUiModel.Time -> {
                // Every time field's key is its ProgressTimeUnit name, so one handler serves all three.
                val onTimeEvent: (TextFieldEvent) -> Unit = { event ->
                    if (event is TextFieldEvent.ValueChanged) {
                        onEvent(
                            QuoteSheetEvent.TimeChanged(
                                unit = ProgressTimeUnit.valueOf(event.key),
                                text = event.text,
                            ),
                        )
                    }
                }

                TextField(
                    model = position.hours,
                    onEvent = onTimeEvent,
                )

                QuoteTimeColon()

                TextField(
                    model = position.minutes,
                    onEvent = onTimeEvent,
                )

                QuoteTimeColon()

                TextField(
                    model = position.seconds,
                    onEvent = onTimeEvent,
                )

                QuotePositionSuffix(suffix = position.suffix)
            }
        }
    }
}

@Composable
private fun QuotePositionSuffix(suffix: String?) {
    if (suffix == null) return

    SoftcoverText(
        text = suffix,
        style = MaterialTheme.editorialTypography.body,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = Spacing.md),
    )
}

@Composable
private fun QuoteTimeColon() {
    SoftcoverText(
        text = ":",
        modifier = Modifier.padding(horizontal = Spacing.xxs),
        style = MaterialTheme.editorialTypography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@StandardPreview
@Composable
private fun QuoteSheetPagePreview() {
    QuoteSheetPreview(model = QuoteSheetUiModel.previews.getValue("Page, advancing progress"))
}

@StandardPreview
@Composable
private fun QuoteSheetTimePreview() {
    QuoteSheetPreview(model = QuoteSheetUiModel.previews.getValue("Time"))
}

@Composable
private fun QuoteSheetPreview(model: QuoteSheetUiModel) {
    SoftcoverTheme {
        SheetScaffoldContent(
            model = model.scaffold,
            onEvent = {},
        ) {
            QuoteSheetBody(
                model = model,
                onEvent = {},
            )
        }
    }
}
