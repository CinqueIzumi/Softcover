package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.callout.Callout
import nl.rhaydus.softcover.core.component.callout.CalloutUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.settings.presentation.screen.handleAppUpdateCalloutEvent

/**
 * The tail of the master–detail `About` category, rendered below the shared `AboutContent`: the
 * app-update card and the debug section.
 */
@Composable
internal fun AboutPane(
    appUpdateCallout: CalloutUiModel?,
    onStartAppUpdate: () -> Unit,
    debugSection: @Composable () -> Unit,
) {
    Spacer(modifier = Modifier.height(Spacing.x4l))

    if (appUpdateCallout != null) {
        Callout(
            model = appUpdateCallout,
            onEvent = { event ->
                handleAppUpdateCalloutEvent(
                    event = event,
                    onStartAppUpdate = onStartAppUpdate,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Spacing.x4l))
    }

    debugSection()
}
