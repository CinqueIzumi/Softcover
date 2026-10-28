package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.DateTimeUnit
import nl.rhaydus.softcover.core.profile.domain.model.LengthBand
import nl.rhaydus.softcover.core.profile.domain.model.ReadDuration
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TimeToFinishMapperTest {
    private fun read(
        days: Int,
        pages: Int? = null,
    ): ReadDuration {
        val start = LocalDate.parse("2026-01-01")

        return ReadDuration(
            bookId = 1,
            readId = 1,
            startedOn = start,
            finishedOn = start.plus(
                days - 1,
                DateTimeUnit.DAY,
            ),
            pages = pages,
        )
    }

    @Nested
    inner class ToTimeToFinish {
        @Test
        fun `no reads has no median, no band medians and carries the excluded count`() {
            // ----- Arrange -----
            val reads = emptyList<ReadDuration>()

            // ----- Act -----
            val result = reads.toTimeToFinish(excludedReadCount = 3)

            // ----- Assert -----
            listOf(result.readCount, result.medianDays, result.bandMedianDays, result.excludedReadCount) shouldBe
                listOf(0, null, emptyMap<LengthBand, Int>(), 3)
        }

        @Test
        fun `an odd count takes the middle value of the sorted days`() {
            // ----- Arrange -----
            val reads = listOf(read(days = 9), read(days = 1), read(days = 4))

            // ----- Act -----
            val result = reads.toTimeToFinish(excludedReadCount = 0)

            // ----- Assert -----
            result.medianDays shouldBe 4
        }

        @Test
        fun `an even count averages the middle two`() {
            // ----- Arrange -----
            val reads = listOf(read(days = 2), read(days = 10), read(days = 4), read(days = 8))

            // ----- Act -----
            val result = reads.toTimeToFinish(excludedReadCount = 0)

            // ----- Assert -----
            result.medianDays shouldBe 6
        }

        @Test
        fun `an even count with a half rounds up`() {
            // ----- Arrange -----
            val reads = listOf(read(days = 3), read(days = 4))

            // ----- Act -----
            val result = reads.toTimeToFinish(excludedReadCount = 0)

            // ----- Assert -----
            result.medianDays shouldBe 4
        }

        @Test
        fun `a single read is its own median`() {
            // ----- Arrange -----
            val reads = listOf(read(days = 7))

            // ----- Act -----
            val result = reads.toTimeToFinish(excludedReadCount = 0)

            // ----- Assert -----
            result.medianDays shouldBe 7
        }

        @Test
        fun `each band gets its own median and empty bands have no key`() {
            // ----- Arrange -----
            val reads = listOf(
                read(
                    days = 2,
                    pages = 100,
                ),
                read(
                    days = 6,
                    pages = 200,
                ),
                read(
                    days = 20,
                    pages = 500,
                ),
            )

            // ----- Act -----
            val result = reads.toTimeToFinish(excludedReadCount = 0)

            // ----- Assert -----
            result.bandMedianDays shouldBe mapOf(LengthBand.UNDER_250 to 4, LengthBand.OVER_450 to 20)
        }

        @Test
        fun `reads with unknown pages count in the overall median only`() {
            // ----- Arrange -----
            val reads = listOf(
                read(
                    days = 1,
                    pages = null,
                ),
                read(
                    days = 3,
                    pages = null,
                ),
                read(
                    days = 5,
                    pages = 300,
                ),
            )

            // ----- Act -----
            val result = reads.toTimeToFinish(excludedReadCount = 0)

            // ----- Assert -----
            listOf(result.readCount, result.medianDays) shouldBe listOf(3, 3)
            result.bandMedianDays shouldBe mapOf(LengthBand.FROM_250_TO_450 to 5)
        }
    }
}
