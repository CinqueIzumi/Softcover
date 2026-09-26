package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnOpenTagEditorAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun UserTagsSection(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    val chips = state.userTagChips

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SmallSectionLabel(text = "Your tags")

        Spacer(modifier = Modifier.height(16.dp))

        if (chips.isEmpty()) {
            DashedTagOpenerChip(
                label = "+ Add tags",
                onClick = { runAction(OnOpenTagEditorAction()) },
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                chips.forEach { chip ->
                    key(chip.key) {
                        Chip(model = chip)
                    }
                }

                DashedTagOpenerChip(
                    label = "Edit tags",
                    onClick = { runAction(OnOpenTagEditorAction()) },
                )
            }
        }
    }
}

/**
 * A dashed-`outline` pill with a primary label (design-system.md's "your tags" opener) — the "+ Add
 * tags" / "Edit tags" affordance that opens [TagEditorBottomSheet]. Distinct from the solid
 * [Chip][nl.rhaydus.softcover.core.component.chip.Chip] used for read-only tags.
 */
@Composable
private fun DashedTagOpenerChip(
    label: String,
    onClick: () -> Unit,
) {
    val color = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(percent = 50)

    Surface(
        modifier = Modifier
            .clip(shape)
            .drawBehind {
                drawRoundRect(
                    color = color,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(6f, 4f)),
                    ),
                    cornerRadius = CornerRadius(size.height / 2f),
                )
            },
        color = Color.Transparent,
        contentColor = color,
        shape = shape,
        onClick = onClick,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}
