package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.designsystem.layout.ExpandableFlowRow
import nl.rhaydus.designsystem.layout.FlowRowExpansion
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.field.TextField
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TagEditorAddBlock(
    selectedCategoryKey: String,
    categoryChips: ImmutableList<ChipUiModel>,
    namingField: TextFieldUiModel,
    suggestionChips: ImmutableList<ChipUiModel>,
    onCategoryChipEvent: (ChipEvent) -> Unit,
    onNamingFieldEvent: (TextFieldEvent) -> Unit,
    onSuggestionChipEvent: (ChipEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            categoryChips.forEach { chip ->
                Chip(
                    model = chip,
                    onEvent = onCategoryChipEvent,
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        TextField(
            model = namingField,
            onEvent = onNamingFieldEvent,
        )

        if (suggestionChips.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Spacing.md))

            // Keyed on the category so the cloud's expansion does not survive a category switch:
            // `ExpandableFlowRow` holds its revealed-line count internally, so without a fresh key per
            // category, switching from an expanded Genre into Mood would show Mood already
            // part-unfurled to the same depth, having never asked for it.
            key(selectedCategoryKey) {
                ExpandableFlowRow(
                    itemCount = suggestionChips.size,
                    key = { index -> suggestionChips[index].key },
                    expansion = FlowRowExpansion.Progressive(linesPerExpand = 3),
                    collapsible = true,
                ) { index ->
                    Chip(
                        model = suggestionChips[index],
                        onEvent = onSuggestionChipEvent,
                    )
                }
            }
        }
    }
}
