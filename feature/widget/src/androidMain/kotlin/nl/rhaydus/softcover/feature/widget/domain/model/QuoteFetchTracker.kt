package nl.rhaydus.softcover.feature.widget.domain.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import nl.rhaydus.common.runCatchingCancellable

/**
 * In-memory record of the quote fetch the widget started, which Room cannot say: a fetch in flight, or one that
 * failed before writing anything. No state means none running and none that failed since the last success.
 */
internal class QuoteFetchTracker {
    enum class State {
        Running,
        Failed,
    }

    private val current = MutableStateFlow<State?>(null)

    val state: StateFlow<State?> = current.asStateFlow()

    /** Forgets a failure, because it belongs to the signed-out user. A fetch still running is kept. */
    fun clearFailure() {
        current.update { if (it == State.Failed) null else it }
    }

    private var inFlight = 0

    suspend fun track(fetch: suspend () -> Result<Unit>): Result<Unit> {
        synchronized(this) {
            inFlight++
            current.value = State.Running
        }

        var failed = false

        try {
            return runCatchingCancellable { fetch() }
                .getOrElse { Result.failure(it) }
                .also { failed = it.isFailure }
        } finally {
            synchronized(this) {
                inFlight--

                if (inFlight == 0) {
                    current.value = if (failed) State.Failed else null
                }
            }
        }
    }
}
