package nl.rhaydus.softcover.feature.deadlines.data.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.feature.deadlines.data.model.BookDeadlineEntity
import nl.rhaydus.softcover.feature.deadlines.domain.model.BookDeadline
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BookDeadlineMappersTest {

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

    private fun buildDomain(
        bookId: Int = 1,
        deadlineDate: LocalDate = LocalDate.of(2026, 5, 1),
        setAt: LocalDate = LocalDate.of(2026, 4, 1),
        initialPagesPerDay: Float = 10f,
    ) = BookDeadline(
        bookId = bookId,
        deadlineDate = deadlineDate,
        setAt = setAt,
        initialPagesPerDay = initialPagesPerDay,
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
                initialPagesPerDay = 12.5f,
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.bookId shouldBe 42
            result.deadlineDate shouldBe LocalDate.of(2026, 7, 15)
            result.setAt shouldBe LocalDate.of(2026, 4, 20)
            result.initialPagesPerDay shouldBe 12.5f
        }

        @Test
        fun `parses a leap-day date correctly`() {
            // ----- Arrange -----
            val entity = buildEntity(deadlineDate = "2024-02-29", setAt = "2024-01-01")

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.deadlineDate shouldBe LocalDate.of(2024, 2, 29)
        }

        @Test
        fun `parses a year-boundary date correctly`() {
            // ----- Arrange -----
            val entity = buildEntity(deadlineDate = "2025-12-31", setAt = "2025-12-01")

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.deadlineDate shouldBe LocalDate.of(2025, 12, 31)
            result.setAt shouldBe LocalDate.of(2025, 12, 1)
        }

        @Test
        fun `parses first day of year correctly`() {
            // ----- Arrange -----
            val entity = buildEntity(deadlineDate = "2026-01-01", setAt = "2025-12-31")

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.deadlineDate shouldBe LocalDate.of(2026, 1, 1)
            result.setAt shouldBe LocalDate.of(2025, 12, 31)
        }
    }

    @Nested
    inner class ToEntity {

        @Test
        fun `maps all fields correctly for a standard date`() {
            // ----- Arrange -----
            val domain = buildDomain(
                bookId = 7,
                deadlineDate = LocalDate.of(2026, 8, 10),
                setAt = LocalDate.of(2026, 4, 5),
                initialPagesPerDay = 8f,
            )

            // ----- Act -----
            val result = domain.toEntity()

            // ----- Assert -----
            result.bookId shouldBe 7
            result.deadlineDate shouldBe "2026-08-10"
            result.setAt shouldBe "2026-04-05"
            result.initialPagesPerDay shouldBe 8f
        }

        @Test
        fun `formats a leap-day date correctly`() {
            // ----- Arrange -----
            val domain = buildDomain(
                deadlineDate = LocalDate.of(2024, 2, 29),
                setAt = LocalDate.of(2024, 1, 1),
            )

            // ----- Act -----
            val result = domain.toEntity()

            // ----- Assert -----
            result.deadlineDate shouldBe "2024-02-29"
        }

        @Test
        fun `formats a year-boundary date correctly`() {
            // ----- Arrange -----
            val domain = buildDomain(
                deadlineDate = LocalDate.of(2025, 12, 31),
                setAt = LocalDate.of(2025, 12, 1),
            )

            // ----- Act -----
            val result = domain.toEntity()

            // ----- Assert -----
            result.deadlineDate shouldBe "2025-12-31"
            result.setAt shouldBe "2025-12-01"
        }
    }

    @Nested
    inner class RoundTrip {

        @Test
        fun `entity to domain to entity preserves all fields`() {
            // ----- Arrange -----
            val original = buildEntity(
                bookId = 99,
                deadlineDate = "2026-11-30",
                setAt = "2026-04-20",
                initialPagesPerDay = 15.75f,
            )

            // ----- Act -----
            val result = original.toDomain().toEntity()

            // ----- Assert -----
            result shouldBe original
        }

        @Test
        fun `domain to entity to domain preserves all fields`() {
            // ----- Arrange -----
            val original = buildDomain(
                bookId = 55,
                deadlineDate = LocalDate.of(2026, 3, 31),
                setAt = LocalDate.of(2026, 1, 15),
                initialPagesPerDay = 5f,
            )

            // ----- Act -----
            val result = original.toEntity().toDomain()

            // ----- Assert -----
            result shouldBe original
        }

        @Test
        fun `round-trip preserves leap-day date`() {
            // ----- Arrange -----
            val original = buildEntity(
                deadlineDate = "2024-02-29",
                setAt = "2024-02-01",
            )

            // ----- Act -----
            val result = original.toDomain().toEntity()

            // ----- Assert -----
            result.deadlineDate shouldBe "2024-02-29"
        }
    }
}
