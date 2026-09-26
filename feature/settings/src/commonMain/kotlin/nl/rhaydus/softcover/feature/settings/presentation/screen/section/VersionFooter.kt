package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Clock
import nl.rhaydus.designsystem.haptics.rememberHaptics
import nl.rhaydus.designsystem.modifier.noRippleClickable
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.settings.presentation.util.SecretTapCounter

/**
 * The quiet, tabular-numeral build string, centred between two `outlineVariant` hairlines so it reads
 * as a plain closing rule rather than a row.
 *
 * [onSecretUnlocked] backs the Component Gallery easter egg (`component-contract.md` § 7.5): a
 * [SecretTapCounter] counts taps on this row, and on its [SecretTapCounter.registerTap]'s seventh (each
 * within its reset window of the last) fires a `milestone` haptic and calls [onSecretUnlocked]. The row is
 * wrapped in [noRippleClickable] rather than
 * [pressScaleClickable][nl.rhaydus.designsystem.modifier.pressScaleClickable] or a plain `clickable`
 * deliberately — the footer must look exactly as it does today, with no ripple, no hand cursor, and no
 * press scale hinting that anything here is interactive. Nothing in this composable's rendered output
 * changes because of this parameter.
 */
@Composable
internal fun VersionFooter(
    versionName: String,
    versionCode: Int,
    onSecretUnlocked: () -> Unit,
) {
    val tapCounter = remember { SecretTapCounter() }
    val haptics = rememberHaptics()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable {
                if (tapCounter.registerTap(at = Clock.System.now())) {
                    haptics.milestone()
                    onSecretUnlocked()
                }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        Text(
            text = "Version $versionName ($versionCode)",
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
