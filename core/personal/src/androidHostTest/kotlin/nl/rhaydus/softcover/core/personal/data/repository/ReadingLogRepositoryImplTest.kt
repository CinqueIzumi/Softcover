package nl.rhaydus.softcover.core.personal.data.repository

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.database.model.ReadingLogEntryEntity
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingLogLocalDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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

    @Nested
    inner class ObserveByBookId {
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
                startedAt = LocalDate(
                    2024,
                    2,
                    1,
                ),
                finishedAt = LocalDate(
                    2024,
                    2,
                    20,
                ),
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
    }
}
