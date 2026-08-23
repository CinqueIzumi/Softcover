package nl.rhaydus.softcover.feature.session.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.presentation.session.ActiveSession

@Composable
internal actual fun FocusModeScreenLayout(
    active: ActiveSession,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    onUpdatePage: (Int) -> Unit,
    onClose: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize(),
    ) {
        // The activity uses windowSoftInputMode=adjustNothing, so the OS never pans/resizes for the
        // keyboard — Compose's imePadding is the single keyboard-avoidance inset. The Box centres a
        // wrap-height scrolling Column (centred when it fits, scrolls otherwise).
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding(),
        ) {
            FocusReadingPanel(
                active = active,
                onPauseResume = onPauseResume,
                onStop = onStop,
                onUpdatePage = onUpdatePage,
                coverWidth = 124.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .cappedContentWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            )

            val closeIcon = drawableIconResource(
                icon = SoftcoverIcon.Close,
                contentDescription = "Close focus mode",
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            ) {
                Icon(
                    painter = closeIcon.getIconPainter(),
                    contentDescription = closeIcon.contentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
