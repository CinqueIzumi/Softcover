package nl.rhaydus.softcover.core.personal.data.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.QuotePosition
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuoteMetadataMapperTest {
    @Nested
    inner class ToQuoteMetadata {
        @Test
        fun `writes an empty object when there is no position`() {
            // ----- Arrange -----
            val position: QuotePosition? = null

            // ----- Act -----
            val result = position.toQuoteMetadata()

            // ----- Assert -----
            result shouldBe emptyMap()
        }

        @Test
        fun `writes a pages position with value, possible and percent`() {
            // ----- Arrange -----
            val position = QuotePosition.pages(
                page = 24,
                totalPages = 400,
            )

            // ----- Act -----
            val result = position.toQuoteMetadata()

            // ----- Assert -----
            result shouldBe mapOf(
                "position" to mapOf(
                    "type" to "pages",
                    "value" to 24,
                    "possible" to 400,
                    "percent" to 6.0,
                ),
            )
        }

        @Test
        fun `omits possible and percent from a pages position that has neither`() {
            // ----- Arrange -----
            val position = QuotePosition.Pages(
                page = 24,
                totalPages = null,
                percent = null,
            )

            // ----- Act -----
            val result = position.toQuoteMetadata()

            // ----- Assert -----
            result shouldBe mapOf(
                "position" to mapOf(
                    "type" to "pages",
                    "value" to 24,
                ),
            )
        }

        @Test
        fun `writes a whole percent position as integers over a possible of 100`() {
            // ----- Arrange -----
            val position = QuotePosition.Percent(percent = 78.0)

            // ----- Act -----
            val result = position.toQuoteMetadata()

            // ----- Assert -----
            result shouldBe mapOf(
                "position" to mapOf(
                    "type" to "percent",
                    "value" to 78,
                    "percent" to 78,
                    "possible" to 100,
                ),
            )
        }

        @Test
        fun `keeps the fraction of a fractional percent position`() {
            // ----- Arrange -----
            val position = QuotePosition.Percent(percent = 12.5)

            // ----- Act -----
            val result = position.toQuoteMetadata()

            // ----- Assert -----
            result shouldBe mapOf(
                "position" to mapOf(
                    "type" to "percent",
                    "value" to 12.5,
                    "percent" to 12.5,
                    "possible" to 100,
                ),
            )
        }

        @Test
        fun `writes a seconds position with value, possible and percent`() {
            // ----- Arrange -----
            val position = QuotePosition.seconds(
                seconds = 3600,
                totalSeconds = 41280,
            )

            // ----- Act -----
            val result = position.toQuoteMetadata()

            // ----- Assert -----
            result shouldBe mapOf(
                "position" to mapOf(
                    "type" to "seconds",
                    "value" to 3600,
                    "possible" to 41280,
                    "percent" to 3600.0 / 41280 * 100,
                ),
            )
        }
    }

    @Nested
    inner class QuotePositionFromMetadata {
        @Test
        fun `reads a pages position back`() {
            // ----- Arrange -----
            val metadata = mapOf(
                "position" to mapOf(
                    "type" to "pages",
                    "value" to 24,
                    "possible" to 400,
                    "percent" to 6.0,
                ),
            )

            // ----- Act -----
            val result = quotePositionFromMetadata(metadata = metadata)

            // ----- Assert -----
            result shouldBe QuotePosition.Pages(
                page = 24,
                totalPages = 400,
                percent = 6.0,
            )
        }

        @Test
        fun `reads a seconds position back`() {
            // ----- Arrange -----
            val metadata = mapOf(
                "position" to mapOf(
                    "type" to "seconds",
                    "value" to 3600,
                    "possible" to 41280,
                    "percent" to 8.7,
                ),
            )

            // ----- Act -----
            val result = quotePositionFromMetadata(metadata = metadata)

            // ----- Assert -----
            result shouldBe QuotePosition.Seconds(
                seconds = 3600,
                totalSeconds = 41280,
                percent = 8.7,
            )
        }

        @Test
        fun `reads a percent position back`() {
            // ----- Arrange -----
            val metadata = mapOf(
                "position" to mapOf(
                    "type" to "percent",
                    "value" to 78,
                    "percent" to 78,
                    "possible" to 100,
                ),
            )

            // ----- Act -----
            val result = quotePositionFromMetadata(metadata = metadata)

            // ----- Assert -----
            result shouldBe QuotePosition.Percent(percent = 78.0)
        }

        @Test
        fun `falls back to value for a percent position without a percent`() {
            // ----- Arrange -----
            val metadata = mapOf(
                "position" to mapOf(
                    "type" to "percent",
                    "value" to 40,
                ),
            )

            // ----- Act -----
            val result = quotePositionFromMetadata(metadata = metadata)

            // ----- Assert -----
            result shouldBe QuotePosition.Percent(percent = 40.0)
        }

        @Test
        fun `keeps a null percent and a missing possible on a pages position`() {
            // ----- Arrange -----
            val metadata = mapOf(
                "position" to mapOf(
                    "type" to "pages",
                    "value" to 24,
                    "percent" to null,
                ),
            )

            // ----- Act -----
            val result = quotePositionFromMetadata(metadata = metadata)

            // ----- Assert -----
            result shouldBe QuotePosition.Pages(
                page = 24,
                totalPages = null,
                percent = null,
            )
        }

        @Test
        fun `accepts numbers that arrive as strings`() {
            // ----- Arrange -----
            val metadata = mapOf(
                "position" to mapOf(
                    "type" to "pages",
                    "value" to "24",
                    "possible" to "400",
                    "percent" to "6.0",
                ),
            )

            // ----- Act -----
            val result = quotePositionFromMetadata(metadata = metadata)

            // ----- Assert -----
            result shouldBe QuotePosition.Pages(
                page = 24,
                totalPages = 400,
                percent = 6.0,
            )
        }

        @Test
        fun `returns null for an unknown position type`() {
            // ----- Arrange -----
            val metadata = mapOf(
                "position" to mapOf(
                    "type" to "chapters",
                    "value" to 3,
                ),
            )

            // ----- Act -----
            val result = quotePositionFromMetadata(metadata = metadata)

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `returns null when the value is missing`() {
            // ----- Arrange -----
            val metadata = mapOf(
                "position" to mapOf(
                    "type" to "pages",
                    "possible" to 400,
                ),
            )

            // ----- Act -----
            val result = quotePositionFromMetadata(metadata = metadata)

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `returns null when there is no position`() {
            // ----- Arrange -----
            val metadata = emptyMap<String, Any?>()

            // ----- Act -----
            val result = quotePositionFromMetadata(metadata = metadata)

            // ----- Assert -----
            result.shouldBeNull()
        }
    }
}
