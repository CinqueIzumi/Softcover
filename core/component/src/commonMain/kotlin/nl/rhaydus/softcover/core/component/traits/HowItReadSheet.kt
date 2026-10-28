package nl.rhaydus.softcover.core.component.traits

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipFlow
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldContent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The modal sheet for a book's private "How it read" survey: pace, plot or character, how the characters
 * read, and the writing style. Every question is optional.
 *
 * Save and Cancel dismiss the sheet through its animated hide after emitting
 * [HowItReadSheetEvent.SaveRequested] or [HowItReadSheetEvent.CancelRequested]; the host closes its session
 * on [HowItReadSheetEvent.Dismissed].
 */
@Composable
fun HowItReadSheet(
    model: HowItReadSheetUiModel,
    onEvent: (HowItReadSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SheetScaffold(
        model = model.scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> onEvent(HowItReadSheetEvent.SaveRequested)

                SheetScaffoldEvent.SecondaryClicked -> onEvent(HowItReadSheetEvent.CancelRequested)

                SheetScaffoldEvent.Dismissed -> onEvent(HowItReadSheetEvent.Dismissed)

                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
        modifier = modifier,
    ) {
        HowItReadSheetBody(
            model = model,
            onEvent = onEvent,
        )
    }
}

/** The sheet's body without the modal container, so the gallery and previews can host it inline. */
@Composable
internal fun HowItReadSheetBody(
    model: HowItReadSheetUiModel,
    onEvent: (HowItReadSheetEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.xxs),
    ) {
        HowItReadChipGroup(
            group = model.pace,
            onEvent = onEvent,
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        HowItReadBalance(
            model = model.balance,
            onEvent = onEvent,
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        HowItReadChipGroup(
            group = model.characters,
            onEvent = onEvent,
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        HowItReadChipGroup(
            group = model.writingStyle,
            onEvent = onEvent,
        )
    }
}

@Composable
internal fun HowItReadSheetLabel(text: String) {
    SoftcoverText(
        text = text.uppercase(),
        style = MaterialTheme.editorialTypography.eyebrowSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.semantics(mergeDescendants = true) { heading() },
    )
}

@Composable
private fun HowItReadChipGroup(
    group: HowItReadChipGroupUiModel,
    onEvent: (HowItReadSheetEvent) -> Unit,
) {
    Column {
        HowItReadSheetLabel(text = group.label)

        Spacer(modifier = Modifier.height(Spacing.sm))

        group.rows.forEachIndexed { index, row ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(Spacing.xs))
            }

            ChipFlow(
                set = row,
                onEvent = { event, key ->
                    if (event is ChipEvent.Clicked) {
                        onEvent(HowItReadSheetEvent.ChipClicked(key = key))
                    }
                },
            )
        }

        group.caption?.let { caption ->
            Spacer(modifier = Modifier.height(Spacing.sm))

            SoftcoverText(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@StandardPreview
@Composable
private fun HowItReadSheetFilledPreview() {
    HowItReadSheetPreview(model = HowItReadSheetUiModel.previews.getValue("Filled draft"))
}

@StandardPreview
@Composable
private fun HowItReadSheetEmptyPreview() {
    HowItReadSheetPreview(model = HowItReadSheetUiModel.previews.getValue("Empty draft"))
}

@Composable
private fun HowItReadSheetPreview(model: HowItReadSheetUiModel) {
    SoftcoverTheme {
        SheetScaffoldContent(
            model = model.scaffold,
            onEvent = {},
        ) {
            HowItReadSheetBody(
                model = model,
                onEvent = {},
            )
        }
    }
}
