package nl.rhaydus.softcover.core.database.dao

import android.content.Context
import androidx.room.Room
import app.cash.turbine.test
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.model.PendingQuoteWriteEntity
import nl.rhaydus.softcover.core.database.model.QuoteEntity
import nl.rhaydus.softcover.core.database.model.QuoteSyncState
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuoteDaoTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: SoftcoverDatabase
    private lateinit var dao: QuoteDao
    private lateinit var pendingDao: PendingQuoteWriteDao

    @BeforeEach
    fun setUp() {
        val context: Context = mockk(relaxed = true)

        database = SoftcoverDatabase.build(
            builder = Room.inMemoryDatabaseBuilder(context = context),
            queryContext = testDispatcher,
        )
        dao = database.quoteDao()
        pendingDao = database.pendingQuoteWriteDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    private fun quote(
        bookId: Int = 1,
        remoteId: Long? = null,
        text: String = "text",
        actionAt: Long = 100L,
        editionId: Int? = null,
        state: QuoteSyncState = QuoteSyncState.SYNCED,
        bookTitle: String? = null,
    ): QuoteEntity = QuoteEntity(
        remoteId = remoteId,
        bookId = bookId,
        editionId = editionId,
        text = text,
        privacySettingId = 1,
        actionAtEpochMillis = actionAt,
        positionType = null,
        positionValue = null,
        positionPossible = null,
        positionPercent = null,
        syncState = state,
        bookTitle = bookTitle,
    )

    private suspend fun observed(bookId: Int): List<QuoteEntity> {
        var result: List<QuoteEntity> = emptyList()

        dao.observeForBook(bookId).test {
            result = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        return result
    }

    private suspend fun observedAll(): List<QuoteEntity> {
        var result: List<QuoteEntity> = emptyList()

        dao.observeAll().test {
            result = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        return result
    }

    @Nested
    inner class ObserveAll {
        @Test
        fun `hides pending deletes and orders newest first across books`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.insertQuote(
                quote(
                    bookId = 1,
                    remoteId = 1,
                    text = "old",
                    actionAt = 10,
                ),
            )
            dao.insertQuote(
                quote(
                    bookId = 2,
                    remoteId = 2,
                    text = "new",
                    actionAt = 30,
                ),
            )
            dao.insertQuote(
                quote(
                    bookId = 3,
                    remoteId = 3,
                    text = "gone",
                    actionAt = 20,
                    state = QuoteSyncState.PENDING_DELETE,
                ),
            )

            // ----- Act -----
            val result = observedAll()

            // ----- Assert -----
            result.map { it.text } shouldBe listOf("new", "old")
        }

        @Test
        fun `round-trips the book title`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.insertQuote(
                quote(
                    remoteId = 1,
                    text = "titled",
                    bookTitle = "Dune",
                ),
            )
            dao.insertQuote(
                quote(
                    remoteId = 2,
                    text = "untitled",
                    actionAt = 50,
                ),
            )

            // ----- Act -----
            val result = observedAll()

            // ----- Assert -----
            result.associate { it.text to it.bookTitle } shouldBe mapOf(
                "titled" to "Dune",
                "untitled" to null,
            )
        }
    }

    @Nested
    inner class ObserveForBook {
        @Test
        fun `hides pending deletes and orders newest first`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.insertQuote(
                quote(
                    remoteId = 1,
                    text = "old",
                    actionAt = 10,
                ),
            )
            dao.insertQuote(
                quote(
                    remoteId = 2,
                    text = "new",
                    actionAt = 30,
                ),
            )
            dao.insertQuote(
                quote(
                    remoteId = 3,
                    text = "gone",
                    actionAt = 20,
                    state = QuoteSyncState.PENDING_DELETE,
                ),
            )
            dao.insertQuote(
                quote(
                    bookId = 2,
                    remoteId = 4,
                    text = "other",
                    actionAt = 40,
                ),
            )

            // ----- Act -----
            val result = observed(bookId = 1)

            // ----- Assert -----
            result.map { it.text } shouldBe listOf("new", "old")
        }
    }

    @Nested
    inner class ReplaceSyncedForBook {
        @Test
        fun `swaps the book's synced rows for the remote rows`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.insertQuote(
                quote(
                    remoteId = 1,
                    text = "stale",
                ),
            )

            // ----- Act -----
            dao.replaceSyncedForBook(
                bookId = 1,
                remote = listOf(
                    quote(
                        remoteId = 2,
                        text = "fresh",
                    ),
                ),
            )

            // ----- Assert -----
            observed(bookId = 1).map { it.text } shouldBe listOf("fresh")
        }

        @Test
        fun `leaves other books and unsynced rows alone`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.insertQuote(
                quote(
                    bookId = 2,
                    remoteId = 9,
                    text = "other book",
                ),
            )
            dao.insertQuote(
                quote(
                    text = "local",
                    state = QuoteSyncState.PENDING_INSERT,
                ),
            )

            // ----- Act -----
            dao.replaceSyncedForBook(
                bookId = 1,
                remote = emptyList(),
            )

            // ----- Assert -----
            observed(bookId = 2).map { it.text } shouldBe listOf("other book")
            observed(bookId = 1).map { it.text } shouldBe listOf("local")
        }

        @Test
        fun `skips a remote row whose id a pending delete holds`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.insertQuote(
                quote(
                    remoteId = 5,
                    state = QuoteSyncState.PENDING_DELETE,
                ),
            )

            // ----- Act -----
            dao.replaceSyncedForBook(
                bookId = 1,
                remote = listOf(
                    quote(
                        remoteId = 5,
                        text = "resurrected",
                    ),
                ),
            )

            // ----- Assert -----
            observed(bookId = 1).shouldBeEmpty()
        }

        @Test
        fun `adopts a matching pending insert and drops its pending write`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val localId = dao.insertWithWrite(
                entity = quote(
                    text = "mine",
                    actionAt = 50,
                    editionId = 3,
                    state = QuoteSyncState.PENDING_INSERT,
                ),
                enqueuedAt = "2024-01-01T00:00:00",
            )

            // ----- Act -----
            dao.replaceSyncedForBook(
                bookId = 1,
                remote = listOf(
                    quote(
                        remoteId = 8,
                        text = "mine",
                        actionAt = 50,
                        editionId = 3,
                    ),
                ),
            )

            // ----- Assert -----
            val adopted = dao.getById(localId).shouldNotBeNull()
            adopted.remoteId shouldBe 8L
            adopted.syncState shouldBe QuoteSyncState.SYNCED
            observed(bookId = 1).size shouldBe 1
            pendingDao.getPending().shouldBeEmpty()
        }

        @Test
        fun `does not adopt a pending insert with a different edition`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.insertQuote(
                quote(
                    text = "mine",
                    actionAt = 50,
                    editionId = 3,
                    state = QuoteSyncState.PENDING_INSERT,
                ),
            )

            // ----- Act -----
            dao.replaceSyncedForBook(
                bookId = 1,
                remote = listOf(
                    quote(
                        remoteId = 8,
                        text = "mine",
                        actionAt = 50,
                        editionId = 4,
                    ),
                ),
            )

            // ----- Assert -----
            observed(bookId = 1).size shouldBe 2
        }

        @Test
        fun `does not adopt a pending insert with different text`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.insertQuote(
                quote(
                    text = "mine",
                    actionAt = 50,
                    editionId = 3,
                    state = QuoteSyncState.PENDING_INSERT,
                ),
            )

            // ----- Act -----
            dao.replaceSyncedForBook(
                bookId = 1,
                remote = listOf(
                    quote(
                        remoteId = 8,
                        text = "theirs",
                        actionAt = 50,
                        editionId = 3,
                    ),
                ),
            )

            // ----- Assert -----
            observed(bookId = 1).size shouldBe 2
        }
    }

    @Nested
    inner class ReplaceAllSynced {
        @Test
        fun `swaps the synced rows of every book for the remote rows`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.insertQuote(
                quote(
                    bookId = 1,
                    remoteId = 1,
                    text = "stale one",
                ),
            )
            dao.insertQuote(
                quote(
                    bookId = 2,
                    remoteId = 2,
                    text = "stale two",
                ),
            )

            // ----- Act -----
            dao.replaceAllSynced(
                remote = listOf(
                    quote(
                        bookId = 3,
                        remoteId = 3,
                        text = "fresh",
                        bookTitle = "Dune",
                    ),
                ),
            )

            // ----- Assert -----
            val result = observedAll()
            result.map { it.text } shouldBe listOf("fresh")
            result.single().bookTitle shouldBe "Dune"
        }

        @Test
        fun `leaves unsynced rows of every book alone`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.insertQuote(
                quote(
                    bookId = 2,
                    text = "local",
                    state = QuoteSyncState.PENDING_INSERT,
                ),
            )

            // ----- Act -----
            dao.replaceAllSynced(remote = emptyList())

            // ----- Assert -----
            observedAll().map { it.text } shouldBe listOf("local")
        }

        @Test
        fun `skips a remote row whose id a pending delete holds`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.insertQuote(
                quote(
                    bookId = 2,
                    remoteId = 5,
                    state = QuoteSyncState.PENDING_DELETE,
                ),
            )

            // ----- Act -----
            dao.replaceAllSynced(
                remote = listOf(
                    quote(
                        bookId = 2,
                        remoteId = 5,
                        text = "resurrected",
                    ),
                ),
            )

            // ----- Assert -----
            observedAll().shouldBeEmpty()
        }

        @Test
        fun `adopts a matching pending insert of any book and drops its pending write`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val localId = dao.insertWithWrite(
                entity = quote(
                    bookId = 4,
                    text = "mine",
                    actionAt = 50,
                    editionId = 3,
                    state = QuoteSyncState.PENDING_INSERT,
                ),
                enqueuedAt = "2024-01-01T00:00:00",
            )

            // ----- Act -----
            dao.replaceAllSynced(
                remote = listOf(
                    quote(
                        bookId = 4,
                        remoteId = 8,
                        text = "mine",
                        actionAt = 50,
                        editionId = 3,
                    ),
                ),
            )

            // ----- Assert -----
            val adopted = dao.getById(localId).shouldNotBeNull()
            adopted.remoteId shouldBe 8L
            adopted.syncState shouldBe QuoteSyncState.SYNCED
            observedAll().size shouldBe 1
            pendingDao.getPending().shouldBeEmpty()
        }
    }

    @Nested
    inner class MarkSynced {
        @Test
        fun `returns true when the row still exists`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val localId = dao.insertQuote(
                quote(
                    text = "mine",
                    state = QuoteSyncState.PENDING_INSERT,
                ),
            )

            // ----- Act -----
            val result = dao.markSynced(
                localId = localId,
                remoteId = 6,
            )

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `returns false when the row is gone`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val missingLocalId = 999L

            // ----- Act -----
            val result = dao.markSynced(
                localId = missingLocalId,
                remoteId = 6,
            )

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `removes another synced row with the same remote id first`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val duplicateId = dao.insertQuote(
                quote(
                    remoteId = 6,
                    text = "pulled by refresh",
                ),
            )
            val localId = dao.insertQuote(
                quote(
                    text = "mine",
                    state = QuoteSyncState.PENDING_INSERT,
                ),
            )

            // ----- Act -----
            dao.markSynced(
                localId = localId,
                remoteId = 6,
            )

            // ----- Assert -----
            dao.getById(duplicateId) shouldBe null
            dao.getById(localId).shouldNotBeNull().syncState shouldBe QuoteSyncState.SYNCED
        }
    }

    @Nested
    inner class RemoveQuote {
        @Test
        fun `an unsynced quote is deleted with its pending writes`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val localId = dao.insertWithWrite(
                entity = quote(state = QuoteSyncState.PENDING_INSERT),
                enqueuedAt = "2024-01-01T00:00:00",
            )

            // ----- Act -----
            val queued = dao.removeQuote(
                localId = localId,
                enqueuedAt = "2024-01-02T00:00:00",
            )

            // ----- Assert -----
            queued shouldBe false
            dao.getById(localId) shouldBe null
            pendingDao.getPending().shouldBeEmpty()
        }

        @Test
        fun `a synced quote is hidden and a delete is queued`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val localId = dao.insertQuote(
                quote(remoteId = 4),
            )

            // ----- Act -----
            val queued = dao.removeQuote(
                localId = localId,
                enqueuedAt = "2024-01-02T00:00:00",
            )

            // ----- Assert -----
            queued shouldBe true
            observed(bookId = 1).shouldBeEmpty()
            pendingDao.getPending().map { it.kind to it.quoteLocalId } shouldBe listOf("DELETE" to localId)
        }

        @Test
        fun `removing a pending delete again queues nothing more`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val localId = dao.insertQuote(
                quote(remoteId = 4),
            )
            dao.removeQuote(
                localId = localId,
                enqueuedAt = "2024-01-02T00:00:00",
            )

            // ----- Act -----
            val queued = dao.removeQuote(
                localId = localId,
                enqueuedAt = "2024-01-03T00:00:00",
            )

            // ----- Assert -----
            queued shouldBe false
            pendingDao.getPending().size shouldBe 1
        }
    }

    @Nested
    inner class PendingWrites {
        @Test
        fun `getPending skips writes at the attempt cap and orders by enqueue time`() = runTest(testDispatcher) {
            // ----- Arrange -----
            pendingDao.insert(
                PendingQuoteWriteEntity(
                    kind = "INSERT",
                    quoteLocalId = 1,
                    enqueuedAt = "b",
                ),
            )
            pendingDao.insert(
                PendingQuoteWriteEntity(
                    kind = "INSERT",
                    quoteLocalId = 2,
                    enqueuedAt = "a",
                ),
            )
            pendingDao.insert(
                PendingQuoteWriteEntity(
                    kind = "INSERT",
                    quoteLocalId = 3,
                    enqueuedAt = "c",
                    attempts = 5,
                ),
            )

            // ----- Act -----
            val result = pendingDao.getPending()

            // ----- Assert -----
            result.map { it.quoteLocalId } shouldBe listOf(2L, 1L)
        }
    }
}
