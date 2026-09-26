package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.AppUpdateState

/**
 * The Settings screen's one tinted surface: a `primaryContainer` editorial highlight, led by the accent-bar
 * + eyebrow ceremony (hand-composed rather than
 * [EditorialSectionHeader][nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader], since the
 * headline needs `onPrimaryContainer` rather than that component's fixed `onSurface`). No badge, no
 * chevron, no alert chrome — the state's action is the pill button (or, while downloading, an indeterminate
 * wavy progress bar; the client has no reliable percentage to show). Headlines and body copy are
 * deliberately version-less: [appUpdateState] carries no version string or download percent.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun AppUpdateSection(
    appUpdateState: AppUpdateState,
    onClick: () -> Unit,
) {
    val eyebrow = when (appUpdateState) {
        AppUpdateState.Downloading -> "Downloading"
        AppUpdateState.Downloaded -> "Ready to install"
        AppUpdateState.Failed -> "Update failed"
        else -> "Update available"
    }

    val headline = when (appUpdateState) {
        AppUpdateState.Downloading -> "Bringing the update down"
        AppUpdateState.Downloaded -> "The update is ready"
        AppUpdateState.Failed -> "That didn't go through"
        else -> "A new version is ready"
    }

    val body = when (appUpdateState) {
        AppUpdateState.Downloading -> "You can keep reading — it'll finish in the background."
        AppUpdateState.Downloaded -> "Downloaded and waiting. Softcover will restart once."
        AppUpdateState.Failed -> "Tap to try again."
        else -> "A newer Softcover is ready whenever you are."
    }

    val buttonLabel = when (appUpdateState) {
        AppUpdateState.Downloaded -> "Install & restart"
        AppUpdateState.Failed -> "Try again"
        else -> "Download update"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 20.dp,
                    bottom = 22.dp,
                ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .width(30.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = headline,
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = body,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            if (appUpdateState == AppUpdateState.Downloading) {
                Spacer(modifier = Modifier.height(14.dp))

                LinearWavyProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))

                UpdatePillButton(
                    label = buttonLabel,
                    onClick = onClick,
                )
            }
        }
    }
}

/**
 * The update card's fully-rounded call to action. Hand-rolled (rather than
 * [RhaydusButton][nl.rhaydus.designsystem.component.RhaydusButton]) so the pill is guaranteed fully rounded
 * at any label width — the same `Surface(onClick, shape = percent(50))` shape already used for
 * [Chip][nl.rhaydus.softcover.core.component.chip.Chip] and the Library control-line pills.
 */
@Composable
private fun UpdatePillButton(
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(percent = 50),
        modifier = Modifier
            .height(40.dp)
            .pointerHandCursor(),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.editorialTypography.titleSmall,
            )
        }
    }
}
