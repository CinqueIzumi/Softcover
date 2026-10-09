package nl.rhaydus.softcover.core.component.divider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider as MaterialVerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.sp
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/** A hairline rule, bare or broken by a centred glyph or label. */
@Composable
fun Divider(
    modifier: Modifier = Modifier,
    ink: DividerInk = DividerInk.Outline,
    center: DividerCenter? = null,
) {
    val color = ink.resolve(
        colorScheme = MaterialTheme.colorScheme,
        contentColor = LocalContentColor.current,
    )

    if (center == null) {
        HorizontalDivider(
            modifier = modifier,
            color = color,
        )
        return
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(
            color = color,
            modifier = Modifier.weight(1f),
        )

        DividerCenterContent(center = center)

        HorizontalDivider(
            color = color,
            modifier = Modifier.weight(1f),
        )
    }
}

/** A vertical hairline rule; it fills the available height unless the caller sets one. */
@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    ink: DividerInk = DividerInk.Outline,
) {
    val color = ink.resolve(
        colorScheme = MaterialTheme.colorScheme,
        contentColor = LocalContentColor.current,
    )

    MaterialVerticalDivider(
        modifier = modifier,
        color = color,
    )
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
            .offset(y = Spacing.xs)
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
