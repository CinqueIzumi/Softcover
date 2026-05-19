package nl.rhaydus.softcover.feature.personal.data.repository

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.personal.data.datasource.ReadingLogLocalDataSource
import nl.rhaydus.softcover.feature.personal.data.model.ReadingLogEntryEntity
import nl.rhaydus.softcover.feature.personal.domain.model.ReadingLogEntry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class ReadingLogRepositoryImplTest {

    private lateinit var localDataSource: ReadingLogLocalDataSource
    private lateinit var repository: ReadingLogRepositoryImpl

    @BeforeEach
    fun setUp() {
        localDataSource = mockk(relaxed = true)
        repository = ReadingLogRepositoryImpl(localDataSource = localDataSource)
    }

    private fun buildEntity(
        id: Long = 1L,
        bookId: Int = 10,
        startedAt: String? = "2024-01-01",
        finishedAt: String? = "2024-01-10",
        rating: Double? = 4.0,
        note: String? = "Good",
        createdAt: String = "2024-01-11T08:00:00Z",
    ) = ReadingLogEntryEntity(
        id = id,
        bookId = bookId,
        startedAt = startedAt,
        finishedAt = finishedAt,
        rating = rating,
        note = note,
        createdAt = createdAt,
    )

    private fun buildEntry(
        id: Long = 1L,
        bookId: Int = 10,
        startedAt: LocalDate? = LocalDate.of(2024, 1, 1),
        finishedAt: LocalDate? = LocalDate.of(2024, 1, 10),
        rating: Double? = 4.0,
        note: String? = "Good",
        createdAt: Instant = Instant.parse("2024-01-11T08:00:00Z"),
    ) = ReadingLogEntry(
        id = id,
        bookId = bookId,
        startedAt = startedAt,
        finishedAt = finishedAt,
        rating = rating,
        note = note,
        createdAt = createdAt,
    )

    @Nested
    inner class ObserveByBookId {

        @Test
        fun `emits empty list when data source emits empty list`() = runTest {
            // ----- Arrange -----
            every {
                localDataSource.observeByBookId(bookId = 10)
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            repository.observeByBookId(bookId = 10).test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }

        @Test
        fun `maps entities to domain models`() = runTest {
            // ----- Arrange -----
            val entities = listOf(
                buildEntity(
                    id = 1L,
                    note = "First",
                ),
                buildEntity(
                    id = 2L,
                    note = "Second",
                ),
            )

            every {
                localDataSource.observeByBookId(bookId = 10)
            } returns flowOf(entities)

            // ----- Act & Assert -----
            repository.observeByBookId(bookId = 10).test {
                val items = awaitItem()
                items.size shouldBe 2
                items[0].note shouldBe "First"
                items[1].note shouldBe "Second"
                awaitComplete()
            }
        }
    }

    @Nested
    inner class ObserveCountByBookId {

        @Test
        fun `emits zero when no entries`() = runTest {
            // ----- Arrange -----
            every {
                localDataSource.observeCountByBookId(bookId = 10)
            } returns flowOf(0)

            // ----- Act & Assert -----
            repository.observeCountByBookId(bookId = 10).test {
                awaitItem() shouldBe 0
                awaitComplete()
            }
        }

        @Test
        fun `emits count from local data source`() = runTest {
            // ----- Arrange -----
            every {
                localDataSource.observeCountByBookId(bookId = 10)
            } returns flowOf(3)

            // ----- Act & Assert -----
            repository.observeCountByBookId(bookId = 10).test {
                awaitItem() shouldBe 3
                awaitComplete()
            }
        }
    }

    @Nested
    inner class Add {

        @Test
        fun `inserts entity and returns generated id`() = runTest {
            // ----- Arrange -----
            val entitySlot = slot<ReadingLogEntryEntity>()

            coEvery {
                localDataSource.insert(capture(entitySlot))
            } returns 88L

            // ----- Act -----
            val result = repository.add(
                bookId = 10,
                startedAt = LocalDate.of(2024, 2, 1),
                finishedAt = LocalDate.of(2024, 2, 20),
                rating = 3.5,
                note = "Fine read",
            )

            // ----- Assert -----
            result shouldBe 88L
            entitySlot.captured.bookId shouldBe 10
            entitySlot.captured.startedAt shouldBe "2024-02-01"
            entitySlot.captured.finishedAt shouldBe "2024-02-20"
            entitySlot.captured.rating shouldBe 3.5
            entitySlot.captured.note shouldBe "Fine read"
        }

        @Test
        fun `inserts entity with null dates correctly`() = runTest {
            // ----- Arrange -----
            val entitySlot = slot<ReadingLogEntryEntity>()

            coEvery {
                localDataSource.insert(capture(entitySlot))
            } returns 89L

            // ----- Act -----
            repository.add(
                bookId = 5,
                startedAt = null,
                finishedAt = null,
                rating = null,
                note = null,
            )

            // ----- Assert -----
            entitySlot.captured.startedAt shouldBe null
            entitySlot.captured.finishedAt shouldBe null
        }
    }

    @Nested
    inner class Update {

        @Test
        fun `delegates update to local data source with converted entity`() = runTest {
            // ----- Arrange -----
            val entry = buildEntry(
                id = 4L,
                note = "Updated note",
            )
            val entitySlot = slot<ReadingLogEntryEntity>()

            coEvery {
                localDataSource.update(capture(entitySlot))
            } returns Unit

            // ----- Act -----
            repository.update(entry = entry)

            // ----- Assert -----
            entitySlot.captured.id shouldBe 4L
            entitySlot.captured.note shouldBe "Updated note"
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `delegates delete to local data source`() = runTest {
            // ----- Arrange -----

            // ----- Act -----
            repository.delete(id = 13L)

            // ----- Assert -----
            coVerify {
                localDataSource.delete(id = 13L)
            }
        }
    }
}
