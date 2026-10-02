package nl.rhaydus.softcover.core.component.divider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

private val DIVIDER_THICKNESS = 1.dp
private val CENTER_GAP = 12.dp

/** A hairline rule, bare or broken by a centred glyph or label. */
@Composable
fun Divider(
    model: DividerUiModel,
    modifier: Modifier = Modifier,
) {
    when (model) {
        is DividerUiModel.Horizontal -> HorizontalDividerScaffold(
            model = model,
            modifier = modifier,
        )

        is DividerUiModel.Vertical -> VerticalDividerScaffold(
            model = model,
            modifier = modifier,
        )
    }
}

@Composable
private fun HorizontalDividerScaffold(
    model: DividerUiModel.Horizontal,
    modifier: Modifier,
) {
    val color = model.ink.resolve(
        colorScheme = MaterialTheme.colorScheme,
        contentColor = LocalContentColor.current,
    )

    if (model.center == null) {
        HorizontalDivider(
            modifier = modifier.padding(horizontal = model.inset),
            thickness = DIVIDER_THICKNESS,
            color = color,
        )
        return
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CENTER_GAP),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = model.inset),
    ) {
        HorizontalDivider(
            color = color,
            thickness = DIVIDER_THICKNESS,
            modifier = Modifier.weight(1f),
        )

        DividerCenterContent(center = model.center)

        HorizontalDivider(
            color = color,
            thickness = DIVIDER_THICKNESS,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DividerCenterContent(center: DividerCenter) {
    when (center) {
        DividerCenter.QuoteMark -> QuoteMarkGlyph()
        is DividerCenter.Label -> DividerLabel(text = center.text)
    }
}

/**
 * The glyph's `lineHeight` is pinned to its `fontSize` so the rule stays a thin row instead of
 * reserving `quoteGlyph`'s display-scale line box; `offset(y = 6.dp)` corrects the residual baseline.
 */
@Composable
private fun QuoteMarkGlyph() {
    SoftcoverText(
        text = "“",
        style = MaterialTheme.editorialTypography.quoteGlyph.copy(
            fontSize = 40.sp,
            lineHeight = 40.sp,
        ),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
        modifier = Modifier
            .offset(y = 6.dp)
            .clearAndSetSemantics {},
    )
}

@Composable
private fun DividerLabel(text: String) {
    SoftcoverText(
        text = text.uppercase(),
        style = MaterialTheme.editorialTypography.eyebrowSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun VerticalDividerScaffold(
    model: DividerUiModel.Vertical,
    modifier: Modifier,
) {
    val color = model.ink.resolve(
        colorScheme = MaterialTheme.colorScheme,
        contentColor = LocalContentColor.current,
    )
    val length = model.length

    VerticalDivider(
        modifier = if (length != null) modifier.height(length) else modifier.fillMaxHeight(),
        thickness = DIVIDER_THICKNESS,
        color = color,
    )
}
