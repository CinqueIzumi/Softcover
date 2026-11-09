package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.skeletonMask
import nl.rhaydus.designsystem.theme.Corners
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController
import org.koin.compose.koinInject

@Composable
internal fun FeaturedSessionButton(
    bookId: Int,
    isAudiobook: Boolean,
    onStartSession: () -> Unit,
    isLoading: Boolean = false,
) {
    val controller = koinInject<ActiveSessionController>()
    val navigator = LocalNavigator.currentOrThrow
    val appNavigator = koinInject<AppNavigator>()
    val active by controller.activeSession.collectAsStateWithLifecycle()

    val isSessionActiveForBook = isLoading.not() && active?.book?.id == bookId

    when {
        isSessionActiveForBook -> {
            Spacer(modifier = Modifier.height(Spacing.sm))

            // Kept tonal (rather than the idle state's outlined pill) so the active-session state
            // still reads with a touch more weight than a plain "start" affordance.
            RhaydusButton(
                label = "Focus mode",
                style = ButtonStyle.TONAL,
                size = ButtonSize.M,
                icon = drawableIconResource(
                    icon = SoftcoverIcon.Reading,
                    contentDescription = "Focus mode",
                ),
                onClick = {
                    navigator.parent?.push(item = appNavigator.screen(ScreenDestination.FocusMode))
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Audiobooks are tracked by listening time, not page-based reading sessions, so
        // the start control is never offered for them.
        isLoading.not() && isAudiobook -> Unit

        isLoading || active == null -> {
            Spacer(modifier = Modifier.height(Spacing.sm))

            RhaydusButton(
                label = "Start reading session",
                style = ButtonStyle.OUTLINED,
                size = ButtonSize.M,
                icon = drawableIconResource(
                    icon = SoftcoverIcon.Play,
                    contentDescription = "Start reading session",
                ),
                onClick = {
                    if (isLoading.not()) {
                        onStartSession()
                    }
                },
                enabled = isLoading.not(),
                modifier = Modifier
                    .fillMaxWidth()
                    .skeletonMask(
                        visible = isLoading,
                        shape = Corners.xs,
                    ),
            )
        }
    }
}
