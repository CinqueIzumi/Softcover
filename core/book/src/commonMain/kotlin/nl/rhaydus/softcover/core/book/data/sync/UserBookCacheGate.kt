package nl.rhaydus.softcover.core.book.data.sync

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.core.book.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind
import nl.rhaydus.softcover.core.domain.model.Book

/**
 * The path for the repository's optimistic, confirmation, removal and refresh writes of user-book rows;
 * maintenance writes (sign-out wipe, id redirects, orphan cleanup) bypass it. A refresh fetches a server snapshot that is stale by the
 * time it lands, so a write made while it is in flight would be overwritten; the gate records every write
 * made during a fetch and hands those records to the refresh's merge, which re-applies them.
 *
 * One mutex serialises every row write and the refresh's merge plus save, so a write is either fully
 * visible to the merge or lands after the save. The fetch itself runs outside the mutex.
 *
 * The mutex is not reentrant: nothing run while it is held (the `reconcile` and `selectToCache` lambdas of
 * [refresh]) may call back into the gate. The lock order is this mutex first, then any mutex the caller's
 * lambdas take, never the reverse.
 *
 * A write that is later rolled back stays recorded; the next refresh without it in flight heals the row.
 */
internal class UserBookCacheGate(
    private val booksLocalDataSource: BooksLocalDataSource,
) {
    /** Writes and removals seen since one refresh began; read and mutated only while the gate mutex is held. */
    class RefreshWindow {
        private val mutableWrites: MutableMap<Int, Set<PendingUserBookWriteKind>> = mutableMapOf()

        private val mutableRemovedUserBookIds: MutableSet<Int> = mutableSetOf()

        val writes: Map<Int, Set<PendingUserBookWriteKind>> get() = mutableWrites

        val removedUserBookIds: Set<Int> get() = mutableRemovedUserBookIds

        internal fun record(
            userBookId: Int,
            kind: PendingUserBookWriteKind,
        ) {
            mutableWrites[userBookId] = mutableWrites[userBookId].orEmpty() + kind
        }

        internal fun markRemoved(userBookIds: Collection<Int>) {
            mutableRemovedUserBookIds += userBookIds
        }

        internal fun clearRemoved(userBookId: Int) {
            mutableRemovedUserBookIds.remove(userBookId)
        }
    }

    private val mutex = Mutex()

    // A list because refreshes with different status filters overlap.
    private val openWindows: MutableList<RefreshWindow> = mutableListOf()

    /**
     * Writes [book], recording [kind] for its user book in every open window. A null [kind] writes
     * without recording, for redirects, undo restores and rollbacks, which are not user intent.
     */
    suspend fun cacheBook(
        book: Book,
        kind: PendingUserBookWriteKind? = null,
    ) {
        mutex.withLock {
            booksLocalDataSource.cacheBook(book = book)

            val userBookId: Int = book.userBook?.id ?: return@withLock

            openWindows.forEach { window ->
                // A fresh write supersedes a removal recorded earlier in the same window.
                window.clearRemoved(userBookId)

                if (kind != null) {
                    window.record(
                        userBookId = userBookId,
                        kind = kind,
                    )
                }
            }
        }
    }

    /**
     * Records a write without touching the row, for the moment the server confirms it: a write that began
     * before a window opened can be confirmed inside it, and the fetch may predate that confirmation.
     */
    suspend fun recordWrite(
        book: Book,
        kind: PendingUserBookWriteKind,
    ) {
        val userBookId: Int = book.userBook?.id ?: return

        mutex.withLock {
            openWindows.forEach { window ->
                window.record(
                    userBookId = userBookId,
                    kind = kind,
                )
            }
        }
    }

    suspend fun removeUserBooks(ids: List<Int>) {
        mutex.withLock {
            booksLocalDataSource.removeUserBooksById(ids = ids)

            openWindows.forEach { window -> window.markRemoved(ids) }
        }
    }

    /**
     * Opens a window, runs [fetch] outside the mutex, then under the mutex lets [reconcile] fold the
     * window's records into the fetched books and saves what [selectToCache] keeps of the result.
     * The window closes on every exit, including failure and cancellation. Returns the reconciled books.
     */
    suspend fun refresh(
        fetch: suspend () -> List<Book>,
        reconcile: suspend (fetched: List<Book>, window: RefreshWindow) -> List<Book>,
        selectToCache: suspend (List<Book>) -> List<Book>,
    ): List<Book> {
        val window = RefreshWindow()

        mutex.withLock { openWindows += window }

        try {
            val fetched: List<Book> = fetch()

            return mutex.withLock {
                val reconciled: List<Book> = reconcile(
                    fetched,
                    window,
                )

                booksLocalDataSource.cacheBooks(books = selectToCache(reconciled))

                reconciled
            }
        } finally {
            withContext(NonCancellable) {
                mutex.withLock { openWindows -= window }
            }
        }
    }
}
