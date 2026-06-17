package nl.rhaydus.softcover.feature.reading.presentation.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.util.BottomBarPulseManager
import nl.rhaydus.softcover.core.designsystem.presentation.util.Haptics
import nl.rhaydus.softcover.core.designsystem.presentation.util.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.util.rememberHaptics
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.action.OnMarkBookAsReadClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction

/**
 * Drives the "mark as read" celebration shared by both platform layouts: it owns the slide-out
 * animation state, the confetti [celebrationKey], and the haptic + bottom-bar pulse, so the
 * `mobileMain` and `jvmMain` `ReadingScreenLayout` actuals only wire it to their own chrome.
 * Obtain one via [rememberMarkAsReadController].
 */
@Stable
internal class MarkAsReadController(
    private val slideDistancePx: Float,
    private val playMotion: Boolean,
    private val haptics: Haptics,
    private val runAction: (ReadingAction) -> Unit,
) {
    var celebrationKey by mutableIntStateOf(0)
        private set

    var slidingBookId by mutableStateOf<Int?>(null)
        private set

    val slideProgress = Animatable(initialValue = 0f)

    fun celebrate() {
        celebrationKey++
    }

    fun requestMarkAsRead(book: Book) {
        if (slidingBookId != null) return

        haptics.commit()
        celebrationKey++
        BottomBarPulseManager.pulseLibrary()

        if (playMotion) {
            slidingBookId = book.id
        } else {
            runAction(OnMarkBookAsReadClickAction(book = book))
        }
    }

    suspend fun runSlide(books: List<Book>) {
        val targetId = slidingBookId ?: return

        slideProgress.snapTo(targetValue = 0f)
        slideProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 320,
                easing = FastOutLinearInEasing,
            ),
        )

        books.firstOrNull { it.id == targetId }?.let { book ->
            runAction(OnMarkBookAsReadClickAction(book = book))
        }

        slideProgress.snapTo(targetValue = 0f)
        slidingBookId = null
    }

    fun slideModifier(bookId: Int): Modifier =
        if (bookId == slidingBookId) {
            Modifier.graphicsLayer {
                translationY = slideProgress.value * slideDistancePx
                alpha = 1f - slideProgress.value
            }
        } else {
            Modifier
        }
}

@Composable
internal fun rememberMarkAsReadController(
    books: List<Book>,
    runAction: (ReadingAction) -> Unit,
): MarkAsReadController {
    val haptics = rememberHaptics()
    val playMotion = playDecorativeMotion()
    val density = LocalDensity.current
    val slideDistancePx = remember(density) { with(density) { 96.dp.toPx() } }

    val currentBooks by rememberUpdatedState(books)
    val currentRunAction by rememberUpdatedState(runAction)

    val controller = remember(slideDistancePx, playMotion, haptics) {
        MarkAsReadController(
            slideDistancePx = slideDistancePx,
            playMotion = playMotion,
            haptics = haptics,
            runAction = { currentRunAction(it) },
        )
    }

    LaunchedEffect(controller, controller.slidingBookId) {
        controller.runSlide(books = currentBooks)
    }

    return controller
}
