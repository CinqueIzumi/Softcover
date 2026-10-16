package nl.rhaydus.softcover.core.personal.data.repository

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.model.QuoteEntity
import nl.rhaydus.softcover.core.database.model.QuoteSyncState
import nl.rhaydus.softcover.core.domain.connectivity.QuoteWriteDrainer
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.personal.data.datasource.QuoteLocalDataSource
import nl.rhaydus.softcover.core.personal.data.datasource.QuoteRemoteDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Instant

class QuoteRepositoryImplTest {
    private lateinit var localDataSource: QuoteLocalDataSource
    private lateinit var remoteDataSource: QuoteRemoteDataSource
    private lateinit var drainer: QuoteWriteDrainer
    private lateinit var repository: QuoteRepositoryImpl
    private val fixedInstant = Instant.parse("2024-06-15T12:30:00Z")

    @BeforeEach
    fun setUp() {
        localDataSource = mockk()
        remoteDataSource = mockk()
        drainer = mockk()
        repository = QuoteRepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            drainer = drainer,
            clock = FixedClock(fixedInstant),
        )
    }

    private class FixedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private fun entity(localId: Long) = QuoteEntity(
        localId = localId,
        remoteId = localId,
        bookId = 1,
        editionId = null,
        text = "A quote $localId",
        privacySettingId = 1,
        actionAtEpochMillis = 0,
        positionType = null,
        positionValue = null,
        positionPossible = null,
        positionPercent = null,
        syncState = QuoteSyncState.SYNCED,
    )

    @Nested
    inner class Add {
        @Test
        fun `persists the given actionAt and drains afterwards`() = runTest {
            // ----- Arrange -----
            val actionAt = Instant.parse("2024-03-01T08:00:00Z")
            val saved = slot<QuoteEntity>()

            coEvery {
                localDataSource.insertWithWrite(
                    entity = capture(saved),
                    enqueuedAt = any(),
                )
            } returns 12L

            coJustRun { drainer.drain() }

            // ----- Act -----
            val result = repository.add(
                bookId = 1,
                editionId = 2,
                text = "A quote",
                privacy = PrivacySetting.PRIVATE,
                actionAt = actionAt,
                position = null,
            )

            // ----- Assert -----
            result shouldBe 12L
            saved.captured.actionAtEpochMillis shouldBe actionAt.toEpochMilliseconds()

            coVerifyOrder {
                localDataSource.insertWithWrite(
                    entity = any(),
                    enqueuedAt = any(),
                )
                drainer.drain()
            }
        }

        @Test
        fun `uses the current time when no actionAt is given`() = runTest {
            // ----- Arrange -----
            val saved = slot<QuoteEntity>()

            coEvery {
                localDataSource.insertWithWrite(
                    entity = capture(saved),
                    enqueuedAt = any(),
                )
            } returns 12L

            coJustRun { drainer.drain() }

            // ----- Act -----
            repository.add(
                bookId = 1,
                editionId = null,
                text = "A quote",
                privacy = PrivacySetting.PUBLIC,
                actionAt = null,
                position = null,
            )

            // ----- Assert -----
            saved.captured.actionAtEpochMillis shouldBe fixedInstant.toEpochMilliseconds()
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `does not drain when the quote was removed locally only`() = runTest {
            // ----- Arrange -----
            coEvery {
                localDataSource.remove(
                    localId = 4L,
                    enqueuedAt = any(),
                )
            } returns false

            // ----- Act -----
            repository.delete(localId = 4L)

            // ----- Assert -----
            coVerify(exactly = 0) { drainer.drain() }
        }

        @Test
        fun `drains when a remote delete was queued`() = runTest {
            // ----- Arrange -----
            coEvery {
                localDataSource.remove(
                    localId = 4L,
                    enqueuedAt = any(),
                )
            } returns true

            coJustRun { drainer.drain() }

            // ----- Act -----
            repository.delete(localId = 4L)

            // ----- Assert -----
            coVerify(exactly = 1) { drainer.drain() }
        }
    }

    @Nested
    inner class Refresh {
        @Test
        fun `drains before fetching and replaces the synced rows with the fetched ones`() = runTest {
            // ----- Arrange -----
            val fetched = listOf(entity(localId = 1), entity(localId = 2))

            coJustRun { drainer.drain() }

            coEvery {
                remoteDataSource.getQuotes(bookId = 1)
            } returns fetched

            coJustRun {
                localDataSource.replaceSyncedForBook(
                    bookId = 1,
                    remote = fetched,
                )
            }

            // ----- Act -----
            repository.refreshForBook(bookId = 1)

            // ----- Assert -----
            coVerifyOrder {
                drainer.drain()
                remoteDataSource.getQuotes(bookId = 1)
                localDataSource.replaceSyncedForBook(
                    bookId = 1,
                    remote = fetched,
                )
            }
        }
    }
}
