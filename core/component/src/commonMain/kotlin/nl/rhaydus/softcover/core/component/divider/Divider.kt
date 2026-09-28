package nl.rhaydus.softcover.core.component.divider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/** A hairline rule, bare or broken by a centred glyph or label. */
@Composable
fun Divider(
    model: DividerUiModel,
    modifier: Modifier = Modifier,
) {
    when (val variant = model.variant) {
        DividerVariant.DebugRoutesRow -> DebugRoutesRowDivider(modifier)
        DividerVariant.ReadingLifeShareCard -> ReadingLifeShareCardDivider(modifier)
        DividerVariant.SettingsRoadmap -> SettingsRoadmapDivider(modifier)
        DividerVariant.CreateListQuote -> CreateListQuoteDivider(modifier)
        is DividerVariant.OnboardingOrTypeIt -> OnboardingOrTypeItDivider(
            label = variant.label,
            modifier = modifier,
        )
    }
}

@Composable
private fun DebugRoutesRowDivider(modifier: Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun ReadingLifeShareCardDivider(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LocalContentColor.current.copy(alpha = 0.25f)),
    )
}

@Composable
private fun SettingsRoadmapDivider(modifier: Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 1.dp,
    )
}

/**
 * The glyph's `lineHeight` is pinned to its `fontSize` so the rule stays a thin row instead of
 * reserving `quoteGlyph`'s display-scale line box; `offset(y = 6.dp)` corrects the residual baseline.
 */
@Composable
private fun CreateListQuoteDivider(modifier: Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = "“",
            style = MaterialTheme.editorialTypography.quoteGlyph.copy(
                fontSize = 40.sp,
                lineHeight = 40.sp,
            ),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
            modifier = Modifier.offset(y = 6.dp),
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun OnboardingOrTypeItDivider(
    label: String,
    modifier: Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        Text(
            text = label.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                fontSize = 10.sp,
                lineHeight = 14.sp,
                letterSpacing = 2.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
