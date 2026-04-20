package nl.rhaydus.softcover.feature.deadlines.data.repository

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.deadlines.data.datasource.BookDeadlineLocalDataSource
import nl.rhaydus.softcover.feature.deadlines.data.model.BookDeadlineEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BookDeadlineRepositoryImplTest {

    private lateinit var localDataSource: BookDeadlineLocalDataSource
    private lateinit var repository: BookDeadlineRepositoryImpl

    @BeforeEach
    fun setUp() {
        localDataSource = mockk(relaxed = true)
        repository = BookDeadlineRepositoryImpl(localDataSource = localDataSource)
    }

    private fun buildEntity(
        bookId: Int = 1,
        deadlineDate: String = "2026-05-01",
        setAt: String = "2026-04-01",
        initialPagesPerDay: Float = 10f,
    ) = BookDeadlineEntity(
        bookId = bookId,
        deadlineDate = deadlineDate,
        setAt = setAt,
        initialPagesPerDay = initialPagesPerDay,
    )

    @Nested
    inner class Observe {

        @Test
        fun `returns mapped domain object when data source emits an entity`() = runTest {
            // ----- Arrange -----
            val entity = buildEntity(bookId = 42, deadlineDate = "2026-07-10", setAt = "2026-04-01", initialPagesPerDay = 8f)

            every {
                localDataSource.observe(bookId = 42)
            } returns flowOf(entity)

            // ----- Act & Assert -----
            repository.observe(bookId = 42).test {
                val item = awaitItem()
                item!!.bookId shouldBe 42
                item.deadlineDate shouldBe LocalDate.of(2026, 7, 10)
                item.initialPagesPerDay shouldBe 8f
                awaitComplete()
            }
        }

        @Test
        fun `returns null when data source emits null`() = runTest {
            // ----- Arrange -----
            every {
                localDataSource.observe(bookId = 5)
            } returns flowOf(null)

            // ----- Act & Assert -----
            repository.observe(bookId = 5).test {
                awaitItem() shouldBe null
                awaitComplete()
            }
        }

        @Test
        fun `delegates to local data source with the given bookId`() = runTest {
            // ----- Arrange -----
            val bookId = 99

            every {
                localDataSource.observe(bookId = bookId)
            } returns flowOf(null)

            // ----- Act & Assert -----
            repository.observe(bookId = bookId).test {
                awaitItem()
                awaitComplete()
            }
        }
    }

    @Nested
    inner class ObserveAll {

        @Test
        fun `returns list of mapped domain objects`() = runTest {
            // ----- Arrange -----
            val entity1 = buildEntity(bookId = 1, deadlineDate = "2026-05-01", setAt = "2026-04-01", initialPagesPerDay = 10f)
            val entity2 = buildEntity(bookId = 2, deadlineDate = "2026-06-15", setAt = "2026-04-10", initialPagesPerDay = 5f)

            every {
                localDataSource.observeAll()
            } returns flowOf(listOf(entity1, entity2))

            // ----- Act & Assert -----
            repository.observeAll().test {
                val items = awaitItem()
                items.size shouldBe 2
                items[0].bookId shouldBe 1
                items[1].bookId shouldBe 2
                awaitComplete()
            }
        }

        @Test
        fun `returns empty list when data source emits empty list`() = runTest {
            // ----- Arrange -----
            every {
                localDataSource.observeAll()
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            repository.observeAll().test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }
    }

    @Nested
    inner class SetDeadline {

        @Test
        fun `upserts entity with correct initialPagesPerDay for future deadline`() = runTest {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadlineDate = LocalDate.of(2026, 5, 20)  // 30 days away
            // 300 pages, currentPage 0 → 300 remaining / 30 days = 10f
            val entitySlot = slot<BookDeadlineEntity>()

            coEvery {
                localDataSource.upsert(entity = capture(entitySlot))
            } returns Unit

            // ----- Act -----
            repository.setDeadline(
                bookId = 1,
                deadlineDate = deadlineDate,
                currentPage = 0,
                totalPages = 300,
                today = today,
            )

            // ----- Assert -----
            entitySlot.captured.initialPagesPerDay shouldBe 10f
            entitySlot.captured.bookId shouldBe 1
            entitySlot.captured.deadlineDate shouldBe "2026-05-20"
            entitySlot.captured.setAt shouldBe "2026-04-20"
        }

        @Test
        fun `initialPagesPerDay is zero when pagesRemaining is zero`() = runTest {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadlineDate = LocalDate.of(2026, 5, 20)
            val entitySlot = slot<BookDeadlineEntity>()

            coEvery {
                localDataSource.upsert(entity = capture(entitySlot))
            } returns Unit

            // ----- Act -----
            repository.setDeadline(
                bookId = 2,
                deadlineDate = deadlineDate,
                currentPage = 300,
                totalPages = 300,
                today = today,
            )

            // ----- Assert -----
            entitySlot.captured.initialPagesPerDay shouldBe 0f
        }

        @Test
        fun `initialPagesPerDay equals pagesRemaining when deadlineDate equals today`() = runTest {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadlineDate = today  // 0 days until deadline
            // 200 pages remaining, 0 days → requiredPerDay = pagesRemaining.toFloat()
            val entitySlot = slot<BookDeadlineEntity>()

            coEvery {
                localDataSource.upsert(entity = capture(entitySlot))
            } returns Unit

            // ----- Act -----
            repository.setDeadline(
                bookId = 3,
                deadlineDate = deadlineDate,
                currentPage = 100,
                totalPages = 300,
                today = today,
            )

            // ----- Assert -----
            entitySlot.captured.initialPagesPerDay shouldBe 200f
        }

        @Test
        fun `initialPagesPerDay equals pagesRemaining when deadline is in the past`() = runTest {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadlineDate = LocalDate.of(2026, 4, 19)  // yesterday
            val entitySlot = slot<BookDeadlineEntity>()

            coEvery {
                localDataSource.upsert(entity = capture(entitySlot))
            } returns Unit

            // ----- Act -----
            repository.setDeadline(
                bookId = 4,
                deadlineDate = deadlineDate,
                currentPage = 50,
                totalPages = 200,
                today = today,
            )

            // ----- Assert -----
            entitySlot.captured.initialPagesPerDay shouldBe 150f
        }

        @Test
        fun `pagesRemaining clamps to zero when currentPage exceeds totalPages`() = runTest {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadlineDate = LocalDate.of(2026, 5, 20)
            val entitySlot = slot<BookDeadlineEntity>()

            coEvery {
                localDataSource.upsert(entity = capture(entitySlot))
            } returns Unit

            // ----- Act -----
            repository.setDeadline(
                bookId = 5,
                deadlineDate = deadlineDate,
                currentPage = 400,
                totalPages = 300,
                today = today,
            )

            // ----- Assert -----
            entitySlot.captured.initialPagesPerDay shouldBe 0f
        }
    }

    @Nested
    inner class ClearDeadline {

        @Test
        fun `delegates delete to local data source with the given bookId`() = runTest {
            // ----- Arrange -----
            val bookId = 77

            // ----- Act -----
            repository.clearDeadline(bookId = bookId)

            // ----- Assert -----
            coVerify {
                localDataSource.delete(bookId = bookId)
            }
        }
    }
}
