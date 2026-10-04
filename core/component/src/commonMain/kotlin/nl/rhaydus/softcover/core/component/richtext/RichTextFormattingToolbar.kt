package nl.rhaydus.softcover.core.component.richtext

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipFace
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel

/**
 * Inline formatting controls for the review editor: one chip per mark, each selected while the whole
 * selection already carries that mark. The labels are words, the bold and italic ones previewing their
 * own effect. The row is inert while [RichTextFormattingToolbarUiModel.enabled] is false.
 */
@Composable
fun RichTextFormattingToolbar(
    model: RichTextFormattingToolbarUiModel,
    onEvent: (RichTextFormattingToolbarEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RichTextMarkType.entries.forEach { mark ->
            Chip(
                model = ChipUiModel(
                    key = mark.name,
                    label = mark.chipLabel(),
                    tone = ChipTone.Tonal,
                    selected = mark in model.activeMarks,
                    interaction = if (model.enabled) ChipInteraction.Clickable else ChipInteraction.Disabled,
                    face = mark.chipFace(),
                ),
                onEvent = { event ->
                    if (event is ChipEvent.Clicked) {
                        onEvent(RichTextFormattingToolbarEvent.MarkToggled(mark = mark))
                    }
                },
            )
        }
    }
}

private fun RichTextMarkType.chipLabel(): String = when (this) {
    RichTextMarkType.BOLD -> "Bold"
    RichTextMarkType.ITALIC -> "Italic"
    RichTextMarkType.SPOILER -> "Spoiler"
}

private fun RichTextMarkType.chipFace(): ChipFace = when (this) {
    RichTextMarkType.BOLD -> ChipFace.Bold
    RichTextMarkType.ITALIC -> ChipFace.Italic
    RichTextMarkType.SPOILER -> ChipFace.Plain
}
