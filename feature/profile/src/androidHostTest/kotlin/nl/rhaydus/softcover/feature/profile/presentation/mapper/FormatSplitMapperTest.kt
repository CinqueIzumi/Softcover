package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chart.ProportionChartVariant
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.FormatSplit
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FormatSplitMapperTest {
    private fun split(
        print: Int = 0,
        ebook: Int = 0,
        audio: Int = 0,
        both: Int = 0,
        unknown: Int = 0,
    ): FormatSplit = FormatSplit(
        counts = mapOf(
            ReadingFormat.Physical to print,
            ReadingFormat.Ebook to ebook,
            ReadingFormat.Audio to audio,
            ReadingFormat.Both to both,
        ),
        unknownCount = unknown,
    )

    private fun caption(
        split: FormatSplit?,
        booksRead: Int = 10,
        scope: StatsScope = StatsScope.AllTime,
    ): String = formatSplitCaption(
        split = split,
        booksRead = booksRead,
        scope = scope,
    )

    @Nested
    inner class Caption {
        @Test
        fun `a null split shows the stand-in caption`() {
            // ----- Arrange -----
            val split: FormatSplit? = null

            // ----- Act -----
            val result = caption(split)

            // ----- Assert -----
            result shouldBe "Mostly paper, with a third on a screen."
        }

        @Test
        fun `no finished books in a bounded scope names the scope`() {
            // ----- Arrange -----
            val split = split()

            // ----- Act -----
            val result = caption(
                split = split,
                booksRead = 0,
                scope = StatsScope.CalendarYear(2024),
            )

            // ----- Assert -----
            result shouldBe "No finished books in 2024."
        }

        @Test
        fun `no finished books all time invites finishing a few`() {
            // ----- Arrange -----
            val split = split()

            // ----- Act -----
            val result = caption(
                split = split,
                booksRead = 0,
            )

            // ----- Assert -----
            result shouldBe "Finish a few books and the formats you read in show up here."
        }

        @Test
        fun `only unknown formats say none is known`() {
            // ----- Arrange -----
            val split = split(unknown = 4)

            // ----- Act -----
            val result = caption(split)

            // ----- Assert -----
            result shouldBe "None of these books has a known format."
        }

        @Test
        fun `unknown at exactly half says most have no known format`() {
            // ----- Arrange -----
            val split = split(
                print = 3,
                unknown = 3,
            )

            // ----- Act -----
            val result = caption(split)

            // ----- Assert -----
            result shouldBe "At least half of these books have no known format."
        }

        @Test
        fun `a single known format and nothing unknown says only that format`() {
            // ----- Arrange -----
            val split = split(print = 5)

            // ----- Act -----
            val result = caption(split)

            // ----- Assert -----
            result shouldBe "Only paper."
        }

        @Test
        fun `print and audio alone uses its own noun`() {
            // ----- Arrange -----
            val split = split(both = 5)

            // ----- Act -----
            val result = caption(split)

            // ----- Assert -----
            result shouldBe "Only print and audio."
        }

        @Test
        fun `a single known format beside some unknown says mostly that format`() {
            // ----- Arrange -----
            val split = split(
                print = 6,
                unknown = 2,
            )

            // ----- Act -----
            val result = caption(split)

            // ----- Assert -----
            result shouldBe "Mostly paper."
        }

        @Test
        fun `a lead over half with a third second names the third on a screen`() {
            // ----- Arrange -----
            val split = split(
                print = 52,
                ebook = 31,
                audio = 17,
            )

            // ----- Act -----
            val result = caption(split)

            // ----- Assert -----
            result shouldBe "Mostly paper, with a third on a screen."
        }

        @Test
        fun `a second at nearly half is called nearly half`() {
            // ----- Arrange -----
            val split = split(
                print = 55,
                ebook = 45,
            )

            // ----- Act -----
            val result = caption(split)

            // ----- Assert -----
            result shouldBe "Mostly paper, with nearly half on a screen."
        }

        @Test
        fun `a lead of exactly half is a split rather than mostly`() {
            // ----- Arrange -----
            val split = split(
                print = 5,
                ebook = 5,
            )

            // ----- Act -----
            val result = caption(split)

            // ----- Assert -----
            result shouldBe "Split between paper and ebooks."
        }

        @Test
        fun `a second of a quarter on paper follows an ebook lead`() {
            // ----- Arrange -----
            val split = split(
                print = 25,
                ebook = 60,
                audio = 15,
            )

            // ----- Act -----
            val result = caption(split)

            // ----- Assert -----
            result shouldBe "Mostly ebooks, with a quarter on paper."
        }

        @Test
        fun `a second of a fifth is phrased as audiobooks`() {
            // ----- Arrange -----
            val split = split(
                print = 70,
                ebook = 10,
                audio = 20,
            )

            // ----- Act -----
            val result = caption(split)

            // ----- Assert -----
            result shouldBe "Mostly paper, with a fifth as audiobooks."
        }

        @Test
        fun `a second under fifteen percent is left out`() {
            // ----- Arrange -----
            val split = split(
                print = 90,
                ebook = 10,
            )

            // ----- Act -----
            val result = caption(split)

            // ----- Assert -----
            result shouldBe "Mostly paper."
        }

        @Test
        fun `a lead under half says split between the top two`() {
            // ----- Arrange -----
            val split = split(
                print = 40,
                ebook = 35,
                audio = 25,
            )

            // ----- Act -----
            val result = caption(split)

            // ----- Assert -----
            result shouldBe "Split between paper and ebooks."
        }

        @Test
        fun `a tie for the lead goes to print before ebook`() {
            // ----- Arrange -----
            val split = split(
                print = 40,
                ebook = 40,
                audio = 20,
            )

            // ----- Act -----
            val result = caption(split)

            // ----- Assert -----
            result shouldBe "Split between paper and ebooks."
        }

        @Test
        fun `a tie for the lead goes to ebook before audiobook`() {
            // ----- Arrange -----
            val split = split(
                ebook = 40,
                audio = 40,
                both = 20,
            )

            // ----- Act -----
            val result = caption(split)

            // ----- Assert -----
            result shouldBe "Split between ebooks and audiobooks."
        }
    }

    @Nested
    inner class Chart {
        @Test
        fun `a null split gives the loading chart`() {
            // ----- Arrange -----
            val split: FormatSplit? = null

            // ----- Act -----
            val chart = formatSplitChart(split)

            // ----- Assert -----
            chart.isLoading shouldBe true
            chart.variant shouldBe ProportionChartVariant.Keys
        }

        @Test
        fun `segments follow the fixed order with unknown last and zero formats absent`() {
            // ----- Arrange -----
            val split = split(
                print = 2,
                audio = 4,
                both = 2,
                unknown = 2,
            )

            // ----- Act -----
            val chart = formatSplitChart(split)

            // ----- Assert -----
            chart.segments.map { it.label } shouldBe listOf("Print", "Audiobook", "Print and audio", "Unknown")
            chart.segments.map { it.tone } shouldBe
                listOf(ChartTone.Lead, ChartTone.Third, ChartTone.Fourth, ChartTone.Unknown)
            chart.segments.map { it.share } shouldBe listOf("20%", "40%", "20%", "20%")
            chart.isLoading shouldBe false
            chart.variant shouldBe ProportionChartVariant.Keys
        }

        @Test
        fun `ebook takes the second tone`() {
            // ----- Arrange -----
            val split = split(
                print = 1,
                ebook = 1,
            )

            // ----- Act -----
            val chart = formatSplitChart(split)

            // ----- Assert -----
            chart.segments.map { it.tone } shouldBe listOf(ChartTone.Lead, ChartTone.Second)
        }

        @Test
        fun `shares that do not divide evenly still sum to one hundred`() {
            // ----- Arrange -----
            val split = split(
                print = 1,
                ebook = 1,
                audio = 1,
            )

            // ----- Act -----
            val chart = formatSplitChart(split)

            // ----- Assert -----
            chart.segments.map { it.share.removeSuffix("%").toInt() }.sum() shouldBe 100
        }

        @Test
        fun `a tiny non-zero share shows one percent`() {
            // ----- Arrange -----
            val split = split(
                print = 199,
                ebook = 1,
            )

            // ----- Act -----
            val chart = formatSplitChart(split)

            // ----- Assert -----
            chart.segments.map { it.share } shouldBe listOf("99%", "1%")
        }

        @Test
        fun `fractions are the share of the total`() {
            // ----- Arrange -----
            val split = split(
                print = 3,
                unknown = 1,
            )

            // ----- Act -----
            val chart = formatSplitChart(split)

            // ----- Assert -----
            chart.segments.map { it.fraction } shouldBe listOf(0.75f, 0.25f)
        }
    }

    @Nested
    inner class HasFormatSplit {
        @Test
        fun `is false while loading`() {
            // ----- Arrange -----
            val split: FormatSplit? = null

            // ----- Act -----
            val result = hasFormatSplit(split)

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false when only unknown formats exist`() {
            // ----- Arrange -----
            val split = split(unknown = 3)

            // ----- Act -----
            val result = hasFormatSplit(split)

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is true with any known format`() {
            // ----- Arrange -----
            val split = split(
                audio = 1,
                unknown = 3,
            )

            // ----- Act -----
            val result = hasFormatSplit(split)

            // ----- Assert -----
            result shouldBe true
        }
    }
}
