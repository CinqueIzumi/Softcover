package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuotePositionTest {
    @Nested
    inner class PagesFactory {
        @Test
        fun `computes percent as the share of total pages`() {
            // ----- Arrange -----
            val page = 24
            val totalPages = 400

            // ----- Act -----
            val result = QuotePosition.pages(
                page = page,
                totalPages = totalPages,
            )

            // ----- Assert -----
            result shouldBe QuotePosition.Pages(
                page = 24,
                totalPages = 400,
                percent = 6.0,
            )
        }
    }

    @Nested
    inner class SecondsFactory {
        @Test
        fun `computes percent as the share of total seconds`() {
            // ----- Arrange -----
            val seconds = 900
            val totalSeconds = 3600

            // ----- Act -----
            val result = QuotePosition.seconds(
                seconds = seconds,
                totalSeconds = totalSeconds,
            )

            // ----- Assert -----
            result shouldBe QuotePosition.Seconds(
                seconds = 900,
                totalSeconds = 3600,
                percent = 25.0,
            )
        }
    }
}
