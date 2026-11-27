package nl.rhaydus.softcover.core.component.feeling

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldContent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme

/**
 * The modal sheet for one mood at one point along the progress bar. Both footer buttons dismiss the sheet
 * through its animated hide after emitting [MoodMomentSheetEvent.SaveRequested] or
 * [MoodMomentSheetEvent.SecondaryRequested]; the host closes its session on [MoodMomentSheetEvent.Dismissed].
 */
@Composable
fun MoodMomentSheet(
    model: MoodMomentSheetUiModel,
    onEvent: (MoodMomentSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SheetScaffold(
        model = model.scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> onEvent(MoodMomentSheetEvent.SaveRequested)

                SheetScaffoldEvent.SecondaryClicked -> onEvent(MoodMomentSheetEvent.SecondaryRequested)

                SheetScaffoldEvent.Dismissed -> onEvent(MoodMomentSheetEvent.Dismissed)

                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
        modifier = modifier,
    ) {
        MoodMomentSheetBody(
            model = model,
            onEvent = onEvent,
        )
    }
}

@Composable
internal fun MoodMomentSheetBody(
    model: MoodMomentSheetUiModel,
    onEvent: (MoodMomentSheetEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.xxs),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        MoodChoices(
            model = model.choices,
            onChipClick = { onEvent(MoodMomentSheetEvent.ChipClicked(key = it)) },
            onOwnWordEvent = { onEvent(MoodMomentSheetEvent.OwnWordChanged(event = it)) },
        )

        MoodStrengthRow(
            model = model.strength,
            onStrengthChange = { onEvent(MoodMomentSheetEvent.StrengthChanged(strength = it)) },
        )
    }
}

@StandardPreview
@Composable
private fun MoodMomentSheetAddingPreview() {
    MoodMomentSheetPreview(model = MoodMomentSheetUiModel.previews.getValue("Adding"))
}

@StandardPreview
@Composable
private fun MoodMomentSheetEditingPreview() {
    MoodMomentSheetPreview(model = MoodMomentSheetUiModel.previews.getValue("Editing"))
}

@Composable
private fun MoodMomentSheetPreview(model: MoodMomentSheetUiModel) {
    SoftcoverTheme {
        SheetScaffoldContent(
            model = model.scaffold,
            onEvent = {},
        ) {
            MoodMomentSheetBody(
                model = model,
                onEvent = {},
            )
        }
    }
}
