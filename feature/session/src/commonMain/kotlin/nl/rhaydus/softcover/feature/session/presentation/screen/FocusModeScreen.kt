package nl.rhaydus.softcover.feature.session.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSession
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSessionController

/**
 * Distraction-free full-screen reading surface for the active session. An editorial hero on the page
 * surface (§3.3): eyebrow → cover → title → the running timer as a `statHero` hero stat → progress →
 * a quick page-update field → pause/resume + stop. Reads the shared [ActiveSessionController] directly
 * and pops itself the moment no session is active. The mobile actual sizes the hero to one phone
 * screen (system-bar + keyboard insets, compact cover); the desktop actual centres a wider reading
 * panel with a larger cover. The shared hero is [FocusReadingPanel].
 */
object FocusModeScreen : Screen {
    override val key: String = "focus-mode"

    @Composable
    override fun Content() {
        val controller = koinInject<ActiveSessionController>()
        val navigator = LocalNavigator.currentOrThrow
        val active by controller.activeSession.collectAsStateWithLifecycle()

        val current = active

        if (current == null) {
            navigator.pop()

            return
        }

        FocusModeScreenLayout(
            active = current,
            onPauseResume = {
                if (current.session.isPaused) controller.resume() else controller.pause()
            },
            onStop = {
                controller.stop()

                navigator.pop()
            },
            onUpdatePage = { controller.updatePage(newPage = it) },
            onClose = { navigator.pop() },
        )
    }
}

// The mobile actual sizes the reading hero to one phone screen (system-bar + keyboard insets); the
// desktop actual centres a wider reading panel with a larger cover and a desktop scrollbar. Both
// render the shared [FocusReadingPanel]. No default arguments — they are not allowed on an expect
// declaration, so every argument is supplied explicitly at the single call site above.
@Composable
internal expect fun FocusModeScreenLayout(
    active: ActiveSession,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    onUpdatePage: (Int) -> Unit,
    onClose: () -> Unit,
)
