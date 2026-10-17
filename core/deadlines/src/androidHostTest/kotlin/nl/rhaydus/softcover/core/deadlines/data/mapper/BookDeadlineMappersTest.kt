package nl.rhaydus.softcover.core.deadlines.data.mapper

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.database.model.BookDeadlineEntity
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookDeadlineMappersTest {
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

    private fun buildDomain(
        bookId: Int = 1,
        deadlineDate: LocalDate = LocalDate(
            2026,
            5,
            1,
        ),
        setAt: LocalDate = LocalDate(
            2026,
            4,
            1,
        ),
        initialPerDay: Float = 10f,
        unit: DeadlineUnit = DeadlineUnit.PAGES,
    ) = BookDeadline(
        bookId = bookId,
        deadlineDate = deadlineDate,
        setAt = setAt,
        initialPerDay = initialPerDay,
        unit = unit,
    )

    @Nested
    inner class ToDomain {
        @Test
        fun `maps all fields correctly for a standard date`() {
            // ----- Arrange -----
            val entity = buildEntity(
                bookId = 42,
                deadlineDate = "2026-07-15",
                setAt = "2026-04-20",
                initialPerDay = 12.5f,
                unit = "PAGES",
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.bookId shouldBe 42
            result.deadlineDate shouldBe LocalDate(
                2026,
                7,
                15,
            )
            result.setAt shouldBe LocalDate(
                2026,
                4,
                20,
            )
            result.initialPerDay shouldBe 12.5f
            result.unit shouldBe DeadlineUnit.PAGES
        }

        @Test
        fun `maps unit SECONDS string to DeadlineUnit SECONDS`() {
            // ----- Arrange -----
            val entity = buildEntity(unit = "SECONDS")

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.unit shouldBe DeadlineUnit.SECONDS
        }

        @Test
        fun `bogus unit string falls back to DeadlineUnit PAGES`() {
            // ----- Arrange -----
            val entity = buildEntity(unit = "UNKNOWN_VALUE")

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.unit shouldBe DeadlineUnit.PAGES
        }
    }

    @Nested
    inner class ToEntity {
        @Test
        fun `maps all fields correctly for a standard date`() {
            // ----- Arrange -----
            val domain = buildDomain(
                bookId = 7,
                deadlineDate = LocalDate(
                    2026,
                    8,
                    10,
                ),
                setAt = LocalDate(
                    2026,
                    4,
                    5,
                ),
                initialPerDay = 8f,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Act -----
            val result = domain.toEntity()

            // ----- Assert -----
            result.bookId shouldBe 7
            result.deadlineDate shouldBe "2026-08-10"
            result.setAt shouldBe "2026-04-05"
            result.initialPerDay shouldBe 8f
            result.unit shouldBe "PAGES"
        }
    }

    @Nested
    inner class RoundTrip {
        @Test
        fun `entity to domain to entity preserves all fields including PAGES unit`() {
            // ----- Arrange -----
            val original = buildEntity(
                bookId = 99,
                deadlineDate = "2026-11-30",
                setAt = "2026-04-20",
                initialPerDay = 15.75f,
                unit = "PAGES",
            )

            // ----- Act -----
            val result = original.toDomain().toEntity()

            // ----- Assert -----
            result shouldBe original
        }

        @Test
        fun `domain to entity to domain preserves SECONDS unit`() {
            // ----- Arrange -----
            val original = buildDomain(
                bookId = 56,
                deadlineDate = LocalDate(
                    2026,
                    3,
                    31,
                ),
                setAt = LocalDate(
                    2026,
                    1,
                    15,
                ),
                initialPerDay = 600f,
                unit = DeadlineUnit.SECONDS,
            )

            // ----- Act -----
            val result = original.toEntity().toDomain()

            // ----- Assert -----
            result shouldBe original
        }
    }
}
