package nl.rhaydus.softcover.core.component.snackbar

import androidx.compose.runtime.mutableStateListOf

/**
 * Picks which of the live [SoftcoverSnackbarHost]s draws, and keeps the time the current snackbar
 * was first shown so a host taking over mid-display can finish the remaining time.
 *
 * The topmost host is the one with the highest level; hosts at the same level resolve to the most
 * recently registered. Times are caller-supplied milliseconds on any monotonic clock.
 */
internal class SnackbarHostRegistry {
    private class Entry(
        val token: Any,
        val level: Int,
    )

    private val hosts = mutableStateListOf<Entry>()
    private var shownData: Any? = null
    private var shownAtMillis = 0L

    fun register(
        token: Any,
        level: Int,
    ) {
        hosts.add(
            Entry(
                token = token,
                level = level,
            ),
        )
    }

    fun unregister(token: Any) {
        hosts.removeAll { it.token === token }
    }

    fun isTopmost(token: Any): Boolean {
        val topLevel = hosts.maxOfOrNull { it.level } ?: return false

        return hosts.lastOrNull { it.level == topLevel }?.token === token
    }

    /**
     * The milliseconds of [durationMillis] still left for [data], counted from the first call made
     * with that instance. Never negative.
     */
    fun remainingMillis(
        data: Any,
        durationMillis: Long,
        nowMillis: Long,
    ): Long {
        if (shownData !== data) {
            shownData = data
            shownAtMillis = nowMillis
        }

        return (durationMillis - (nowMillis - shownAtMillis)).coerceAtLeast(0L)
    }
}
