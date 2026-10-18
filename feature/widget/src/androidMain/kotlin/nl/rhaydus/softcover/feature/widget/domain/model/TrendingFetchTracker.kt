package nl.rhaydus.softcover.feature.widget.domain.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import nl.rhaydus.common.runCatchingCancellable

/**
 * In-memory record of fetches the widget started, which Room cannot say: a fetch in flight, or one that failed
 * before writing anything. A feed with no state has no fetch running and none that failed since it last succeeded.
 */
internal class TrendingFetchTracker {
    enum class State {
        Running,
        Failed,
    }

    private val states = MutableStateFlow<Map<TrendingFeed, State>>(emptyMap())

    fun observe(feed: TrendingFeed): Flow<State?> = states
        .map { it[feed] }
        .distinctUntilChanged()

    /** Forgets failures, because they belong to the signed-out user. Fetches still running are kept. */
    fun clearFailures() {
        states.update { current -> current.filterValues { it == State.Running } }
    }

    private val inFlight = mutableMapOf<TrendingFeed, Int>()

    suspend fun track(
        feed: TrendingFeed,
        fetch: suspend () -> Result<Unit>,
    ): Result<Unit> {
        synchronized(inFlight) {
            inFlight[feed] = (inFlight[feed] ?: 0) + 1
            states.update { it + (feed to State.Running) }
        }

        var failed = false

        try {
            return runCatchingCancellable { fetch() }
                .getOrElse { Result.failure(it) }
                .also { failed = it.isFailure }
        } finally {
            synchronized(inFlight) {
                val remaining = (inFlight[feed] ?: 1) - 1

                if (remaining > 0) {
                    inFlight[feed] = remaining
                } else {
                    inFlight.remove(feed)
                    states.update { if (failed) it + (feed to State.Failed) else it - feed }
                }
            }
        }
    }
}
