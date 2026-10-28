package nl.rhaydus.softcover.core.connectivity.data.store

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.offlinesync.PendingWrite
import nl.rhaydus.softcover.core.database.dao.PendingQuoteWriteDao
import nl.rhaydus.softcover.core.database.model.QuoteEntity
import nl.rhaydus.softcover.core.database.model.QuoteSyncState
import nl.rhaydus.softcover.core.domain.connectivity.PendingQuoteWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingQuoteWriteKind
import nl.rhaydus.softcover.core.personal.data.datasource.QuoteLocalDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PendingQuoteWriteStoreTest {
    private lateinit var dao: PendingQuoteWriteDao
    private lateinit var localDataSource: QuoteLocalDataSource
    private lateinit var store: PendingQuoteWriteStore

    @BeforeEach
    fun setUp() {
        dao = mockk()
        localDataSource = mockk()
        store = PendingQuoteWriteStore(
            dao = dao,
            localDataSource = localDataSource,
        )

        coJustRun { dao.delete(any()) }
        coJustRun { localDataSource.deleteRow(any()) }
        coJustRun { localDataSource.restoreSynced(any()) }
    }

    private fun pendingWrite(kind: PendingQuoteWriteKind) = PendingWrite(
        localId = 4L,
        attempts = 9,
        payload = PendingQuoteWrite(
            kind = kind,
            quoteLocalId = QUOTE_LOCAL_ID,
            enqueuedAt = "2026-05-10T10:00:00Z",
        ),
    )

    private fun quote(remoteId: Long?) = QuoteEntity(
        localId = QUOTE_LOCAL_ID,
        remoteId = remoteId,
        bookId = 20,
        editionId = null,
        text = "A quote",
        privacySettingId = 1,
        actionAtEpochMillis = 1_000L,
        positionType = null,
        positionValue = null,
        positionPossible = null,
        positionPercent = null,
        syncState = QuoteSyncState.PENDING_DELETE,
    )

    @Nested
    inner class Abandon {
        @Test
        fun `deletes the row and the never-synced quote for an insert`() = runTest {
            // ----- Arrange -----
            val write = pendingWrite(kind = PendingQuoteWriteKind.INSERT)

            // ----- Act -----
            store.abandon(write = write)

            // ----- Assert -----
            coVerify(exactly = 1) { dao.delete(localId = 4L) }
            coVerify(exactly = 1) { localDataSource.deleteRow(localId = QUOTE_LOCAL_ID) }
            coVerify(exactly = 0) { localDataSource.restoreSynced(localId = any()) }
        }

        @Test
        fun `deletes the row and restores the quote for a delete of a quote with a remote id`() = runTest {
            // ----- Arrange -----
            val write = pendingWrite(kind = PendingQuoteWriteKind.DELETE)

            coEvery {
                localDataSource.getById(localId = QUOTE_LOCAL_ID)
            } returns quote(remoteId = 50L)

            // ----- Act -----
            store.abandon(write = write)

            // ----- Assert -----
            coVerify(exactly = 1) { dao.delete(localId = 4L) }
            coVerify(exactly = 1) { localDataSource.restoreSynced(localId = QUOTE_LOCAL_ID) }
            coVerify(exactly = 0) { localDataSource.deleteRow(localId = any()) }
        }

        @Test
        fun `deletes the row and the quote for a delete of a quote with no remote id`() = runTest {
            // ----- Arrange -----
            val write = pendingWrite(kind = PendingQuoteWriteKind.DELETE)

            coEvery {
                localDataSource.getById(localId = QUOTE_LOCAL_ID)
            } returns quote(remoteId = null)

            // ----- Act -----
            store.abandon(write = write)

            // ----- Assert -----
            coVerify(exactly = 1) { dao.delete(localId = 4L) }
            coVerify(exactly = 1) { localDataSource.deleteRow(localId = QUOTE_LOCAL_ID) }
            coVerify(exactly = 0) { localDataSource.restoreSynced(localId = any()) }
        }

        @Test
        fun `deletes the row and the quote for a delete when the quote is missing`() = runTest {
            // ----- Arrange -----
            val write = pendingWrite(kind = PendingQuoteWriteKind.DELETE)

            coEvery {
                localDataSource.getById(localId = QUOTE_LOCAL_ID)
            } returns null

            // ----- Act -----
            store.abandon(write = write)

            // ----- Assert -----
            coVerify(exactly = 1) { dao.delete(localId = 4L) }
            coVerify(exactly = 1) { localDataSource.deleteRow(localId = QUOTE_LOCAL_ID) }
            coVerify(exactly = 0) { localDataSource.restoreSynced(localId = any()) }
        }

        @Test
        fun `propagates the failure and keeps the row when settling the quote throws`() = runTest {
            // ----- Arrange -----
            val write = pendingWrite(kind = PendingQuoteWriteKind.INSERT)

            coEvery {
                localDataSource.deleteRow(localId = QUOTE_LOCAL_ID)
            } throws IllegalStateException("settle failed")

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> { store.abandon(write = write) }
            coVerify(exactly = 0) { dao.delete(localId = any()) }
        }
    }

    private companion object {
        const val QUOTE_LOCAL_ID = 7L
    }
}
