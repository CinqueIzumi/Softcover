package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.statistic.StatTileVariant
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.StatsWindow
import nl.rhaydus.softcover.core.profile.domain.model.LengthBand
import nl.rhaydus.softcover.core.profile.domain.model.TimeToFinish
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TimeToFinishMapperTest {
    private fun timeToFinish(
        readCount: Int = 5,
        medianDays: Int? = 9,
        bandMedianDays: Map<LengthBand, Int> = emptyMap(),
        excludedReadCount: Int = 0,
    ): TimeToFinish = TimeToFinish(
        readCount = readCount,
        medianDays = medianDays,
        bandMedianDays = bandMedianDays,
        excludedReadCount = excludedReadCount,
    )

    @Nested
    inner class TimeToFinishUiModelTests {
        @Test
        fun `null input yields the loading stand-in with four masked tiles`() {
            // ----- Arrange -----
            val input: TimeToFinish? = null

            // ----- Act -----
            val result = timeToFinishUiModel(
                timeToFinish = input,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.isLoading shouldBe true
            result.tiles.map { it.eyebrow } shouldBe
                listOf("All books", "Under 250 pages", "250 to 450 pages", "Over 450 pages")
            result.tiles.map { it.stat.shouldNotBeNull().isLoading } shouldBe List(4) { true }
        }

        @Test
        fun `names only the typical book when fewer than two bands have reads`() {
            // ----- Arrange -----
            val input = timeToFinish(bandMedianDays = mapOf(LengthBand.OVER_450 to 23))

            // ----- Act -----
            val result = timeToFinishUiModel(
                timeToFinish = input,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.framing shouldBe "Your typical book took 9 days."
            result.hasReads shouldBe true
            result.isLoading shouldBe false
        }

        @Test
        fun `uses the singular for a typical book that took one day`() {
            // ----- Arrange -----
            val input = timeToFinish(medianDays = 1)

            // ----- Act -----
            val result = timeToFinishUiModel(
                timeToFinish = input,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.framing shouldBe "Your typical book took 1 day."
        }

        @Test
        fun `names the middle band when it is the longest books with reads`() {
            // ----- Arrange -----
            val input = timeToFinish(
                bandMedianDays = mapOf(
                    LengthBand.UNDER_250 to 40,
                    LengthBand.FROM_250_TO_450 to 12,
                ),
            )

            // ----- Act -----
            val result = timeToFinishUiModel(
                timeToFinish = input,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.framing shouldBe "Your typical book took 9 days. Ones between 250 and 450 pages took 12."
        }

        @Test
        fun `names the over 450 band whenever it has reads, even with a lower median`() {
            // ----- Arrange -----
            val input = timeToFinish(
                bandMedianDays = mapOf(
                    LengthBand.UNDER_250 to 30,
                    LengthBand.FROM_250_TO_450 to 20,
                    LengthBand.OVER_450 to 10,
                ),
            )

            // ----- Act -----
            val result = timeToFinishUiModel(
                timeToFinish = input,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.framing shouldBe "Your typical book took 9 days. Ones over 450 pages took 10."
        }

        @Test
        fun `orders the tiles and masks the band that has no reads`() {
            // ----- Arrange -----
            val input = timeToFinish(
                bandMedianDays = mapOf(
                    LengthBand.UNDER_250 to 3,
                    LengthBand.OVER_450 to 20,
                ),
            )

            // ----- Act -----
            val result = timeToFinishUiModel(
                timeToFinish = input,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.tiles.map { it.eyebrow } shouldBe
                listOf("All books", "Under 250 pages", "250 to 450 pages", "Over 450 pages")
            result.tiles.map { it.stat?.value } shouldBe listOf(9.0, 3.0, null, 20.0)
            result.tiles[2].stat.shouldBeNull()
        }

        @Test
        fun `labels a tile with day when the value is one and days otherwise`() {
            // ----- Arrange -----
            val input = timeToFinish(
                medianDays = 1,
                bandMedianDays = mapOf(LengthBand.UNDER_250 to 2),
            )

            // ----- Act -----
            val result = timeToFinishUiModel(
                timeToFinish = input,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            val units = result.tiles.map { (it.variant as StatTileVariant.Compact).unit }
            units shouldBe listOf("day", "days", "days", "days")
        }

        @Test
        fun `counts the reads in the caption`() {
            // ----- Arrange -----
            val input = timeToFinish(readCount = 31)

            // ----- Act -----
            val result = timeToFinishUiModel(
                timeToFinish = input,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.caption shouldBe "Median calendar days from start to finish, paused time included, " +
                "across 31 reads with both dates. A reread counts on its own."
        }

        @Test
        fun `uses the singular read in the caption for a single read`() {
            // ----- Arrange -----
            val input = timeToFinish(readCount = 1)

            // ----- Act -----
            val result = timeToFinishUiModel(
                timeToFinish = input,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.caption shouldBe "Median calendar days from start to finish, paused time included, " +
                "across 1 read with both dates. A reread counts on its own."
        }
    }

    @Nested
    inner class EmptyScopeTests {
        private val noReads = timeToFinish(
            readCount = 0,
            medianDays = null,
        )

        private val onlyUndated = timeToFinish(
            readCount = 0,
            medianDays = null,
            excludedReadCount = 3,
        )

        @Test
        fun `drops the tiles and caption and flags no reads`() {
            // ----- Arrange -----
            val input = noReads

            // ----- Act -----
            val result = timeToFinishUiModel(
                timeToFinish = input,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.hasReads shouldBe false
            result.isLoading shouldBe false
            result.tiles.shouldBeEmpty()
            result.caption shouldBe ""
        }

        @Test
        fun `asks for finished books across all time when nothing is finished`() {
            // ----- Arrange -----
            val input = noReads

            // ----- Act -----
            val result = timeToFinishUiModel(
                timeToFinish = input,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.framing shouldBe "Finish a few books and how long they take shows up here."
        }

        @Test
        fun `names a rolling window when nothing was finished in it`() {
            // ----- Arrange -----
            val input = noReads

            // ----- Act -----
            val result = timeToFinishUiModel(
                timeToFinish = input,
                scope = StatsScope.Rolling(StatsWindow.LAST_6_MONTHS),
            )

            // ----- Assert -----
            result.framing shouldBe "No finished reads in the last 6 months."
        }

        @Test
        fun `names a calendar year when nothing was finished in it`() {
            // ----- Arrange -----
            val input = noReads

            // ----- Act -----
            val result = timeToFinishUiModel(
                timeToFinish = input,
                scope = StatsScope.CalendarYear(2025),
            )

            // ----- Assert -----
            result.framing shouldBe "No finished reads in 2025."
        }

        @Test
        fun `says no finished read has both dates across all time`() {
            // ----- Arrange -----
            val input = onlyUndated

            // ----- Act -----
            val result = timeToFinishUiModel(
                timeToFinish = input,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            result.framing shouldBe "None of the reads you finished has both a start and a finish date."
        }

        @Test
        fun `says no finished read has both dates within a rolling window`() {
            // ----- Arrange -----
            val input = onlyUndated

            // ----- Act -----
            val result = timeToFinishUiModel(
                timeToFinish = input,
                scope = StatsScope.Rolling(StatsWindow.LAST_30_DAYS),
            )

            // ----- Assert -----
            result.framing shouldBe
                "None of the reads you finished in the last 30 days has both a start and a finish date."
        }

        @Test
        fun `says no finished read has both dates within a calendar year`() {
            // ----- Arrange -----
            val input = onlyUndated

            // ----- Act -----
            val result = timeToFinishUiModel(
                timeToFinish = input,
                scope = StatsScope.CalendarYear(2024),
            )

            // ----- Assert -----
            result.framing shouldBe
                "None of the reads you finished in 2024 has both a start and a finish date."
        }
    }
}
