package nl.rhaydus.softcover.core.uibinding.quote

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.QuotePosition
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuotePositionLabelTest {
    @Nested
    inner class ToPositionLabel {
        @Test
        fun `labels a page position with its page number`() {
            // ----- Arrange -----
            val position = QuotePosition.pages(
                page = 214,
                totalPages = 437,
            )

            // ----- Act -----
            val result = position.toPositionLabel()

            // ----- Assert -----
            result shouldBe "p. 214"
        }

        @Test
        fun `labels a page position without a total the same way`() {
            // ----- Arrange -----
            val position = QuotePosition.Pages(
                page = 9,
                totalPages = null,
                percent = null,
            )

            // ----- Act -----
            val result = position.toPositionLabel()

            // ----- Assert -----
            result shouldBe "p. 9"
        }

        @Test
        fun `labels a whole percent position`() {
            // ----- Arrange -----
            val position = QuotePosition.Percent(percent = 53.0)

            // ----- Act -----
            val result = position.toPositionLabel()

            // ----- Assert -----
            result shouldBe "53%"
        }

        @Test
        fun `rounds a fractional percent up from the half`() {
            // ----- Arrange -----
            val position = QuotePosition.Percent(percent = 52.5)

            // ----- Act -----
            val result = position.toPositionLabel()

            // ----- Assert -----
            result shouldBe "53%"
        }

        @Test
        fun `rounds a fractional percent down below the half`() {
            // ----- Arrange -----
            val position = QuotePosition.Percent(percent = 52.4)

            // ----- Act -----
            val result = position.toPositionLabel()

            // ----- Assert -----
            result shouldBe "52%"
        }

        @Test
        fun `labels a time position past an hour as hours minutes seconds`() {
            // ----- Arrange -----
            val position = QuotePosition.seconds(
                seconds = 3723,
                totalSeconds = 36000,
            )

            // ----- Act -----
            val result = position.toPositionLabel()

            // ----- Assert -----
            result shouldBe "1:02:03"
        }

        @Test
        fun `labels a time position under an hour with a zero hour`() {
            // ----- Arrange -----
            val position = QuotePosition.seconds(
                seconds = 125,
                totalSeconds = 36000,
            )

            // ----- Act -----
            val result = position.toPositionLabel()

            // ----- Assert -----
            result shouldBe "0:02:05"
        }
    }

    @Nested
    inner class ToQuoteClockText {
        @Test
        fun `renders zero seconds as a full clock`() {
            // ----- Arrange -----
            val seconds = 0

            // ----- Act -----
            val result = seconds.toQuoteClockText()

            // ----- Assert -----
            result shouldBe "0:00:00"
        }

        @Test
        fun `keeps counting hours past ten`() {
            // ----- Arrange -----
            val seconds = 11 * 3600 + 59 * 60 + 59

            // ----- Act -----
            val result = seconds.toQuoteClockText()

            // ----- Assert -----
            result shouldBe "11:59:59"
        }
    }
}
