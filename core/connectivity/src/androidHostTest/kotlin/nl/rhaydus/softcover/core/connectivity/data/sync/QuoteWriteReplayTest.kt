package nl.rhaydus.softcover.core.connectivity.data.sync

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.offlinesync.ReplayOutcome
import nl.rhaydus.softcover.core.database.model.QuoteEntity
import nl.rhaydus.softcover.core.database.model.QuoteSyncState
import nl.rhaydus.softcover.core.domain.connectivity.PendingQuoteWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingQuoteWriteKind
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.personal.data.datasource.QuoteLocalDataSource
import nl.rhaydus.softcover.core.personal.data.datasource.QuoteRemoteDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuoteWriteReplayTest {
    private lateinit var remoteDataSource: QuoteRemoteDataSource
    private lateinit var localDataSource: QuoteLocalDataSource
    private lateinit var replay: QuoteWriteReplay

    @BeforeEach
    fun setUp() {
        remoteDataSource = mockk()
        localDataSource = mockk()
        replay = QuoteWriteReplay(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
        )
    }

    private fun write(
        kind: PendingQuoteWriteKind,
        attempts: Int = 0,
    ) = PendingQuoteWrite(
        kind = kind,
        quoteLocalId = LOCAL_ID,
        attempts = attempts,
        enqueuedAt = "2026-05-10T10:00:00Z",
    )

    private fun quote(
        syncState: QuoteSyncState,
        remoteId: Long? = null,
    ) = QuoteEntity(
        localId = LOCAL_ID,
        remoteId = remoteId,
        bookId = 1,
        editionId = null,
        text = "A quote",
        privacySettingId = 1,
        actionAtEpochMillis = 0,
        positionType = null,
        positionValue = null,
        positionPossible = null,
        positionPercent = null,
        syncState = syncState,
    )

    @Nested
    inner class Insert {
        @Test
        fun `is discarded without a remote call when the row is gone`() = runTest {
            // ----- Arrange -----
            coEvery {
                localDataSource.getById(localId = LOCAL_ID)
            } returns null

            // ----- Act -----
            val result = replay(payload = write(kind = PendingQuoteWriteKind.INSERT))

            // ----- Assert -----
            result shouldBe ReplayOutcome.DISCARDED

            coVerify(exactly = 0) { remoteDataSource.insertQuote(entity = any()) }
        }

        @Test
        fun `is done without a remote call when the quote is already synced`() = runTest {
            // ----- Arrange -----
            coEvery {
                localDataSource.getById(localId = LOCAL_ID)
            } returns quote(
                syncState = QuoteSyncState.SYNCED,
                remoteId = 50L,
            )

            // ----- Act -----
            val result = replay(payload = write(kind = PendingQuoteWriteKind.INSERT))

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 0) { remoteDataSource.insertQuote(entity = any()) }
        }

        @Test
        fun `marks the row synced with the id Hardcover returned`() = runTest {
            // ----- Arrange -----
            val pending = quote(syncState = QuoteSyncState.PENDING_INSERT)

            coEvery {
                localDataSource.getById(localId = LOCAL_ID)
            } returns pending

            coEvery {
                remoteDataSource.insertQuote(entity = pending)
            } returns 901L

            coEvery {
                localDataSource.markSynced(
                    localId = LOCAL_ID,
                    remoteId = 901L,
                )
            } returns true

            // ----- Act -----
            val result = replay(payload = write(kind = PendingQuoteWriteKind.INSERT))

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 1) {
                localDataSource.markSynced(
                    localId = LOCAL_ID,
                    remoteId = 901L,
                )
            }
            coVerify(exactly = 0) { remoteDataSource.getQuotes(bookId = any()) }
        }

        @Test
        fun `deletes the local row and rethrows when the server rejects the insert`() = runTest {
            // ----- Arrange -----
            val pending = quote(syncState = QuoteSyncState.PENDING_INSERT)

            coEvery {
                localDataSource.getById(localId = LOCAL_ID)
            } returns pending

            coEvery {
                remoteDataSource.insertQuote(entity = pending)
            } throws IllegalStateException("InsertQuote rejected by server")

            coJustRun { localDataSource.deleteRow(localId = LOCAL_ID) }

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> { replay(payload = write(kind = PendingQuoteWriteKind.INSERT)) }

            coVerify(exactly = 1) { localDataSource.deleteRow(localId = LOCAL_ID) }
        }

        @Test
        fun `keeps the local row and rethrows on a transient failure`() = runTest {
            // ----- Arrange -----
            val pending = quote(syncState = QuoteSyncState.PENDING_INSERT)

            coEvery {
                localDataSource.getById(localId = LOCAL_ID)
            } returns pending

            coEvery {
                remoteDataSource.insertQuote(entity = pending)
            } throws OfflineException()

            // ----- Act & Assert -----
            shouldThrow<OfflineException> { replay(payload = write(kind = PendingQuoteWriteKind.INSERT)) }

            coVerify(exactly = 0) { localDataSource.deleteRow(localId = any()) }
        }

        @Test
        fun `a retry adopts the matching remote quote instead of posting`() = runTest {
            // ----- Arrange -----
            val pending = quote(syncState = QuoteSyncState.PENDING_INSERT)

            coEvery {
                localDataSource.getById(localId = LOCAL_ID)
            } returns pending

            coEvery {
                remoteDataSource.getQuotes(bookId = 1)
            } returns listOf(
                pending.copy(
                    localId = 0,
                    remoteId = 777L,
                    syncState = QuoteSyncState.SYNCED,
                ),
            )

            coEvery {
                localDataSource.markSynced(
                    localId = LOCAL_ID,
                    remoteId = 777L,
                )
            } returns true

            // ----- Act -----
            val result = replay(
                payload = write(
                    kind = PendingQuoteWriteKind.INSERT,
                    attempts = 1,
                ),
            )

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 0) { remoteDataSource.insertQuote(entity = any()) }
            coVerify(exactly = 1) {
                localDataSource.markSynced(
                    localId = LOCAL_ID,
                    remoteId = 777L,
                )
            }
        }

        @Test
        fun `a retry posts when no remote quote matches`() = runTest {
            // ----- Arrange -----
            val pending = quote(syncState = QuoteSyncState.PENDING_INSERT)

            coEvery {
                localDataSource.getById(localId = LOCAL_ID)
            } returns pending

            coEvery {
                remoteDataSource.getQuotes(bookId = 1)
            } returns listOf(
                pending.copy(
                    localId = 0,
                    remoteId = 777L,
                    text = "Another quote",
                    syncState = QuoteSyncState.SYNCED,
                ),
            )

            coEvery {
                remoteDataSource.insertQuote(entity = pending)
            } returns 901L

            coEvery {
                localDataSource.markSynced(
                    localId = LOCAL_ID,
                    remoteId = 901L,
                )
            } returns true

            // ----- Act -----
            val result = replay(
                payload = write(
                    kind = PendingQuoteWriteKind.INSERT,
                    attempts = 2,
                ),
            )

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 1) { remoteDataSource.insertQuote(entity = pending) }
        }

        @Test
        fun `a retry posts when looking for the remote copy fails`() = runTest {
            // ----- Arrange -----
            val pending = quote(syncState = QuoteSyncState.PENDING_INSERT)

            coEvery {
                localDataSource.getById(localId = LOCAL_ID)
            } returns pending

            coEvery {
                remoteDataSource.getQuotes(bookId = 1)
            } throws OfflineException()

            coEvery {
                remoteDataSource.insertQuote(entity = pending)
            } returns 901L

            coEvery {
                localDataSource.markSynced(
                    localId = LOCAL_ID,
                    remoteId = 901L,
                )
            } returns true

            // ----- Act -----
            val result = replay(
                payload = write(
                    kind = PendingQuoteWriteKind.INSERT,
                    attempts = 1,
                ),
            )

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 1) { remoteDataSource.insertQuote(entity = pending) }
        }

        @Test
        fun `deletes the remote quote when the local row vanished mid-flight`() = runTest {
            // ----- Arrange -----
            val pending = quote(syncState = QuoteSyncState.PENDING_INSERT)

            coEvery {
                localDataSource.getById(localId = LOCAL_ID)
            } returns pending

            coEvery {
                remoteDataSource.insertQuote(entity = pending)
            } returns 901L

            coEvery {
                localDataSource.markSynced(
                    localId = LOCAL_ID,
                    remoteId = 901L,
                )
            } returns false

            coJustRun { remoteDataSource.deleteQuote(remoteId = 901L) }

            // ----- Act -----
            val result = replay(payload = write(kind = PendingQuoteWriteKind.INSERT))

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 1) { remoteDataSource.deleteQuote(remoteId = 901L) }
        }

        @Test
        fun `still finishes when deleting the orphaned remote quote fails`() = runTest {
            // ----- Arrange -----
            val pending = quote(syncState = QuoteSyncState.PENDING_INSERT)

            coEvery {
                localDataSource.getById(localId = LOCAL_ID)
            } returns pending

            coEvery {
                remoteDataSource.insertQuote(entity = pending)
            } returns 901L

            coEvery {
                localDataSource.markSynced(
                    localId = LOCAL_ID,
                    remoteId = 901L,
                )
            } returns false

            coEvery {
                remoteDataSource.deleteQuote(remoteId = 901L)
            } throws OfflineException()

            // ----- Act -----
            val result = replay(payload = write(kind = PendingQuoteWriteKind.INSERT))

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `is discarded when the row has no remote id`() = runTest {
            // ----- Arrange -----
            coEvery {
                localDataSource.getById(localId = LOCAL_ID)
            } returns quote(
                syncState = QuoteSyncState.PENDING_DELETE,
                remoteId = null,
            )

            // ----- Act -----
            val result = replay(payload = write(kind = PendingQuoteWriteKind.DELETE))

            // ----- Assert -----
            result shouldBe ReplayOutcome.DISCARDED

            coVerify(exactly = 0) { remoteDataSource.deleteQuote(remoteId = any()) }
        }

        @Test
        fun `deletes remotely and then removes the local row`() = runTest {
            // ----- Arrange -----
            coEvery {
                localDataSource.getById(localId = LOCAL_ID)
            } returns quote(
                syncState = QuoteSyncState.PENDING_DELETE,
                remoteId = 50L,
            )

            coJustRun { remoteDataSource.deleteQuote(remoteId = 50L) }

            coJustRun { localDataSource.deleteRow(localId = LOCAL_ID) }

            // ----- Act -----
            val result = replay(payload = write(kind = PendingQuoteWriteKind.DELETE))

            // ----- Assert -----
            result shouldBe ReplayOutcome.SYNCED

            coVerify(exactly = 1) { localDataSource.deleteRow(localId = LOCAL_ID) }
        }

        @Test
        fun `restores the row and rethrows when the server rejects the delete`() = runTest {
            // ----- Arrange -----
            coEvery {
                localDataSource.getById(localId = LOCAL_ID)
            } returns quote(
                syncState = QuoteSyncState.PENDING_DELETE,
                remoteId = 50L,
            )

            coEvery {
                remoteDataSource.deleteQuote(remoteId = 50L)
            } throws IllegalStateException("rejected")

            coJustRun { localDataSource.restoreSynced(localId = LOCAL_ID) }

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> { replay(payload = write(kind = PendingQuoteWriteKind.DELETE)) }

            coVerify(exactly = 1) { localDataSource.restoreSynced(localId = LOCAL_ID) }
            coVerify(exactly = 0) { localDataSource.deleteRow(localId = any()) }
        }

        @Test
        fun `leaves the row pending and rethrows on a transient failure`() = runTest {
            // ----- Arrange -----
            coEvery {
                localDataSource.getById(localId = LOCAL_ID)
            } returns quote(
                syncState = QuoteSyncState.PENDING_DELETE,
                remoteId = 50L,
            )

            coEvery {
                remoteDataSource.deleteQuote(remoteId = 50L)
            } throws OfflineException()

            // ----- Act & Assert -----
            shouldThrow<OfflineException> { replay(payload = write(kind = PendingQuoteWriteKind.DELETE)) }

            coVerify(exactly = 0) { localDataSource.restoreSynced(localId = any()) }
            coVerify(exactly = 0) { localDataSource.deleteRow(localId = any()) }
        }
    }

    private companion object {
        const val LOCAL_ID = 7L
    }
}
