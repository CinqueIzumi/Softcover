package nl.rhaydus.softcover.core.domain.connectivity

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Serialises direct sends and queue replays of the same book. Locks are per `bookId`; different books
 * never contend. Entries are kept for the process lifetime.
 *
 * Lock order: the drainer takes its own mutex first, then this lock. A holder may take the cache gate's
 * mutex inside it, but must never call the drainer's `drain()` or the gate's `refresh` inside it.
 */
class UserBookWriteLock {
    private val registryMutex = Mutex()
    private val locks = mutableMapOf<Int, Mutex>()

    suspend fun <T> withLock(
        bookId: Int,
        block: suspend () -> T,
    ): T {
        val mutex: Mutex = registryMutex.withLock {
            locks.getOrPut(bookId) { Mutex() }
        }

        return mutex.withLock { block() }
    }
}
