package nl.rhaydus.softcover.core.connectivity.data.sync

import nl.rhaydus.offlinesync.ReplayOutcome
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWriteKind
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.core.lists.data.datasource.ListsRemoteDataSource

/**
 * Replays a single queued list mutation and caches the server's response. Always [ReplayOutcome.SYNCED] on
 * return: a list write that cannot be reconstructed throws, and the drainer classifies every list failure
 * as transient (unlike user-book writes, a rejected list write is retried rather than discarded).
 */
internal class ListWriteReplay(
    private val listsRemoteDataSource: ListsRemoteDataSource,
    private val listsLocalDataSource: ListsLocalDataSource,
) {
    suspend operator fun invoke(payload: PendingListWrite): ReplayOutcome {
        when (payload.kind) {
            PendingListWriteKind.CREATE_LIST -> replayCreateList(write = payload)
            PendingListWriteKind.ADD_LIST_BOOK -> replayAddListBook(write = payload)
            PendingListWriteKind.REMOVE_LIST_BOOK -> replayRemoveListBook(write = payload)
            PendingListWriteKind.REORDER_LIST_BOOKS -> replayReorderListBooks(write = payload)
        }

        return ReplayOutcome.SYNCED
    }

    private suspend fun replayCreateList(write: PendingListWrite) {
        val name: String = write.listName ?: error("CREATE_LIST replay missing listName")

        // Rows queued before this column existed (or optimistically written pre-migration) carry no
        // privacy choice — fall back to PUBLIC, today's pre-existing behaviour, rather than dropping
        // the replay.
        val privacy: PrivacySetting = write.privacy ?: PrivacySetting.PUBLIC

        val created: BookList = listsRemoteDataSource.createList(
            name = name,
            privacy = privacy,
        )

        listsLocalDataSource.cacheUserBookLists(lists = listOf(created))
    }

    private suspend fun replayAddListBook(write: PendingListWrite) {
        val listId: Int = write.listId ?: error("ADD_LIST_BOOK replay missing listId")
        val bookId: Int = write.bookId ?: error("ADD_LIST_BOOK replay missing bookId")
        val editionId: Int = write.editionId ?: error("ADD_LIST_BOOK replay missing editionId")

        val real: ListBook = listsRemoteDataSource.addBookToList(
            listId = listId,
            bookId = bookId,
            editionId = editionId,
        )

        listsLocalDataSource.removeOptimisticListBook(
            listId = listId,
            bookId = bookId,
        )
        listsLocalDataSource.cacheListBook(book = real)
    }

    private suspend fun replayRemoveListBook(write: PendingListWrite) {
        val listId: Int = write.listId ?: error("REMOVE_LIST_BOOK replay missing listId")
        val listBookId: Int =
            write.listBookId ?: error("REMOVE_LIST_BOOK replay missing listBookId")
        val bookId: Int = write.bookId ?: error("REMOVE_LIST_BOOK replay missing bookId")
        val editionId: Int = write.editionId ?: error("REMOVE_LIST_BOOK replay missing editionId")

        val snapshot = ListBook(
            listBookId = listBookId,
            listId = listId,
            bookId = bookId,
            editionId = editionId,
        )

        val updatedList: BookList = listsRemoteDataSource.removeListBook(book = snapshot)

        listsLocalDataSource.cacheUserBookLists(lists = listOf(updatedList))
    }

    private suspend fun replayReorderListBooks(write: PendingListWrite) {
        val listId: Int = write.listId ?: error("REORDER_LIST_BOOKS replay missing listId")
        val startPosition: Int =
            write.startPosition ?: error("REORDER_LIST_BOOKS replay missing startPosition")
        val ordered: List<Int> = write.orderedListBookIds
            ?.takeIf { it.isNotEmpty() }
            ?: error("REORDER_LIST_BOOKS replay missing orderedListBookIds")

        listsRemoteDataSource.updateListBookPositions(
            listId = listId,
            startPosition = startPosition,
            orderedListBookIds = ordered,
        )
    }
}
