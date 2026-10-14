package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.component.LocalModalSheetForm
import nl.rhaydus.designsystem.model.ModalSheetForm
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipFlow
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareSheetEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareTagPickerUiModel

@Composable
internal fun ShareTagPicker(
    model: ShareTagPickerUiModel,
    onEvent: (ShareSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun toggle(tag: String) {
        onEvent(
            ShareSheetEvent.TagToggled(
                variant = model.variant,
                tag = tag,
            ),
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        model.groups.forEach { group ->
            Spacer(modifier = Modifier.height(Spacing.xl))

            SectionHeader(model = group.header)

            Spacer(modifier = Modifier.height(Spacing.md))

            // A pointer can't scroll a chip row sideways in the desktop panel, so there the tags wrap.
            if (LocalModalSheetForm.current == ModalSheetForm.PANEL) {
                ChipFlow(
                    set = group.tags,
                    onEvent = { _, tag -> toggle(tag) },
                )
            } else {
                Row(
                    modifier = Modifier.horizontalScroll(state = rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    group.tags.chips.forEach { chip ->
                        Chip(
                            model = chip,
                            onEvent = { toggle(chip.key) },
                        )
                    }
                }
            }
        }
    }
}
