package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.AcquisitionSplit
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.AuthorshipGender
import nl.rhaydus.softcover.core.profile.domain.model.CharacterBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.CharacterRepresentation
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.DiversityStats
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
import nl.rhaydus.softcover.core.profile.domain.model.PublicationDecades
import nl.rhaydus.softcover.core.profile.domain.model.PublicationStats
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DiversityChapterMapperTest {
    private fun slice(
        gender: AuthorshipGender,
        fraction: Double,
        count: Int = 1,
    ): GenderSlice = GenderSlice(
        gender = gender,
        count = count,
        fraction = fraction,
    )

    @Nested
    inner class DiversityChapterUiModelTests {
        @Test
        fun `words the header description differently while books without detail are hidden`() {
            // ----- Arrange -----
            val stats = DiversityStats(booksRead = 5)

            // ----- Act -----
            val shown = diversityChapterUiModel(
                stats = stats,
                hideBooksWithoutDetail = false,
            ).header.description?.joinToString(separator = "") { it.text }

            val hidden = diversityChapterUiModel(
                stats = stats,
                hideBooksWithoutDetail = true,
            ).header.description?.joinToString(separator = "") { it.text }

            // ----- Assert -----
            (shown != hidden) shouldBe true
        }

        @Test
        fun `returns the loading chapter that cannot be hidden when stats is null`() {
            // ----- Arrange -----
            val stats: DiversityStats? = null

            // ----- Act -----
            val result = diversityChapterUiModel(
                stats = stats,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.genderBreakdown.chart.isLoading shouldBe true
            result.characterLgbtq.chart.isLoading shouldBe true
            result.hasContent shouldBe false
            result.canHideBooksWithoutDetail shouldBe false
        }

        @Test
        fun `can hide when only a character bar has a not answered bucket`() {
            // ----- Arrange -----
            val stats = DiversityStats(
                booksRead = 5,
                characterRepresentation = CharacterRepresentation(
                    lgbtq = CharacterBreakdown(
                        yesCount = 1,
                        noCount = 2,
                        notAnsweredCount = 2,
                    ),
                ),
            )

            // ----- Act -----
            val result = diversityChapterUiModel(
                stats = stats,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.canHideBooksWithoutDetail shouldBe true
        }

        @Test
        fun `can hide when only the author gender has unknown books`() {
            // ----- Arrange -----
            val stats = DiversityStats(
                booksRead = 5,
                authorDemographics = AuthorDemographics(
                    genderSlices = listOf(
                        slice(
                            gender = AuthorshipGender.Female,
                            fraction = 0.6,
                            count = 3,
                        ),
                        slice(
                            gender = AuthorshipGender.Unknown,
                            fraction = 0.4,
                            count = 2,
                        ),
                    ),
                    knownGenderCount = 3,
                    unknownGenderCount = 2,
                ),
            )

            // ----- Act -----
            val result = diversityChapterUiModel(
                stats = stats,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.canHideBooksWithoutDetail shouldBe true
        }

        @Test
        fun `cannot hide when no bar has a bucket of books without its detail`() {
            // ----- Arrange -----
            val stats = DiversityStats(
                booksRead = 4,
                authorDemographics = AuthorDemographics(
                    bipocBreakdown = DemographicBreakdown(
                        yesCount = 1,
                        noCount = 3,
                    ),
                ),
                characterRepresentation = CharacterRepresentation(
                    bipoc = CharacterBreakdown(
                        yesCount = 1,
                        noCount = 3,
                    ),
                ),
            )

            // ----- Act -----
            val result = diversityChapterUiModel(
                stats = stats,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.canHideBooksWithoutDetail shouldBe false
        }

        @Test
        fun `stays hideable while the switch is on because it reads the unfiltered stats`() {
            // ----- Arrange -----
            val stats = DiversityStats(
                booksRead = 5,
                characterRepresentation = CharacterRepresentation(
                    bipoc = CharacterBreakdown(
                        yesCount = 1,
                        notAnsweredCount = 4,
                    ),
                ),
            )

            // ----- Act -----
            val result = diversityChapterUiModel(
                stats = stats,
                hideBooksWithoutDetail = true,
            )

            // ----- Assert -----
            result.canHideBooksWithoutDetail shouldBe true
            result.characterBipoc.chart.segments.map { it.label } shouldBe listOf("Yes")
        }

        @Test
        fun `can hide when only a publication section has an unknown bucket`() {
            // ----- Arrange -----
            val stats = DiversityStats(
                booksRead = 5,
                publication = PublicationStats(
                    decades = PublicationDecades(
                        booksByDecade = mapOf(1990 to 3),
                        unknownYear = 2,
                    ),
                ),
            )

            // ----- Act -----
            val result = diversityChapterUiModel(
                stats = stats,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.canHideBooksWithoutDetail shouldBe true
        }

        @Test
        fun `has content when only publication data exists`() {
            // ----- Arrange -----
            val stats = DiversityStats(
                booksRead = 2,
                publication = PublicationStats(
                    acquisition = AcquisitionSplit(byKind = mapOf(AcquisitionKind.GIFT to 2)),
                ),
            )

            // ----- Act -----
            val result = diversityChapterUiModel(
                stats = stats,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.authorBipoc.hasData shouldBe false
            result.acquisition.hasData shouldBe true
            result.hasContent shouldBe true
        }

        @Test
        fun `has content when only a character bar has data`() {
            // ----- Arrange -----
            val stats = DiversityStats(
                booksRead = 2,
                characterRepresentation = CharacterRepresentation(
                    lgbtq = CharacterBreakdown(yesCount = 2),
                ),
            )

            // ----- Act -----
            val result = diversityChapterUiModel(
                stats = stats,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.characterLgbtq.hasData shouldBe true
            result.authorBipoc.hasData shouldBe false
            result.hasContent shouldBe true
        }

        @Test
        fun `has no content when every bar is empty`() {
            // ----- Arrange -----
            val stats = DiversityStats(booksRead = 3)

            // ----- Act -----
            val result = diversityChapterUiModel(
                stats = stats,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.hasContent shouldBe false
        }

        @Test
        fun `has no content while hiding when the only data was books without detail`() {
            // ----- Arrange -----
            val stats = DiversityStats(
                booksRead = 3,
                characterRepresentation = CharacterRepresentation(
                    bipoc = CharacterBreakdown(notAnsweredCount = 3),
                ),
            )

            // ----- Act -----
            val result = diversityChapterUiModel(
                stats = stats,
                hideBooksWithoutDetail = true,
            )

            // ----- Assert -----
            result.hasContent shouldBe false
            result.canHideBooksWithoutDetail shouldBe true
        }

        @Test
        fun `renormalises the author bars over the books with the detail while hiding`() {
            // ----- Arrange -----
            val stats = DiversityStats(
                booksRead = 10,
                authorDemographics = AuthorDemographics(
                    bipocBreakdown = DemographicBreakdown(
                        yesCount = 1,
                        noCount = 1,
                        unknownCount = 8,
                    ),
                ),
            )

            // ----- Act -----
            val result = diversityChapterUiModel(
                stats = stats,
                hideBooksWithoutDetail = true,
            )

            // ----- Assert -----
            result.authorBipoc.chart.segments.map { it.share } shouldBe listOf("50%", "50%")
        }
    }

    @Nested
    inner class GenderChartUiModelTests {
        @Test
        fun `returns the loading chart with three masked segments when slices is null`() {
            // ----- Arrange -----
            val slices: List<GenderSlice>? = null

            // ----- Act -----
            val result = genderChartUiModel(slices)

            // ----- Assert -----
            result.isLoading shouldBe true
            result.segments shouldHaveSize 3
            result.segments.map { it.share }.toSet() shouldBe setOf("")
        }

        @Test
        fun `returns no segments and is not loading for an empty list`() {
            // ----- Arrange -----
            val slices = emptyList<GenderSlice>()

            // ----- Act -----
            val result = genderChartUiModel(slices)

            // ----- Assert -----
            result.isLoading shouldBe false
            result.segments shouldHaveSize 0
        }

        @Test
        fun `labels each gender and assigns Lead, Second, Third in rank order`() {
            // ----- Arrange -----
            val slices = listOf(
                slice(
                    gender = AuthorshipGender.Female,
                    fraction = 0.5,
                ),
                slice(
                    gender = AuthorshipGender.Male,
                    fraction = 0.3,
                ),
                slice(
                    gender = AuthorshipGender.Other,
                    fraction = 0.2,
                ),
            )

            // ----- Act -----
            val result = genderChartUiModel(slices)

            // ----- Assert -----
            result.segments.map { it.label } shouldBe listOf("Women", "Men", "Other")
            result.segments.map { it.tone } shouldBe listOf(ChartTone.Lead, ChartTone.Second, ChartTone.Third)
            result.segments.map { it.share } shouldBe listOf("50%", "30%", "20%")
            result.segments.map { it.fraction } shouldBe listOf(0.5f, 0.3f, 0.2f)
        }

        @Test
        fun `gives Mixed the tone after the known genders even when it is the largest slice`() {
            // ----- Arrange -----
            val slices = listOf(
                slice(
                    gender = AuthorshipGender.Female,
                    fraction = 0.2,
                ),
                slice(
                    gender = AuthorshipGender.Male,
                    fraction = 0.1,
                ),
                slice(
                    gender = AuthorshipGender.Mixed,
                    fraction = 0.5,
                ),
                slice(
                    gender = AuthorshipGender.Unknown,
                    fraction = 0.2,
                ),
            )

            // ----- Act -----
            val result = genderChartUiModel(slices)

            // ----- Assert -----
            result.segments.map { it.label } shouldBe listOf("Women", "Men", "Mixed", "Unknown")

            result.segments.map { it.tone } shouldBe
                listOf(ChartTone.Lead, ChartTone.Second, ChartTone.Third, ChartTone.Unknown)
        }

        @Test
        fun `maps Unknown to the Unknown tone without consuming a known tone`() {
            // ----- Arrange -----
            val slices = listOf(
                slice(
                    gender = AuthorshipGender.Male,
                    fraction = 0.4,
                ),
                slice(
                    gender = AuthorshipGender.Unknown,
                    fraction = 0.35,
                ),
                slice(
                    gender = AuthorshipGender.Female,
                    fraction = 0.25,
                ),
            )

            // ----- Act -----
            val result = genderChartUiModel(slices)

            // ----- Assert -----
            result.segments.map { it.tone } shouldBe listOf(ChartTone.Lead, ChartTone.Unknown, ChartTone.Second)
        }
    }

    @Nested
    inner class AuthorChartUiModelTests {
        @Test
        fun `returns the loading chart with three masked segments when breakdown is null`() {
            // ----- Arrange -----
            val breakdown: DemographicBreakdown? = null

            // ----- Act -----
            val result = authorChartUiModel(breakdown)

            // ----- Assert -----
            result.isLoading shouldBe true
            result.segments shouldHaveSize 3
        }

        @Test
        fun `maps Yes to Lead and No to Fourth and omits Unknown when no book lacks the detail`() {
            // ----- Arrange -----
            val breakdown = DemographicBreakdown(
                yesCount = 1,
                noCount = 3,
                unknownCount = 0,
            )

            // ----- Act -----
            val result = authorChartUiModel(breakdown)

            // ----- Assert -----
            result.isLoading shouldBe false
            result.segments.map { it.label } shouldBe listOf("Yes", "No")
            result.segments.map { it.tone } shouldBe listOf(ChartTone.Lead, ChartTone.Fourth)
            result.segments.map { it.share } shouldBe listOf("25%", "75%")
            result.segments.map { it.fraction } shouldBe listOf(0.25f, 0.75f)
        }

        @Test
        fun `adds an Unknown segment when unknownCount is positive`() {
            // ----- Arrange -----
            val breakdown = DemographicBreakdown(
                yesCount = 2,
                noCount = 2,
                unknownCount = 4,
            )

            // ----- Act -----
            val result = authorChartUiModel(breakdown)

            // ----- Assert -----
            result.segments shouldHaveSize 3
            result.segments.last().label shouldBe "Unknown"
            result.segments.last().tone shouldBe ChartTone.Unknown
            result.segments.map { it.share } shouldBe listOf("25%", "25%", "50%")
        }

        @Test
        fun `keeps Yes and No at 0 percent when there is no data at all`() {
            // ----- Arrange -----
            val breakdown = DemographicBreakdown()

            // ----- Act -----
            val result = authorChartUiModel(breakdown)

            // ----- Assert -----
            result.segments.map { it.share } shouldBe listOf("0%", "0%")
            result.segments.map { it.fraction } shouldBe listOf(0f, 0f)
        }
    }

    @Nested
    inner class CharacterChartUiModelTests {
        @Test
        fun `returns the loading chart with three masked segments when breakdown is null`() {
            // ----- Arrange -----
            val breakdown: CharacterBreakdown? = null

            // ----- Act -----
            val result = characterChartUiModel(breakdown)

            // ----- Assert -----
            result.isLoading shouldBe true
            result.segments shouldHaveSize 3
        }

        @Test
        fun `draws Yes, Complicated, No and Not answered with their shares of every book`() {
            // ----- Arrange -----
            val breakdown = CharacterBreakdown(
                yesCount = 1,
                complicatedCount = 1,
                noCount = 2,
                notAnsweredCount = 4,
            )

            // ----- Act -----
            val result = characterChartUiModel(breakdown)

            // ----- Assert -----
            result.segments.map { it.label } shouldBe listOf("Yes", "Complicated", "No", "Not answered")
            result.segments.map { it.share } shouldBe listOf("13%", "13%", "25%", "50%")
            result.segments.last().tone shouldBe ChartTone.Unknown
        }

        @Test
        fun `draws nothing for a bucket with no books`() {
            // ----- Arrange -----
            val breakdown = CharacterBreakdown(
                yesCount = 3,
                noCount = 1,
            )

            // ----- Act -----
            val result = characterChartUiModel(breakdown)

            // ----- Assert -----
            result.segments.map { it.label } shouldBe listOf("Yes", "No")
            result.segments.map { it.share } shouldBe listOf("75%", "25%")
        }

        @Test
        fun `has no segments for an empty breakdown`() {
            // ----- Arrange -----
            val breakdown = CharacterBreakdown()

            // ----- Act -----
            val result = characterChartUiModel(breakdown)

            // ----- Assert -----
            result.isLoading shouldBe false
            result.segments shouldHaveSize 0
        }
    }

    @Nested
    inner class GenderBreakdownUiModelTests {
        @Test
        fun `has no data and the loading chart when demographics is null`() {
            // ----- Arrange -----
            val demographics: AuthorDemographics? = null

            // ----- Act -----
            val result = genderBreakdownUiModel(
                demographics = demographics,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.hasData shouldBe false
            result.chart.isLoading shouldBe true
        }

        @Test
        fun `has no data when there are no gender slices`() {
            // ----- Arrange -----
            val demographics = AuthorDemographics()

            // ----- Act -----
            val result = genderBreakdownUiModel(
                demographics = demographics,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.hasData shouldBe false
            result.chart.isLoading shouldBe false
        }

        @Test
        fun `counts known and unknown books in the caption`() {
            // ----- Arrange -----
            val demographics = AuthorDemographics(
                genderSlices = listOf(
                    slice(
                        gender = AuthorshipGender.Female,
                        fraction = 0.6,
                    ),
                    slice(
                        gender = AuthorshipGender.Unknown,
                        fraction = 0.4,
                    ),
                ),
                knownGenderCount = 6,
                unknownGenderCount = 4,
            )

            // ----- Act -----
            val result = genderBreakdownUiModel(
                demographics = demographics,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.hasData shouldBe true
            result.caption shouldContain "10 books"
            result.caption shouldNotContain "known"
            result.chart shouldBe genderChartUiModel(demographics.genderSlices)
        }

        @Test
        fun `says the books have the authors gender while books without detail are hidden`() {
            // ----- Arrange -----
            val demographics = AuthorDemographics(
                genderSlices = listOf(
                    slice(
                        gender = AuthorshipGender.Female,
                        fraction = 1.0,
                    ),
                ),
                knownGenderCount = 6,
                unknownGenderCount = 0,
            )

            // ----- Act -----
            val result = genderBreakdownUiModel(
                demographics = demographics,
                hideBooksWithoutDetail = true,
            )

            // ----- Assert -----
            result.caption shouldContain "6 books with"
        }

        @Test
        fun `uses the same fill-in caption for no books whether or not books are hidden`() {
            // ----- Arrange -----
            val demographics = AuthorDemographics()

            val fillInCaption = genderBreakdownUiModel(
                demographics = demographics,
                hideBooksWithoutDetail = false,
            ).caption

            // ----- Act -----
            val result = genderBreakdownUiModel(
                demographics = demographics,
                hideBooksWithoutDetail = true,
            )

            // ----- Assert -----
            result.caption shouldBe fillInCaption
        }

        @Test
        fun `names the books with no gender data in the caption when some are unknown and shown`() {
            // ----- Arrange -----
            val demographics = AuthorDemographics(
                genderSlices = listOf(
                    slice(
                        gender = AuthorshipGender.Female,
                        fraction = 1.0,
                    ),
                ),
                knownGenderCount = 6,
                unknownGenderCount = 4,
            )

            // ----- Act -----
            val result = genderBreakdownUiModel(
                demographics = demographics,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.caption shouldBe "Across 10 books, 4 with no gender data."
        }

        @Test
        fun `gives only the book count when none are unknown and shown`() {
            // ----- Arrange -----
            val demographics = AuthorDemographics(
                genderSlices = listOf(
                    slice(
                        gender = AuthorshipGender.Female,
                        fraction = 1.0,
                    ),
                ),
                knownGenderCount = 6,
                unknownGenderCount = 0,
            )

            // ----- Act -----
            val result = genderBreakdownUiModel(
                demographics = demographics,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.caption shouldBe "Across 6 books."
        }

        @Test
        fun `drops the no gender data count while hiding even if unknown books exist`() {
            // ----- Arrange -----
            val demographics = AuthorDemographics(
                genderSlices = listOf(
                    slice(
                        gender = AuthorshipGender.Female,
                        fraction = 1.0,
                    ),
                ),
                knownGenderCount = 6,
                unknownGenderCount = 4,
            )

            // ----- Act -----
            val result = genderBreakdownUiModel(
                demographics = demographics,
                hideBooksWithoutDetail = true,
            )

            // ----- Assert -----
            result.caption shouldBe "Across 10 books with the authors' gender."
        }
    }

    @Nested
    inner class AuthorBreakdownUiModelTests {
        @Test
        fun `has no data and the loading chart when the breakdown is null`() {
            // ----- Arrange -----
            val breakdown: DemographicBreakdown? = null

            // ----- Act -----
            val result = authorBreakdownUiModel(
                label = "Authors of colour",
                breakdown = breakdown,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.hasData shouldBe false
            result.chart.isLoading shouldBe true
        }

        @Test
        fun `has no data and a different caption for an empty breakdown while books are hidden`() {
            // ----- Arrange -----
            val breakdown = DemographicBreakdown()

            val fillInCaption = authorBreakdownUiModel(
                label = "Authors of colour",
                breakdown = breakdown,
                hideBooksWithoutDetail = false,
            ).caption

            // ----- Act -----
            val result = authorBreakdownUiModel(
                label = "Authors of colour",
                breakdown = breakdown,
                hideBooksWithoutDetail = true,
            )

            // ----- Assert -----
            result.hasData shouldBe false
            result.chart.isLoading shouldBe false
            result.caption shouldBe "None of your books carry this detail yet."
            fillInCaption shouldNotContain "None"
        }

        @Test
        fun `lists yes, no and unknown counts over the total in books`() {
            // ----- Arrange -----
            val breakdown = DemographicBreakdown(
                yesCount = 2,
                noCount = 3,
                unknownCount = 5,
            )

            // ----- Act -----
            val result = authorBreakdownUiModel(
                label = "LGBTQ+ authors",
                breakdown = breakdown,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.label shouldBe "LGBTQ+ authors"
            result.hasData shouldBe true
            result.caption shouldBe "Yes 2 · No 3 · Unknown 5 · of 10 books"
            result.chart shouldBe authorChartUiModel(breakdown)
        }

        @Test
        fun `omits the unknown count when no book lacks the detail`() {
            // ----- Arrange -----
            val breakdown = DemographicBreakdown(
                yesCount = 2,
                noCount = 3,
            )

            // ----- Act -----
            val result = authorBreakdownUiModel(
                label = "LGBTQ+ authors",
                breakdown = breakdown,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.caption shouldBe "Yes 2 · No 3 · of 5 books"
        }

        @Test
        fun `names the total as books with this detail while books are hidden`() {
            // ----- Arrange -----
            val breakdown = DemographicBreakdown(
                yesCount = 2,
                noCount = 3,
            )

            // ----- Act -----
            val result = authorBreakdownUiModel(
                label = "LGBTQ+ authors",
                breakdown = breakdown,
                hideBooksWithoutDetail = true,
            )

            // ----- Assert -----
            result.caption shouldBe "Yes 2 · No 3 · of 5 books with this detail"
        }
    }

    @Nested
    inner class CharacterBreakdownUiModelTests {
        @Test
        fun `has no data and the loading chart when the breakdown is null`() {
            // ----- Arrange -----
            val breakdown: CharacterBreakdown? = null

            // ----- Act -----
            val result = characterBreakdownUiModel(
                label = "BIPOC characters",
                breakdown = breakdown,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.hasData shouldBe false
            result.chart.isLoading shouldBe true
        }

        @Test
        fun `has no data and words the empty caption by whether books are hidden`() {
            // ----- Arrange -----
            val breakdown = CharacterBreakdown()

            // ----- Act -----
            val shown = characterBreakdownUiModel(
                label = "BIPOC characters",
                breakdown = breakdown,
                hideBooksWithoutDetail = false,
            )

            val hidden = characterBreakdownUiModel(
                label = "BIPOC characters",
                breakdown = breakdown,
                hideBooksWithoutDetail = true,
            )

            // ----- Assert -----
            shown.hasData shouldBe false
            hidden.hasData shouldBe false
            (shown.caption == hidden.caption) shouldBe false
        }

        @Test
        fun `lists every bucket including Not answered over the total in books`() {
            // ----- Arrange -----
            val breakdown = CharacterBreakdown(
                yesCount = 1,
                complicatedCount = 2,
                noCount = 3,
                notAnsweredCount = 4,
            )

            // ----- Act -----
            val result = characterBreakdownUiModel(
                label = "BIPOC characters",
                breakdown = breakdown,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.hasData shouldBe true
            result.caption shouldBe "Yes 1 · Complicated 2 · No 3 · Not answered 4 · of 10 books"
            result.chart shouldBe characterChartUiModel(breakdown)
        }

        @Test
        fun `drops Not answered from the caption and names the total as books with this detail while hidden`() {
            // ----- Arrange -----
            val breakdown = CharacterBreakdown(
                yesCount = 1,
                complicatedCount = 2,
                noCount = 3,
            )

            // ----- Act -----
            val result = characterBreakdownUiModel(
                label = "BIPOC characters",
                breakdown = breakdown,
                hideBooksWithoutDetail = true,
            )

            // ----- Assert -----
            result.caption shouldBe "Yes 1 · Complicated 2 · No 3 · of 6 books with this detail"
        }
    }

    @Nested
    inner class DiversityScopeCaptionTests {
        @Test
        fun `names the scope when books are shown`() {
            // ----- Arrange -----
            val scope = StatsScope.CalendarYear(2025)

            // ----- Act -----
            val result = diversityScopeCaption(
                hideBooksWithoutDetail = false,
                scope = scope,
            )

            // ----- Assert -----
            result shouldBe "Shares of every book you read in 2025, with or without the detail."
        }

        @Test
        fun `names no scope for all time`() {
            // ----- Arrange -----
            val scope = StatsScope.AllTime

            // ----- Act -----
            val result = diversityScopeCaption(
                hideBooksWithoutDetail = false,
                scope = scope,
            )

            // ----- Assert -----
            result shouldBe "Shares of every book you read, with or without the detail."
        }

        @Test
        fun `describes the per-bar denominator instead of the scope while books are hidden`() {
            // ----- Arrange -----
            val scope = StatsScope.CalendarYear(2025)

            // ----- Act -----
            val result = diversityScopeCaption(
                hideBooksWithoutDetail = true,
                scope = scope,
            )

            // ----- Assert -----
            result shouldBe "Each bar counts only the books with that detail."
        }
    }

    @Nested
    inner class DiversityEmptyCaptionTests {
        @Test
        fun `words the empty caption as no books read in a narrower scope with no books`() {
            // ----- Arrange -----
            val scope = StatsScope.CalendarYear(2025)

            // ----- Act -----
            val result = diversityEmptyCaption(
                hideBooksWithoutDetail = true,
                booksRead = 0,
                scope = scope,
            )

            // ----- Assert -----
            result shouldBe "No books read in 2025."
        }

        @Test
        fun `falls back to the fill-in line when books exist and the switch is off`() {
            // ----- Arrange -----
            val scope = StatsScope.CalendarYear(2025)

            // ----- Act -----
            val result = diversityEmptyCaption(
                hideBooksWithoutDetail = false,
                booksRead = 3,
                scope = scope,
            )

            // ----- Assert -----
            result shouldContain "fills in"
        }

        @Test
        fun `points at the switch when books exist and the switch is on`() {
            // ----- Arrange -----
            val scope = StatsScope.AllTime

            // ----- Act -----
            val result = diversityEmptyCaption(
                hideBooksWithoutDetail = true,
                booksRead = 3,
                scope = scope,
            )

            // ----- Assert -----
            result shouldContain "Hide books without this detail"
        }

        @Test
        fun `says no books read yet for all time with no books`() {
            // ----- Arrange -----
            val scope = StatsScope.AllTime

            // ----- Act -----
            val result = diversityEmptyCaption(
                hideBooksWithoutDetail = false,
                booksRead = 0,
                scope = scope,
            )

            // ----- Assert -----
            result shouldBe "No books read yet."
        }
    }
}
