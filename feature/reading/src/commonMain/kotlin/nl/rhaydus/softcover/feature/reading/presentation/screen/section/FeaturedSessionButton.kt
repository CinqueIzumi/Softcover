package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.haptics.rememberHaptics
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.notification.rememberNotificationPermissionRequester
import nl.rhaydus.softcover.core.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController

@Composable
internal fun FeaturedSessionButton(
    book: Book,
    foreground: Color,
) {
    val controller = koinInject<ActiveSessionController>()
    val navigator = LocalNavigator.currentOrThrow
    val appNavigator = koinInject<AppNavigator>()
    val haptics = rememberHaptics()
    val active by controller.activeSession.collectAsStateWithLifecycle()

    val currentBook by rememberUpdatedState(book)

    // The lock-screen surface is a plain notification, so it needs POST_NOTIFICATIONS (Android
    // 13+). Ask at the natural moment — the first session start — then start regardless of the
    // outcome, since the in-app peek bar and Focus Mode work without the permission.
    val sessionPermissionRequester = rememberNotificationPermissionRequester(
        onResult = { controller.start(book = currentBook) },
    )

    when {
        active?.book?.id == book.id -> {
            Spacer(modifier = Modifier.height(10.dp))

            // Kept tonal (rather than the idle state's outlined pill) so the active-session state
            // still reads with a touch more weight than a plain "start" affordance.
            RhaydusButton(
                label = "Focus mode",
                style = ButtonStyle.TONAL,
                size = ButtonSize.M,
                icon = drawableIconResource(
                    icon = SoftcoverIcon.Reading,
                    contentDescription = "Focus mode icon",
                ),
                onClick = {
                    navigator.parent?.push(item = appNavigator.screen(ScreenDestination.FocusMode))
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Audiobooks are tracked by listening time, not page-based reading sessions, so
        // the start control is never offered for them.
        book.currentEdition?.isAudiobook == true -> Unit

        active == null -> {
            Spacer(modifier = Modifier.height(10.dp))

            // The catalog's OUTLINED style hardcodes its border and label colour to the theme's
            // `outline` / `onSurface` roles, tuned for a plain surface — not reliably visible now
            // that the button sits on the featured card's blurred-cover backdrop. A scoped
            // ColorScheme override (shapes/typography still inherited) keeps the border and label
            // legible against the image regardless of theme, without a bespoke button component.
            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(
                    outline = foreground,
                    onSurface = foreground,
                ),
            ) {
                RhaydusButton(
                    label = "Start reading session",
                    style = ButtonStyle.OUTLINED,
                    size = ButtonSize.M,
                    icon = drawableIconResource(
                        icon = SoftcoverIcon.Play,
                        contentDescription = "Start reading session icon",
                    ),
                    onClick = {
                        haptics.threshold()

                        sessionPermissionRequester.request()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
