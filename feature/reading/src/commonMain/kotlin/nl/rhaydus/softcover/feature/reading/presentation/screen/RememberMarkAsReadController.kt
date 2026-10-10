package nl.rhaydus.softcover.feature.reading.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.designsystem.nav.NavPulse
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import org.koin.compose.koinInject

@Composable
internal fun rememberMarkAsReadController(
    books: List<Book>,
    runAction: (ReadingAction) -> Unit,
): MarkAsReadController {
    val haptics = LocalHaptics.current
    val playMotion = playDecorativeMotion()
    val density = LocalDensity.current
    val slideDistancePx = remember(density) { with(density) { 96.dp.toPx() } }
    val navPulse = koinInject<NavPulse>()

    val currentBooks by rememberUpdatedState(books)
    val currentRunAction by rememberUpdatedState(runAction)

    val controller = remember(slideDistancePx, playMotion, haptics, navPulse) {
        MarkAsReadController(
            slideDistancePx = slideDistancePx,
            playMotion = playMotion,
            haptics = haptics,
            navPulse = navPulse,
            runAction = { currentRunAction(it) },
        )
    }

    LaunchedEffect(controller, controller.slidingBookId) {
        controller.runSlide(books = currentBooks)
    }

    return controller
}
