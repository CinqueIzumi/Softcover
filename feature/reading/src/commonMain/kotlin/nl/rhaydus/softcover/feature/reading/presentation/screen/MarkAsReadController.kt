package nl.rhaydus.softcover.feature.reading.presentation.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import nl.rhaydus.designsystem.nav.NavPulse
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurstScale
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurstUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.util.LibraryNavPulseKey
import nl.rhaydus.softcover.feature.reading.presentation.action.OnMarkBookAsReadClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction

/**
 * Drives the "mark as read" celebration shared by both platform layouts: it owns the slide-out
 * animation state, the confetti [burst], and the bottom-bar pulse, so the
 * `mobileMain` and `jvmMain` `ReadingScreenLayout` actuals only wire it to their own chrome.
 * Obtain one via [rememberMarkAsReadController].
 */
@Stable
internal class MarkAsReadController(
    private val slideDistancePx: Float,
    private val playMotion: Boolean,
    private val navPulse: NavPulse,
    private val runAction: (ReadingAction) -> Unit,
) {
    var burst by mutableStateOf(
        MarkAsReadBurstUiModel(
            triggerKey = 0,
            scale = MarkAsReadBurstScale.Screen,
        ),
    )
        private set

    var slidingBookId by mutableStateOf<Int?>(null)
        private set

    val slideProgress = Animatable(initialValue = 0f)

    // Set when an explicit "mark as read" affordance bursts at the moment of the gesture, so the
    // progress-sheet finish path (which bursts later, off the Applied outcome) can tell that this
    // finish was already celebrated and skip its own burst — keeping a single finish to one burst.
    private var explicitBurstFired = false

    fun celebrate() {
        burst = burst.next()
    }

    /** Reads and clears the "an explicit affordance already burst for the pending finish" flag. */
    fun consumeExplicitBurstFired(): Boolean {
        val fired = explicitBurstFired
        explicitBurstFired = false

        return fired
    }

    // Set alongside slidingBookId so the deferred runSlide() completion below can carry the same
    // backdated instant the immediate (playMotion == false) branch applies right away.
    private var pendingActionAt: String? = null

    fun requestMarkAsRead(
        book: Book,
        actionAt: String? = null,
    ) {
        if (slidingBookId != null) return

        celebrate()
        explicitBurstFired = true
        navPulse.pulse(LibraryNavPulseKey)

        if (playMotion) {
            pendingActionAt = actionAt
            slidingBookId = book.id
        } else {
            runAction(
                OnMarkBookAsReadClickAction(
                    book = book,
                    actionAt = actionAt,
                ),
            )
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
            runAction(
                OnMarkBookAsReadClickAction(
                    book = book,
                    actionAt = pendingActionAt,
                ),
            )
        }

        pendingActionAt = null
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
