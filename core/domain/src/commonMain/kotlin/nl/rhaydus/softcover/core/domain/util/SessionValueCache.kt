package nl.rhaydus.softcover.core.domain.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Process-lifetime, in-memory cache of one value per key - the same shape as core/profile's
// ProfileRefreshGate once-per-session guard, generalized to values that vary by a key (e.g. per
// genre) instead of a single flag. A new session starts every key empty (Koin recreates the
// singleton on process death); TTL and persistence are a deliberate non-goal - editorial content
// that turns over at most daily does not need either.
//
// Locking is PER KEY, not per cache. Concurrent callers for the same key collapse to one load: the
// second waits on that key's lock and then finds the first caller's stored value instead of
// re-fetching. Concurrent callers for *different* keys do not block each other at all - a class-wide
// lock held across the suspending load would serialize them, and with RateLimitInterceptor's token
// wait and the read retry's backoff sitting inside that load, a single class-wide lock could stall an
// unrelated key for seconds. That would surface as an unexplained hang rather than a wrong value,
// which is exactly the kind of thing nobody diagnoses later.
//
// `registryMutex` guards only the two maps and is never held across [load], so it is uncontended
// bookkeeping; the per-key lock is the one that spans the fetch.
//
// Caches on success only: [load] returning normally is what fills a key, and a thrown exception
// propagates to the caller with nothing stored, so the next call for that key retries. This is the
// property that makes it safe to wrap any fetch in - a transient failure (a 429, a dropped
// connection) never sticks and blanks a shelf for the rest of the session.
class SessionValueCache<K, V> {
    private val registryMutex = Mutex()
    private val values = mutableMapOf<K, V>()
    private val loadLocks = mutableMapOf<K, Mutex>()

    suspend fun getOrPut(
        key: K,
        load: suspend () -> V,
    ): V {
        // containsKey rather than a null check, so a load that legitimately resolves to null (an
        // absent featured release, say) counts as settled and is not re-fetched every call.
        readCached(key) { return it }

        val loadLock: Mutex = registryMutex.withLock { loadLocks.getOrPut(key) { Mutex() } }

        return loadLock.withLock {
            // Re-check under the key's lock: a caller that queued behind the loader must take its
            // value rather than repeat the fetch.
            readCached(key) { return@withLock it }

            val value = load()

            registryMutex.withLock {
                values[key] = value

                // Dropping the lock once the value is stored keeps the map from growing without
                // bound over a long session. Safe while still holding it: a caller already waiting on
                // this same instance re-checks and finds the value, and a caller arriving afterwards
                // never needs a lock because the fast path above now hits.
                loadLocks.remove(key)
            }

            value
        }
    }

    /**
     * Loads unconditionally and replaces any cached value for [key], for the case where a *person* has
     * asked for fresh data — a pull-to-refresh must not be answered from a session cache, or it looks
     * broken: the spinner turns, nothing changes, and no error is shown.
     *
     * Takes the same per-key lock as [getOrPut], so a refresh and a concurrent read of the same key do
     * not both fetch, and replaces the stored value only on success — a failed refresh leaves the
     * previous value in place rather than emptying the shelf.
     */
    suspend fun refresh(
        key: K,
        load: suspend () -> V,
    ): V {
        val loadLock: Mutex = registryMutex.withLock { loadLocks.getOrPut(key) { Mutex() } }

        return loadLock.withLock {
            val value = load()

            registryMutex.withLock {
                values[key] = value
                loadLocks.remove(key)
            }

            value
        }
    }

    // Inlined so `onHit` can return from the caller, which is what lets both call sites above skip
    // the load without duplicating the containsKey/getValue pair.
    private suspend inline fun readCached(
        key: K,
        onHit: (V) -> Unit,
    ) {
        registryMutex.withLock {
            if (values.containsKey(key)) onHit(values.getValue(key))
        }
    }
}

// Convenience for the common case of a cache with a single, implicit slot.
suspend fun <V> SessionValueCache<Unit, V>.getOrPut(load: suspend () -> V): V = getOrPut(
    key = Unit,
    load = load,
)

// Convenience for the single-slot case — see SessionValueCache.refresh.
suspend fun <V> SessionValueCache<Unit, V>.refresh(load: suspend () -> V): V = refresh(
    key = Unit,
    load = load,
)
