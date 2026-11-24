package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.domain.model.MoodWord
import nl.rhaydus.softcover.core.domain.model.Pace
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.WritingFlag
import nl.rhaydus.softcover.core.domain.model.WritingStyle
import nl.rhaydus.softcover.core.profile.domain.model.HowYouReadStats
import nl.rhaydus.softcover.core.profile.domain.model.MoodProfile
import nl.rhaydus.softcover.core.profile.domain.model.MoodScore
import nl.rhaydus.softcover.core.profile.domain.model.PlotCharacterSplit
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HowYouReadMapperTest {
    private fun stats(
        plot: Int = 0,
        mix: Int = 0,
        character: Int = 0,
        paceCounts: Map<Pace, Int> = emptyMap(),
        styles: Map<WritingStyle, Int> = emptyMap(),
        flags: Map<WritingFlag, Int> = emptyMap(),
        booksWithPace: Int = 0,
        booksWithStyle: Int = 0,
        booksWithFlag: Int = 0,
        booksWithMoods: Int = 0,
        topMoods: List<MoodScore> = emptyList(),
    ): HowYouReadStats = HowYouReadStats(
        plotCharacter = PlotCharacterSplit(
            plot = plot,
            mix = mix,
            character = character,
        ),
        paceCounts = paceCounts,
        writingStyleCounts = styles,
        writingFlagCounts = flags,
        booksWithPace = booksWithPace,
        booksWithWritingStyle = booksWithStyle,
        booksWithWritingFlag = booksWithFlag,
        moods = MoodProfile(
            booksWithMoods = booksWithMoods,
            topMoods = topMoods,
        ),
    )

    private fun mood(
        text: String,
        strength: Double?,
    ): MoodScore = MoodScore(
        word = MoodWord.Own(text),
        bookCount = 1,
        averageStrength = strength,
    )

    @Nested
    inner class Loading {
        @Test
        fun `returns the masked stand-in when stats is null`() {
            // ----- Arrange -----
            val stats: HowYouReadStats? = null

            // ----- Act -----
            val result = howYouReadUiModel(
                stats = stats,
                scope = StatsScope.AllTime,
            )

            // ----- Assert -----
            listOf(result.isLoading, result.split?.chart?.isLoading, result.emptyLine) shouldBe
                listOf(true, true, null)
        }
    }

    @Nested
    inner class Split {
        @Test
        fun `names character first and tones the parts Lead, Second, Third by fixed position`() {
            // ----- Arrange -----
            val stats = stats(
                plot = 1,
                mix = 1,
                character = 2,
            )

            // ----- Act -----
            val split = howYouReadUiModel(
                stats,
                StatsScope.AllTime,
            ).split

            // ----- Assert -----
            listOf(
                split?.title,
                split?.chart?.segments?.map { it.share },
                split?.chart?.segments?.map { it.tone },
            ) shouldBe listOf(
                "Character first",
                listOf("25%", "25%", "50%"),
                listOf(ChartTone.Lead, ChartTone.Second, ChartTone.Third),
            )
        }

        @Test
        fun `names plot first and quotes the plot share of the counted books`() {
            // ----- Arrange -----
            val stats = stats(
                plot = 3,
                character = 1,
            )

            // ----- Act -----
            val split = howYouReadUiModel(
                stats,
                StatsScope.AllTime,
            ).split

            // ----- Assert -----
            split?.title shouldBe "Plot first"
            split?.caption shouldContain "Your reading leaned plot-driven: 75% of the 4 books"
        }

        @Test
        fun `calls a plot and character tie a mix and drops the empty mix part`() {
            // ----- Arrange -----
            val stats = stats(
                plot = 2,
                character = 2,
            )

            // ----- Act -----
            val split = howYouReadUiModel(
                stats,
                StatsScope.AllTime,
            ).split

            // ----- Assert -----
            listOf(split?.title, split?.chart?.segments?.map { it.label }) shouldBe
                listOf("A mix", listOf("Plot", "Character"))

            split?.caption shouldContain "50% of the 4 books"
        }

        @Test
        fun `calls the mix bucket a mix when it is the largest and quotes its share`() {
            // ----- Arrange -----
            val stats = stats(
                plot = 1,
                mix = 2,
                character = 1,
            )

            // ----- Act -----
            val split = howYouReadUiModel(
                stats,
                StatsScope.AllTime,
            ).split

            // ----- Assert -----
            split?.title shouldBe "A mix"
            split?.caption shouldContain "Your reading was a mix of plot and character: 50% of the 4 books"
        }

        @Test
        fun `shares sum to 100 when the counts divide unevenly`() {
            // ----- Arrange -----
            val stats = stats(
                plot = 1,
                mix = 1,
                character = 1,
            )

            // ----- Act -----
            val shares = howYouReadUiModel(
                stats,
                StatsScope.AllTime,
            ).split?.chart?.segments
                ?.map { it.share.removeSuffix("%").toInt() }

            // ----- Assert -----
            shares?.sum() shouldBe 100
        }

        @Test
        fun `drops a part with no books and keeps shares of the rest summing to 100`() {
            // ----- Arrange -----
            val stats = stats(
                plot = 1,
                character = 3,
            )

            // ----- Act -----
            val segments = howYouReadUiModel(
                stats,
                StatsScope.AllTime,
            ).split?.chart?.segments

            // ----- Assert -----
            listOf(
                segments?.map { it.label },
                segments?.map { it.share },
                segments?.map { it.tone },
            ) shouldBe listOf(
                listOf("Plot", "Character"),
                listOf("25%", "75%"),
                listOf(ChartTone.Lead, ChartTone.Third),
            )
        }

        @Test
        fun `says book in the singular for a single counted book`() {
            // ----- Arrange -----
            val stats = stats(plot = 1)

            // ----- Act -----
            val split = howYouReadUiModel(
                stats,
                StatsScope.AllTime,
            ).split

            // ----- Assert -----
            split?.caption shouldContain "100% of the 1 book with"
        }

        @Test
        fun `adds the scope phrase to the caption except for all time`() {
            // ----- Arrange -----
            val stats = stats(plot = 2)

            // ----- Act -----
            val scoped = howYouReadUiModel(
                stats,
                StatsScope.CalendarYear(2025),
            ).split?.caption

            val allTime = howYouReadUiModel(
                stats,
                StatsScope.AllTime,
            ).split?.caption

            // ----- Assert -----
            scoped shouldStartWith "Your reading leaned plot-driven in 2025: "
            allTime shouldStartWith "Your reading leaned plot-driven: "
        }

        @Test
        fun `has no split block when no book answered plot or character`() {
            // ----- Arrange -----
            val stats = stats(
                paceCounts = mapOf(Pace.FAST to 1),
                booksWithPace = 1,
            )

            // ----- Act -----
            val result = howYouReadUiModel(
                stats,
                StatsScope.AllTime,
            )

            // ----- Assert -----
            listOf(result.split, result.emptyLine) shouldBe listOf(null, null)
        }
    }

    @Nested
    inner class PaceBlock {
        @Test
        fun `lists paces in fixed order and drops those with no books`() {
            // ----- Arrange -----
            val stats = stats(
                paceCounts = mapOf(
                    Pace.VARIABLE to 1,
                    Pace.FAST to 1,
                    Pace.MODERATE to 0,
                    Pace.SLOW to 2,
                ),
                booksWithPace = 4,
            )

            // ----- Act -----
            val segments = howYouReadUiModel(
                stats,
                StatsScope.AllTime,
            ).pace?.chart?.segments

            // ----- Assert -----
            listOf(segments?.map { it.label }, segments?.map { it.share }) shouldBe listOf(
                listOf("Slow", "Fast", "Variable"),
                listOf("50%", "25%", "25%"),
            )
        }

        @Test
        fun `words the caption from the books with a pace`() {
            // ----- Arrange -----
            val many = stats(
                paceCounts = mapOf(Pace.SLOW to 3),
                booksWithPace = 3,
            )

            val one = stats(
                paceCounts = mapOf(Pace.SLOW to 1),
                booksWithPace = 1,
            )

            // ----- Act -----
            val manyCaption = howYouReadUiModel(
                many,
                StatsScope.AllTime,
            ).pace?.caption

            val oneCaption = howYouReadUiModel(
                one,
                StatsScope.AllTime,
            ).pace?.caption

            // ----- Assert -----
            listOf(manyCaption, oneCaption) shouldBe
                listOf("Of 3 books with a pace.", "Of 1 book with a pace.")
        }

        @Test
        fun `has no pace block when every count is zero`() {
            // ----- Arrange -----
            val stats = stats(
                plot = 1,
                paceCounts = mapOf(Pace.SLOW to 0),
            )

            // ----- Act -----
            val pace = howYouReadUiModel(
                stats,
                StatsScope.AllTime,
            ).pace

            // ----- Assert -----
            pace shouldBe null
        }
    }

    @Nested
    inner class WritingBlock {
        @Test
        fun `sorts chips by count, drops zeros and highlights the top chip of each row`() {
            // ----- Arrange -----
            val stats = stats(
                styles = mapOf(
                    WritingStyle.SIMPLE to 1,
                    WritingStyle.MODERATE to 0,
                    WritingStyle.DEMANDING to 3,
                ),
                flags = mapOf(
                    WritingFlag.VIVID to 1,
                    WritingFlag.POETIC to 2,
                ),
                booksWithStyle = 4,
                booksWithFlag = 3,
            )

            // ----- Act -----
            val writing = howYouReadUiModel(
                stats,
                StatsScope.AllTime,
            ).writing

            // ----- Assert -----
            listOf(
                writing?.styles?.chips?.map { it.label },
                writing?.styles?.chips?.map { it.selected },
                writing?.styles?.chips?.map { (it.trailing as ChipTrailing.Count).text },
                writing?.flags?.chips?.map { it.label },
                writing?.flags?.chips?.map { it.selected },
            ) shouldBe listOf(
                listOf("Demanding", "Simple"),
                listOf(true, false),
                listOf("3", "1"),
                listOf("Poetic", "Vivid"),
                listOf(true, false),
            )
        }

        @Test
        fun `words the caption for styles only, flags only and both`() {
            // ----- Arrange -----
            val stylesOnly = stats(
                styles = mapOf(WritingStyle.SIMPLE to 2),
                booksWithStyle = 2,
                booksWithFlag = 9,
            )

            val flagsOnly = stats(
                flags = mapOf(WritingFlag.VIVID to 3),
                booksWithStyle = 9,
                booksWithFlag = 3,
            )

            val both = stats(
                styles = mapOf(WritingStyle.SIMPLE to 4),
                flags = mapOf(WritingFlag.VIVID to 1),
                booksWithStyle = 4,
                booksWithFlag = 1,
            )

            // ----- Act -----
            val captions = listOf(stylesOnly, flagsOnly, both).map {
                howYouReadUiModel(
                    it,
                    StatsScope.AllTime,
                ).writing?.caption
            }

            // ----- Assert -----
            captions shouldBe listOf(
                "2 books with a style.",
                "3 books with a flag.",
                "4 books with a style, 1 with a flag.",
            )
        }

        @Test
        fun `says book in the singular on the caption and the chip description`() {
            // ----- Arrange -----
            val stats = stats(
                styles = mapOf(WritingStyle.SIMPLE to 1),
                booksWithStyle = 1,
            )

            // ----- Act -----
            val writing = howYouReadUiModel(
                stats,
                StatsScope.AllTime,
            ).writing

            // ----- Assert -----
            listOf(writing?.caption, writing?.styles?.chips?.single()?.contentDescription) shouldBe
                listOf("1 book with a style.", "Simple, 1 book")
        }

        @Test
        fun `has no writing block when every count is zero`() {
            // ----- Arrange -----
            val stats = stats(
                plot = 1,
                styles = mapOf(WritingStyle.SIMPLE to 0),
                flags = mapOf(WritingFlag.VIVID to 0),
            )

            // ----- Act -----
            val writing = howYouReadUiModel(
                stats,
                StatsScope.AllTime,
            ).writing

            // ----- Assert -----
            writing shouldBe null
        }
    }

    @Nested
    inner class MoodsBlock {
        @Test
        fun `scales the strength to five with one decimal and fills to a hundredth`() {
            // ----- Arrange -----
            val stats = stats(
                booksWithMoods = 2,
                topMoods = listOf(
                    mood(
                        "Cozy",
                        80.0,
                    ),
                    mood(
                        "Calm",
                        50.0,
                    ),
                ),
            )

            // ----- Act -----
            val rows = howYouReadUiModel(
                stats,
                StatsScope.AllTime,
            ).moods?.bars?.rows

            // ----- Assert -----
            listOf(
                rows?.map { it.label },
                rows?.map {
                    it.valueText.replace(
                        ',',
                        '.',
                    )
                },
                rows?.map { it.fill },
            ) shouldBe listOf(
                listOf("Cozy", "Calm"),
                listOf("4.0", "2.5"),
                listOf(0.8f, 0.5f),
            )
        }

        @Test
        fun `shows a dash with an empty bar when the strength was not given`() {
            // ----- Arrange -----
            val stats = stats(
                booksWithMoods = 1,
                topMoods = listOf(
                    mood(
                        "Tense",
                        null,
                    ),
                ),
            )

            // ----- Act -----
            val row = howYouReadUiModel(
                stats,
                StatsScope.AllTime,
            ).moods?.bars?.rows?.single()

            // ----- Assert -----
            listOf(row?.valueText, row?.fill, row?.valueContentDescription) shouldBe
                listOf("–", 0f, "no strength given")
        }

        @Test
        fun `words the caption in the singular and with the scope phrase`() {
            // ----- Arrange -----
            val one = stats(
                booksWithMoods = 1,
                topMoods = listOf(
                    mood(
                        "Cozy",
                        40.0,
                    ),
                ),
            )

            val three = stats(
                booksWithMoods = 3,
                topMoods = listOf(
                    mood(
                        "Cozy",
                        40.0,
                    ),
                ),
            )

            // ----- Act -----
            val singular = howYouReadUiModel(
                one,
                StatsScope.AllTime,
            ).moods?.caption

            val scoped = howYouReadUiModel(
                three,
                StatsScope.CalendarYear(2025),
            ).moods?.caption

            // ----- Assert -----
            singular shouldStartWith "From the 1 book you said how it felt."
            scoped shouldStartWith "From the 3 books you said how they felt in 2025."
        }

        @Test
        fun `has no moods block without books with moods or without top moods`() {
            // ----- Arrange -----
            val noBooks = stats(
                plot = 1,
                topMoods = listOf(
                    mood(
                        "Cozy",
                        40.0,
                    ),
                ),
            )

            val noMoods = stats(
                plot = 1,
                booksWithMoods = 2,
            )

            // ----- Act -----
            val results = listOf(noBooks, noMoods).map {
                howYouReadUiModel(
                    it,
                    StatsScope.AllTime,
                ).moods
            }

            // ----- Assert -----
            results shouldBe listOf(null, null)
        }
    }

    @Nested
    inner class EmptyLine {
        @Test
        fun `words the empty line with the scope phrase and without it for all time`() {
            // ----- Arrange -----
            val stats = stats()

            // ----- Act -----
            val scoped = howYouReadUiModel(
                stats,
                StatsScope.CalendarYear(2025),
            )

            val allTime = howYouReadUiModel(
                stats,
                StatsScope.AllTime,
            )

            // ----- Assert -----
            listOf(scoped.emptyLine, allTime.emptyLine) shouldBe listOf(
                "No book read in 2025 has a How it read or How it felt answer yet.",
                "No book has a How it read or How it felt answer yet.",
            )

            listOf(scoped.split, scoped.pace, scoped.writing, scoped.moods) shouldBe
                listOf(null, null, null, null)
        }

        @Test
        fun `has no empty line once any block has an answer`() {
            // ----- Arrange -----
            val stats = stats(
                styles = mapOf(WritingStyle.SIMPLE to 1),
                booksWithStyle = 1,
            )

            // ----- Act -----
            val result = howYouReadUiModel(
                stats,
                StatsScope.AllTime,
            )

            // ----- Assert -----
            result.emptyLine shouldBe null
        }
    }
}
