package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.divider.DividerCenter

/** The version rule at the foot of the About screen. */
@Composable
internal fun VersionFooter(versionLabel: String) {
    Divider(
        center = DividerCenter.Label(text = versionLabel),
        modifier = Modifier.fillMaxWidth(),
    )
}
