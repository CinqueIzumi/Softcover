package nl.rhaydus.softcover.core.component.statistic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.divider.DividerInk
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
fun StatTile(
    model: StatTileUiModel,
    modifier: Modifier = Modifier,
) {
    when (val variant = model.variant) {
        is StatTileVariant.Hero -> HeroTile(
            model = model,
            variant = variant,
            modifier = modifier,
        )

        is StatTileVariant.Standard -> StandardTile(
            model = model,
            variant = variant,
            modifier = modifier,
        )

        is StatTileVariant.Compact -> CompactTile(
            model = model,
            variant = variant,
            modifier = modifier,
        )
    }
}

@Composable
private fun HeroTile(
    model: StatTileUiModel,
    variant: StatTileVariant.Hero,
    modifier: Modifier,
) {
    val typography = MaterialTheme.editorialTypography

    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {},
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(28.dp),
    ) {
        val demoted = LocalContentColor.current.copy(alpha = 0.75f)

        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            SoftcoverText(
                text = model.eyebrow.uppercase(),
                style = typography.eyebrow,
                color = demoted,
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatNumber(
                model = model.stat,
                style = typography.statHero,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 44.sp,
                    maxFontSize = 72.sp,
                    stepSize = 2.sp,
                ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Divider(ink = DividerInk.ContentTint)

            Spacer(modifier = Modifier.height(10.dp))

            SoftcoverText(
                text = variant.caption,
                style = typography.body,
                color = demoted,
            )
        }
    }
}

@Composable
private fun StandardTile(
    model: StatTileUiModel,
    variant: StatTileVariant.Standard,
    modifier: Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.editorialTypography

    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {},
        color = colors.surfaceContainerHigh,
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            SoftcoverText(
                text = model.eyebrow.uppercase(),
                style = typography.eyebrowSmall,
                color = colors.primary,
            )

            Spacer(modifier = Modifier.height(20.dp))

            StatNumber(
                model = model.stat,
                style = typography.statLarge,
            )

            SoftcoverText(
                text = variant.caption,
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CompactTile(
    model: StatTileUiModel,
    variant: StatTileVariant.Compact,
    modifier: Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.editorialTypography

    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {},
        color = colors.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(
                horizontal = 16.dp,
                vertical = 14.dp,
            ),
        ) {
            SoftcoverText(
                text = model.eyebrow.uppercase(),
                style = typography.eyebrowSmall,
                color = colors.primary,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                StatNumber(
                    model = model.stat,
                    style = typography.headlineMedium,
                )

                Spacer(modifier = Modifier.width(6.dp))

                SoftcoverText(
                    text = variant.unit,
                    style = typography.body,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }
    }
}
