package nl.rhaydus.softcover.core.database.dao

import android.content.Context
import androidx.room.Room
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.model.ProgressHistoryEntity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProgressHistoryDaoTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: SoftcoverDatabase
    private lateinit var dao: ProgressHistoryDao

    @BeforeEach
    fun setUp() {
        val context: Context = mockk(relaxed = true)

        database = SoftcoverDatabase.build(
            builder = Room.inMemoryDatabaseBuilder(context = context),
            queryContext = testDispatcher,
        )
        dao = database.progressHistoryDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    private fun row(
        bookId: Int,
        actionAtEpochMillis: Long,
        pages: Int,
        pending: Boolean = false,
    ): ProgressHistoryEntity = ProgressHistoryEntity(
        bookId = bookId,
        actionAtEpochMillis = actionAtEpochMillis,
        pages = pages,
        seconds = null,
        pending = pending,
    )

    private suspend fun pagesFor(bookId: Int): List<Int?> =
        dao.observeByBookId(bookId = bookId).first().map { it.pages }

    @Nested
    inner class ObserveByBookId {
        @Test
        fun `orders by epoch millis and breaks ties by insertion order`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 3000,
                    pages = 3,
                ),
            )
            dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 1000,
                    pages = 1,
                ),
            )
            dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 1000,
                    pages = 2,
                ),
            )

            // ----- Act -----
            val result = pagesFor(bookId = 1)

            // ----- Assert -----
            result shouldBe listOf(
                1,
                2,
                3,
            )
        }

        @Test
        fun `orders rows less than a second apart by their millis`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 1500,
                    pages = 2,
                ),
            )
            dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 1200,
                    pages = 1,
                ),
            )
            dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 1999,
                    pages = 3,
                ),
            )

            // ----- Act -----
            val result = pagesFor(bookId = 1)

            // ----- Assert -----
            result shouldBe listOf(
                1,
                2,
                3,
            )
        }

        @Test
        fun `only returns rows of the requested book`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 1000,
                    pages = 1,
                ),
            )
            dao.insert(
                entity = row(
                    bookId = 2,
                    actionAtEpochMillis = 1000,
                    pages = 2,
                ),
            )

            // ----- Act -----
            val result = pagesFor(bookId = 2)

            // ----- Assert -----
            result shouldBe listOf(2)
        }

        @Test
        fun `emits an empty list for a book without rows`() = runTest(testDispatcher) {
            // ----- Arrange -----

            // ----- Act -----
            val result = dao.observeByBookId(bookId = 1).first()

            // ----- Assert -----
            result.shouldBeEmpty()
        }
    }

    @Nested
    inner class GetSyncedIdsForBook {
        @Test
        fun `returns only the synced rows of that book`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val syncedId = dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 1000,
                    pages = 1,
                ),
            )
            dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 2000,
                    pages = 2,
                    pending = true,
                ),
            )
            dao.insert(
                entity = row(
                    bookId = 2,
                    actionAtEpochMillis = 1000,
                    pages = 3,
                ),
            )

            // ----- Act -----
            val result = dao.getSyncedIdsForBook(bookId = 1)

            // ----- Assert -----
            result shouldContainExactly listOf(syncedId)
        }
    }

    @Nested
    inner class ReplaceSynced {
        @Test
        fun `deletes the snapshotted rows and inserts the new ones, keeping other books`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val syncedId = dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 1000,
                    pages = 1,
                ),
            )
            dao.insert(
                entity = row(
                    bookId = 2,
                    actionAtEpochMillis = 1000,
                    pages = 99,
                ),
            )

            // ----- Act -----
            dao.replaceSynced(
                syncedIdsBeforeFetch = listOf(syncedId),
                entities = listOf(
                    row(
                        bookId = 1,
                        actionAtEpochMillis = 3000,
                        pages = 7,
                    ),
                ),
            )

            // ----- Assert -----
            pagesFor(bookId = 1) shouldBe listOf(7)
            pagesFor(bookId = 2) shouldBe listOf(99)
        }

        @Test
        fun `keeps a row that was pending at snapshot time`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val syncedId = dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 1000,
                    pages = 1,
                ),
            )
            dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 2000,
                    pages = 2,
                    pending = true,
                ),
            )

            // ----- Act -----
            dao.replaceSynced(
                syncedIdsBeforeFetch = listOf(syncedId),
                entities = emptyList(),
            )

            // ----- Assert -----
            pagesFor(bookId = 1) shouldBe listOf(2)
        }

        @Test
        fun `keeps a row that turned synced after the snapshot`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val snapshottedId = dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 1000,
                    pages = 1,
                ),
            )
            val racingId = dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 2000,
                    pages = 2,
                    pending = true,
                ),
            )

            // ----- Act -----
            val snapshot = listOf(snapshottedId)
            dao.markSynced(localId = racingId)
            dao.replaceSynced(
                syncedIdsBeforeFetch = snapshot,
                entities = emptyList(),
            )

            // ----- Assert -----
            pagesFor(bookId = 1) shouldBe listOf(2)
        }

        @Test
        fun `an empty snapshot deletes nothing and still inserts the new rows`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 1000,
                    pages = 1,
                ),
            )

            // ----- Act -----
            dao.replaceSynced(
                syncedIdsBeforeFetch = emptyList(),
                entities = listOf(
                    row(
                        bookId = 1,
                        actionAtEpochMillis = 3000,
                        pages = 7,
                    ),
                ),
            )

            // ----- Assert -----
            pagesFor(bookId = 1) shouldBe listOf(
                1,
                7,
            )
        }
    }

    @Nested
    inner class MarkSynced {
        @Test
        fun `clears pending on that row only`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val firstId = dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 1000,
                    pages = 1,
                    pending = true,
                ),
            )
            dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 2000,
                    pages = 2,
                    pending = true,
                ),
            )

            // ----- Act -----
            dao.markSynced(localId = firstId)

            // ----- Assert -----
            dao.observeByBookId(bookId = 1).first().map { it.pending } shouldBe listOf(
                false,
                true,
            )
        }
    }

    @Nested
    inner class DeleteById {
        @Test
        fun `removes only the targeted row`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val firstId = dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 1000,
                    pages = 1,
                ),
            )
            dao.insert(
                entity = row(
                    bookId = 1,
                    actionAtEpochMillis = 2000,
                    pages = 2,
                ),
            )

            // ----- Act -----
            dao.deleteById(localId = firstId)

            // ----- Assert -----
            pagesFor(bookId = 1) shouldBe listOf(2)
        }
    }
}
