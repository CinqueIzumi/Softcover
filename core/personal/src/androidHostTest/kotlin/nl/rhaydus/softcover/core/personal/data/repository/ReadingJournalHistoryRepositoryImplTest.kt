package nl.rhaydus.softcover.core.personal.data.repository

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.model.ProgressHistoryEntity
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingJournalHistoryLocalDataSource
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingJournalHistoryRemoteDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingJournalHistoryRepositoryImplTest {
    private class FakeLocal : ReadingJournalHistoryLocalDataSource {
        val rows = mutableListOf<ProgressHistoryEntity>()
        private var nextId = 1L

        fun add(entity: ProgressHistoryEntity): Long {
            val id = nextId++
            rows.add(entity.copy(localId = id))
            return id
        }

        override fun observeByBookId(bookId: Int) = flowOf(rows.filter { it.bookId == bookId })

        override suspend fun replaceSyncedForBook(
            bookId: Int,
            entities: List<ProgressHistoryEntity>,
        ) {
            rows.removeAll { it.bookId == bookId && it.pending.not() }
            entities.forEach { add(entity = it) }
        }

        override suspend fun replaceAllSynced(entities: List<ProgressHistoryEntity>) {
            rows.removeAll { it.pending.not() }
            entities.forEach { add(entity = it) }
        }
    }

    private lateinit var remote: ReadingJournalHistoryRemoteDataSource
    private lateinit var local: FakeLocal
    private lateinit var repository: ReadingJournalHistoryRepositoryImpl

    @BeforeEach
    fun setUp() {
        remote = mockk()
        local = FakeLocal()
        repository = ReadingJournalHistoryRepositoryImpl(
            readingJournalHistoryRemoteDataSource = remote,
            readingJournalHistoryLocalDataSource = local,
        )
    }

    private fun entity(
        actionAtEpochMillis: Long,
        pages: Int?,
        pending: Boolean = false,
        localId: Long = 0L,
    ): ProgressHistoryEntity = ProgressHistoryEntity(
        localId = localId,
        bookId = 5,
        actionAtEpochMillis = actionAtEpochMillis,
        pages = pages,
        seconds = null,
        pending = pending,
    )

    @Nested
    inner class ObserveReadingJournalHistory {
        @Test
        fun `maps stored rows to entries in the stored order`() = runTest {
            // ----- Arrange -----
            val rows = listOf(
                entity(
                    actionAtEpochMillis = 1_728_122_400_000L,
                    pages = 10,
                ),
                entity(
                    actionAtEpochMillis = 1_728_208_800_000L,
                    pages = 30,
                    pending = true,
                ),
            )

            local.rows.addAll(rows)

            // ----- Act -----
            val result = repository.observeReadingJournalHistory(bookId = 5).first()

            // ----- Assert -----
            result.map { it.pages } shouldBe listOf(
                10,
                30,
            )
        }

        @Test
        fun `emits an empty list when nothing is stored`() = runTest {
            // ----- Arrange -----
            // ----- Act -----
            val result = repository.observeReadingJournalHistory(bookId = 5).first()

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class RefreshReadingJournalHistory {
        @Test
        fun `replaces the previously synced rows with the remote rows and keeps pending rows`() = runTest {
            // ----- Arrange -----
            local.add(
                entity = entity(
                    actionAtEpochMillis = 1_000L,
                    pages = 1,
                ),
            )
            local.add(
                entity = entity(
                    actionAtEpochMillis = 2_000L,
                    pages = 2,
                    pending = true,
                ),
            )

            coEvery {
                remote.getReadingJournalHistory(bookId = 5)
            } returns listOf(
                entity(
                    actionAtEpochMillis = 3_000L,
                    pages = 7,
                ),
            )

            // ----- Act -----
            repository.refreshReadingJournalHistory(bookId = 5)

            // ----- Assert -----
            local.rows.map { it.pages }.shouldContainExactly(
                2,
                7,
            )
        }

        @Test
        fun `propagates a remote failure and leaves the rows untouched`() = runTest {
            // ----- Arrange -----
            local.add(
                entity = entity(
                    actionAtEpochMillis = 1_000L,
                    pages = 1,
                ),
            )
            local.add(
                entity = entity(
                    actionAtEpochMillis = 2_000L,
                    pages = 2,
                    pending = true,
                ),
            )

            coEvery {
                remote.getReadingJournalHistory(bookId = 5)
            } throws RuntimeException("offline")

            // ----- Act & Assert -----
            shouldThrow<RuntimeException> {
                repository.refreshReadingJournalHistory(bookId = 5)
            }

            local.rows.map { it.pages }.shouldContainExactly(
                1,
                2,
            )
        }
    }
}
