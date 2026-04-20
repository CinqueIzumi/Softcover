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
import nl.rhaydus.softcover.feature.deadlines.domain.model.DeadlineUnit
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
        initialPerDay: Float = 10f,
        unit: String = "PAGES",
    ) = BookDeadlineEntity(
        bookId = bookId,
        deadlineDate = deadlineDate,
        setAt = setAt,
        initialPerDay = initialPerDay,
        unit = unit,
    )

    @Nested
    inner class Observe {

        @Test
        fun `returns mapped domain object when data source emits an entity`() = runTest {
            // ----- Arrange -----
            val entity = buildEntity(bookId = 42, deadlineDate = "2026-07-10", setAt = "2026-04-01", initialPerDay = 8f)

            every {
                localDataSource.observe(bookId = 42)
            } returns flowOf(entity)

            // ----- Act & Assert -----
            repository.observe(bookId = 42).test {
                val item = awaitItem()
                item!!.bookId shouldBe 42
                item.deadlineDate shouldBe LocalDate.of(2026, 7, 10)
                item.initialPerDay shouldBe 8f
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
            val entity1 = buildEntity(bookId = 1, deadlineDate = "2026-05-01", setAt = "2026-04-01", initialPerDay = 10f)
            val entity2 = buildEntity(bookId = 2, deadlineDate = "2026-06-15", setAt = "2026-04-10", initialPerDay = 5f)

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
        fun `upserts entity with correct initialPerDay for future deadline`() = runTest {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadlineDate = LocalDate.of(2026, 5, 20)  // 30 days away
            // 300 units, current 0 → 300 remaining / 30 days = 10f
            val entitySlot = slot<BookDeadlineEntity>()

            coEvery {
                localDataSource.upsert(entity = capture(entitySlot))
            } returns Unit

            // ----- Act -----
            repository.setDeadline(
                bookId = 1,
                deadlineDate = deadlineDate,
                current = 0,
                total = 300,
                unit = DeadlineUnit.PAGES,
                today = today,
            )

            // ----- Assert -----
            entitySlot.captured.initialPerDay shouldBe 10f
            entitySlot.captured.bookId shouldBe 1
            entitySlot.captured.deadlineDate shouldBe "2026-05-20"
            entitySlot.captured.setAt shouldBe "2026-04-20"
        }

        @Test
        fun `initialPerDay is zero when remaining is zero`() = runTest {
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
                current = 300,
                total = 300,
                unit = DeadlineUnit.PAGES,
                today = today,
            )

            // ----- Assert -----
            entitySlot.captured.initialPerDay shouldBe 0f
        }

        @Test
        fun `initialPerDay equals remaining when deadlineDate equals today`() = runTest {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadlineDate = today  // 0 days until deadline
            // 200 units remaining, 0 days → initialPerDay = remaining.toFloat()
            val entitySlot = slot<BookDeadlineEntity>()

            coEvery {
                localDataSource.upsert(entity = capture(entitySlot))
            } returns Unit

            // ----- Act -----
            repository.setDeadline(
                bookId = 3,
                deadlineDate = deadlineDate,
                current = 100,
                total = 300,
                unit = DeadlineUnit.PAGES,
                today = today,
            )

            // ----- Assert -----
            entitySlot.captured.initialPerDay shouldBe 200f
        }

        @Test
        fun `initialPerDay equals remaining when deadline is in the past`() = runTest {
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
                current = 50,
                total = 200,
                unit = DeadlineUnit.PAGES,
                today = today,
            )

            // ----- Assert -----
            entitySlot.captured.initialPerDay shouldBe 150f
        }

        @Test
        fun `remaining clamps to zero when current exceeds total`() = runTest {
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
                current = 400,
                total = 300,
                unit = DeadlineUnit.PAGES,
                today = today,
            )

            // ----- Assert -----
            entitySlot.captured.initialPerDay shouldBe 0f
        }

        @Test
        fun `unit PAGES is stored as string PAGES on the entity`() = runTest {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadlineDate = LocalDate.of(2026, 5, 20)
            val entitySlot = slot<BookDeadlineEntity>()

            coEvery {
                localDataSource.upsert(entity = capture(entitySlot))
            } returns Unit

            // ----- Act -----
            repository.setDeadline(
                bookId = 6,
                deadlineDate = deadlineDate,
                current = 0,
                total = 300,
                unit = DeadlineUnit.PAGES,
                today = today,
            )

            // ----- Assert -----
            entitySlot.captured.unit shouldBe "PAGES"
        }

        @Test
        fun `unit SECONDS is stored as string SECONDS on the entity`() = runTest {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadlineDate = LocalDate.of(2026, 5, 20)
            val entitySlot = slot<BookDeadlineEntity>()

            coEvery {
                localDataSource.upsert(entity = capture(entitySlot))
            } returns Unit

            // ----- Act -----
            repository.setDeadline(
                bookId = 7,
                deadlineDate = deadlineDate,
                current = 0,
                total = 18000,
                unit = DeadlineUnit.SECONDS,
                today = today,
            )

            // ----- Assert -----
            entitySlot.captured.unit shouldBe "SECONDS"
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
