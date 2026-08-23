package nl.rhaydus.softcover.feature.settings.presentation.util

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private const val REQUIRED_TAPS = 7
private val RESET_WINDOW: Duration = 2.seconds

/**
 * Counts taps for the version footer's easter egg (`component-contract.md` § 7.5): seven taps
 * within two seconds of each other unlocks the Component Gallery.
 *
 * A plain class rather than Compose state, because the logic is worth unit-testing on its own and
 * this repo has no Compose UI test harness — the gesture's timing edge cases (a tap right at the
 * window boundary, a broken run restarting at 1) are exactly the kind of thing that should be
 * covered by a fast unit test rather than a manual gesture check.
 */
internal class SecretTapCounter(
    private val requiredTaps: Int = REQUIRED_TAPS,
    private val resetWindow: Duration = RESET_WINDOW,
) {
    private var tapCount = 0
    private var lastTapAt: Instant? = null

    /**
     * Registers a tap at [at]. Returns `true` exactly on the [requiredTaps]-th tap of a run, where a
     * run continues only while consecutive taps are at most [resetWindow] apart — a longer gap
     * starts the count over at 1, since the late tap is itself the first tap of the new run.
     * Unlocking resets the counter, so a second run of [requiredTaps] unlocks again.
     */
    fun registerTap(at: Instant): Boolean {
        val previousTapAt = lastTapAt

        tapCount = if (previousTapAt != null && at - previousTapAt <= resetWindow) tapCount + 1 else 1
        lastTapAt = at

        val unlocked = tapCount >= requiredTaps

        if (unlocked) {
            tapCount = 0
            lastTapAt = null
        }

        return unlocked
    }
}
