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
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.layout.ExpandableFlowRow
import nl.rhaydus.designsystem.layout.FlowRowExpansion
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.domain.model.TagCategory

/**
 * The name-field hint per category. Deliberately not "Name a ${'$'}{label} tag" — that template
 * degenerates to "Name a Tag tag" for the plain Tag category.
 */
private val TagCategory.namingHint: String
    get() = when (this) {
        TagCategory.GENRE -> "Name a genre…"
        TagCategory.MOOD -> "Name a mood…"
        TagCategory.TAG -> "Name a tag…"
        TagCategory.CONTENT_WARNING -> "Name a content warning…"
        TagCategory.OTHER -> "Name a tag…"
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TagEditorAddBlock(
    selectedCategory: TagCategory,
    categoryChips: List<ChipUiModel>,
    draft: String,
    suggestionChips: List<ChipUiModel>,
    onCategoryChipEvent: (ChipEvent) -> Unit,
    onDraftChange: (String) -> Unit,
    onCommit: () -> Unit,
    onSuggestionChipEvent: (ChipEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categoryChips.forEach { chip ->
                Chip(
                    model = chip,
                    onEvent = onCategoryChipEvent,
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        TagNamingField(
            draft = draft,
            placeholder = selectedCategory.namingHint,
            onDraftChange = onDraftChange,
            onCommit = onCommit,
        )

        if (suggestionChips.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))

            // Keyed on the category so the cloud's expansion does not survive a category switch:
            // `ExpandableFlowRow` holds its revealed-line count internally, so without a fresh key per
            // category, switching from an expanded Genre into Mood would show Mood already
            // part-unfurled to the same depth, having never asked for it.
            key(selectedCategory) {
                ExpandableFlowRow(
                    expansion = FlowRowExpansion.Progressive(linesPerExpand = 3),
                    collapsible = true,
                ) {
                    suggestionChips.forEach { chip ->
                        key(chip.key) {
                            Chip(
                                model = chip,
                                onEvent = onSuggestionChipEvent,
                            )
                        }
                    }
                }
            }
        }
    }
}
