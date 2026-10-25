package nl.rhaydus.softcover.core.book.data.sync

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.core.book.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.core.book.data.datasource.UserTagsLocalDataSource
import nl.rhaydus.softcover.core.book.domain.model.UserBooksSnapshot
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserTag

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
    private val userTagsLocalDataSource: UserTagsLocalDataSource,
) {
    /** Writes, removals and tag writes seen since one refresh began; read and mutated only while the gate mutex is held. */
    class RefreshWindow {
        private val mutableWrites: MutableMap<Int, Set<PendingUserBookWriteKind>> = mutableMapOf()

        private val mutableRemovedUserBookIds: MutableSet<Int> = mutableSetOf()

        private val mutableTaggedBookIds: MutableSet<Int> = mutableSetOf()

        val writes: Map<Int, Set<PendingUserBookWriteKind>> get() = mutableWrites

        val removedUserBookIds: Set<Int> get() = mutableRemovedUserBookIds

        val taggedBookIds: Set<Int> get() = mutableTaggedBookIds

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

        internal fun markTagged(bookId: Int) {
            mutableTaggedBookIds += bookId
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

    /** Replaces a book's own tags, marking the book tagged in every open window so a stale fetch skips it. */
    suspend fun writeUserTags(
        bookId: Int,
        rows: List<UserTag>,
    ) {
        mutex.withLock {
            userTagsLocalDataSource.replaceForBook(
                bookId = bookId,
                tags = rows,
            )

            openWindows.forEach { window -> window.markTagged(bookId) }
        }
    }

    /**
     * Puts [previous] back for a failed save, but only while the book still holds exactly [written]; any
     * other content came from a later write or refresh and must stay.
     */
    suspend fun restoreUserTags(
        bookId: Int,
        written: List<UserTag>,
        previous: List<UserTag>,
    ) {
        mutex.withLock {
            val current: Set<UserTag> = userTagsLocalDataSource
                .getForBook(bookId = bookId)
                .toSet()

            if (current != written.withoutCounts()) return@withLock

            userTagsLocalDataSource.replaceForBook(
                bookId = bookId,
                tags = previous,
            )

            openWindows.forEach { window -> window.markTagged(bookId) }
        }
    }

    /**
     * Opens a window, runs [fetch] outside the mutex, then under it stores the result unless
     * [writeUserTags] touched the book meanwhile, since the fetch would overwrite that newer write.
     */
    suspend fun refreshUserTags(
        bookId: Int,
        fetch: suspend () -> List<UserTag>,
    ) {
        val window = RefreshWindow()

        mutex.withLock { openWindows += window }

        try {
            val fetched: List<UserTag> = fetch()

            mutex.withLock {
                if (bookId in window.taggedBookIds) return@withLock

                userTagsLocalDataSource.replaceForBook(
                    bookId = bookId,
                    tags = fetched,
                )
            }
        } finally {
            withContext(NonCancellable) {
                mutex.withLock { openWindows -= window }
            }
        }
    }

    private fun List<UserTag>.withoutCounts(): Set<UserTag> = map { it.copy(count = 0) }.toSet()

    /**
     * Opens a window, runs [fetch] outside the mutex, then under the mutex lets [reconcile] fold the
     * window's records into the fetched books and saves what [selectToCache] keeps of the result, along
     * with the snapshot's tags for those books except any tagged during the fetch.
     * The window closes on every exit, including failure and cancellation. Returns the reconciled books.
     */
    suspend fun refresh(
        fetch: suspend () -> UserBooksSnapshot,
        reconcile: suspend (fetched: List<Book>, window: RefreshWindow) -> List<Book>,
        selectToCache: suspend (List<Book>) -> List<Book>,
    ): List<Book> {
        val window = RefreshWindow()

        mutex.withLock { openWindows += window }

        try {
            val fetched: UserBooksSnapshot = fetch()

            return mutex.withLock {
                val reconciled: List<Book> = reconcile(
                    fetched.books,
                    window,
                )
                val toCache: List<Book> = selectToCache(reconciled)

                booksLocalDataSource.cacheBooks(books = toCache)

                userTagsLocalDataSource.replaceForBooks(
                    bookIds = toCache.map { it.id }.toSet() - window.taggedBookIds,
                    tagsByBookId = fetched.userTagsByBookId,
                )

                reconciled
            }
        } finally {
            withContext(NonCancellable) {
                mutex.withLock { openWindows -= window }
            }
        }
    }
}
