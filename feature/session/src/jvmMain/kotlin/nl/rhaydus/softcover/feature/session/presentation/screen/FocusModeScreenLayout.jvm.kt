package nl.rhaydus.softcover.feature.session.presentation.screen

import nl.rhaydus.designsystem.component.DesktopTooltip
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import nl.rhaydus.designsystem.modifier.dismissOnEscape
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.component.DesktopVerticalScrollbar
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSession

// Keeps the reading panel at a comfortable column measure rather than stretching the hero across a
// maximized window.
private val PANEL_MAX_WIDTH = 440.dp

// A larger cover than the phone hero — the desktop window has the room for it.
private val DESKTOP_COVER_WIDTH = 180.dp

/**
 * Desktop focus mode. The same distraction-free reading hero ([FocusReadingPanel]) as mobile, centred
 * in the window at a comfortable column measure with a larger cover, a persistent desktop scrollbar
 * for short windows, and a hover/cursor close affordance. No system-bar or keyboard insets (desktop
 * has neither). The opaque [Surface] keeps the reading surface from letting the screen beneath bleed
 * through during the navigation transition.
 */
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
        modifier = Modifier
            .fillMaxSize()
            .dismissOnEscape(onDismiss = onClose),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val scrollState = rememberScrollState()

            FocusReadingPanel(
                active = active,
                onPauseResume = onPauseResume,
                onStop = onStop,
                onUpdatePage = onUpdatePage,
                coverWidth = DESKTOP_COVER_WIDTH,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = PANEL_MAX_WIDTH)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            )

            DesktopVerticalScrollbar(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
            )

            val closeIcon = drawableIconResource(
                icon = SoftcoverIcon.Close,
                contentDescription = "Close focus mode",
            )

            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                DesktopTooltip(text = "Close") {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .padding(8.dp)
                            .pointerHandCursor(),
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
    }
}
