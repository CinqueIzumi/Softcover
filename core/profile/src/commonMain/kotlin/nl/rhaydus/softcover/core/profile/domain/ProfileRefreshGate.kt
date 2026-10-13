package nl.rhaydus.softcover.core.profile.domain

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Process-lifetime, in-memory: guards a profile network fetch to at most once per app session.
// Two independent instances are wired (one per Koin qualifier) so the activity half
// (RefreshReadingActivityUseCase) and the stats half (RefreshUserProfileStatsUseCase) can be
// deferred from one another without either blocking or re-triggering the other. A new session
// starts fresh (Koin recreates the singletons on process death), and logout/reset calls reset()
// on both explicitly so the next login re-fetches instead of reusing a stale session flag.
// Public only because it appears in those use cases' public constructors.
class ProfileRefreshGate {
    private val mutex = Mutex()
    private var wasRefreshedThisSession = false

    // Guards check-fetch-mark as one atomic unit: two concurrent refresh calls cannot both see
    // "not yet refreshed" and both run block(). block() only runs while not yet refreshed, and the
    // session is marked refreshed only once block() returns without throwing - a failed fetch
    // leaves the gate open so the next call retries.
    suspend fun runOnce(block: suspend () -> Unit) {
        mutex.withLock {
            if (wasRefreshedThisSession) return@withLock

            block()
            wasRefreshedThisSession = true
        }
    }

    suspend fun reset() {
        mutex.withLock {
            wasRefreshedThisSession = false
        }
    }
}
