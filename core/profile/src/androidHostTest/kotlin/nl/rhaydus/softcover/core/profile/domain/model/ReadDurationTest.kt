package nl.rhaydus.softcover.core.profile.domain.model

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadDurationTest {
    private fun duration(
        startedOn: String,
        finishedOn: String,
    ) = ReadDuration(
        bookId = 1,
        readId = 1,
        startedOn = LocalDate.parse(startedOn),
        finishedOn = LocalDate.parse(finishedOn),
        pages = null,
    )

    @Nested
    inner class Days {
        @Test
        fun `a read started and finished on the same day is one day`() {
            // ----- Arrange -----
            val read = duration(
                startedOn = "2026-03-10",
                finishedOn = "2026-03-10",
            )

            // ----- Act & Assert -----
            read.days shouldBe 1
        }

        @Test
        fun `finishing the next day is two days`() {
            // ----- Arrange -----
            val read = duration(
                startedOn = "2026-03-10",
                finishedOn = "2026-03-11",
            )

            // ----- Act & Assert -----
            read.days shouldBe 2
        }

        @Test
        fun `a read spanning a month boundary counts every calendar day`() {
            // ----- Arrange -----
            val read = duration(
                startedOn = "2026-01-30",
                finishedOn = "2026-02-02",
            )

            // ----- Act & Assert -----
            read.days shouldBe 4
        }
    }
}
