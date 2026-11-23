package nl.rhaydus.softcover.core.component.feeling

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldContent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The modal sheet for a book's private "How it felt": chips to pick the moods, then one slider per picked
 * mood. A mood with no slide still counts as there.
 *
 * Save and Cancel dismiss the sheet through its animated hide after emitting
 * [HowItFeltSheetEvent.SaveRequested] or [HowItFeltSheetEvent.CancelRequested]; the host closes its session
 * on [HowItFeltSheetEvent.Dismissed].
 */
@Composable
fun HowItFeltSheet(
    model: HowItFeltSheetUiModel,
    onEvent: (HowItFeltSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SheetScaffold(
        model = model.scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> onEvent(HowItFeltSheetEvent.SaveRequested)

                SheetScaffoldEvent.SecondaryClicked -> onEvent(HowItFeltSheetEvent.CancelRequested)

                SheetScaffoldEvent.Dismissed -> onEvent(HowItFeltSheetEvent.Dismissed)

                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
        modifier = modifier,
    ) {
        HowItFeltSheetBody(
            model = model,
            onEvent = onEvent,
        )
    }
}

@Composable
internal fun HowItFeltSheetBody(
    model: HowItFeltSheetUiModel,
    onEvent: (HowItFeltSheetEvent) -> Unit,
) {
    val playMotion = playDecorativeMotion()
    val sizeSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.xxs)
            .animateContentSize(animationSpec = if (playMotion) sizeSpec else snap()),
    ) {
        MoodChoices(
            model = model.choices,
            onChipClick = { onEvent(HowItFeltSheetEvent.ChipClicked(key = it)) },
            onOwnWordEvent = { onEvent(HowItFeltSheetEvent.OwnWordChanged(event = it)) },
            modifier = Modifier.padding(bottom = Spacing.md),
        )

        DepartingRows(rows = model.strengths) { strength ->
            MoodStrengthRow(
                model = strength,
                onStrengthChange = {
                    onEvent(
                        HowItFeltSheetEvent.StrengthChanged(
                            key = strength.key,
                            strength = it,
                        ),
                    )
                },
                modifier = Modifier.padding(bottom = Spacing.md),
            )
        }

        SoftcoverText(
            text = model.caption,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@StandardPreview
@Composable
private fun HowItFeltSheetFilledPreview() {
    HowItFeltSheetPreview(model = HowItFeltSheetUiModel.previews.getValue("Filled draft"))
}

@StandardPreview
@Composable
private fun HowItFeltSheetEmptyPreview() {
    HowItFeltSheetPreview(model = HowItFeltSheetUiModel.previews.getValue("Empty draft"))
}

@Composable
private fun HowItFeltSheetPreview(model: HowItFeltSheetUiModel) {
    SoftcoverTheme {
        SheetScaffoldContent(
            model = model.scaffold,
            onEvent = {},
        ) {
            HowItFeltSheetBody(
                model = model,
                onEvent = {},
            )
        }
    }
}
