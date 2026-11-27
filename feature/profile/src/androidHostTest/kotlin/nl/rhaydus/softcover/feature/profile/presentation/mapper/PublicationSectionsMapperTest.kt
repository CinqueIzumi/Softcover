package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.core.profile.domain.model.AcquisitionSplit
import nl.rhaydus.softcover.core.profile.domain.model.LanguageCount
import nl.rhaydus.softcover.core.profile.domain.model.LanguageSplit
import nl.rhaydus.softcover.core.profile.domain.model.PublicationDecades
import nl.rhaydus.softcover.core.profile.domain.model.PublicationStats
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PublicationSectionsMapperTest {
    private fun languages(vararg counts: Pair<String, Int>): List<LanguageCount> = counts.map { (name, books) ->
        LanguageCount(
            name = name,
            books = books,
        )
    }

    @Nested
    inner class LanguagesSectionTests {
        @Test
        fun `names the top four languages and pools the rest as other languages`() {
            // ----- Arrange -----
            val split = LanguageSplit(
                languages = languages(
                    "English" to 10,
                    "Dutch" to 8,
                    "French" to 6,
                    "German" to 4,
                    "Italian" to 2,
                    "Czech" to 1,
                ),
            )

            // ----- Act -----
            val result = languagesSection(
                split = split,
                hideBooksWithoutDetail = false,
                isFillingLanguages = false,
            )

            // ----- Assert -----
            result.chart.segments.map { it.label } shouldBe
                listOf("English", "Dutch", "French", "German", "Other languages")

            result.chart.segments.last().tone shouldBe ChartTone.Fifth
            result.hasData shouldBe true
        }

        @Test
        fun `puts unknown last while books without detail are shown`() {
            // ----- Arrange -----
            val split = LanguageSplit(
                languages = languages(
                    "English" to 5,
                    "Dutch" to 3,
                ),
                unknown = 2,
            )

            // ----- Act -----
            val result = languagesSection(
                split = split,
                hideBooksWithoutDetail = false,
                isFillingLanguages = false,
            )

            // ----- Assert -----
            result.chart.segments.map { it.label } shouldBe listOf("English", "Dutch", "Unknown")
            result.chart.segments.last().tone shouldBe ChartTone.Unknown
        }

        @Test
        fun `drops unknown and renormalises the shares when books without detail are hidden`() {
            // ----- Arrange -----
            val split = LanguageSplit(
                languages = languages(
                    "English" to 5,
                    "Dutch" to 5,
                ),
                unknown = 10,
            )

            // ----- Act -----
            val result = languagesSection(
                split = split,
                hideBooksWithoutDetail = true,
                isFillingLanguages = false,
            )

            // ----- Assert -----
            result.chart.segments.map { it.label } shouldBe listOf("English", "Dutch")
            result.chart.segments.map { it.share } shouldBe listOf("50%", "50%")
        }

        @Test
        fun `whole percents sum to 100 and a tiny share still shows 1 percent`() {
            // ----- Arrange -----
            val split = LanguageSplit(
                languages = languages(
                    "English" to 1000,
                    "Dutch" to 1000,
                    "French" to 1000,
                    "Czech" to 1,
                ),
            )

            // ----- Act -----
            val result = languagesSection(
                split = split,
                hideBooksWithoutDetail = false,
                isFillingLanguages = false,
            )

            // ----- Assert -----
            val percents = result.chart.segments.map { it.share.removeSuffix("%").toInt() }

            percents.sum() shouldBe 100
            percents.last() shouldBe 1
        }

        @Test
        fun `shows the loading chart while the languages are being filled`() {
            // ----- Arrange -----
            val split = LanguageSplit(languages = languages("English" to 5))

            // ----- Act -----
            val result = languagesSection(
                split = split,
                hideBooksWithoutDetail = false,
                isFillingLanguages = true,
            )

            // ----- Assert -----
            result.chart.isLoading shouldBe true
        }

        @Test
        fun `has no data when no language is known`() {
            // ----- Arrange -----
            val split = LanguageSplit(unknown = 4)

            // ----- Act -----
            val result = languagesSection(
                split = split,
                hideBooksWithoutDetail = false,
                isFillingLanguages = false,
            )

            // ----- Assert -----
            result.hasData shouldBe false
        }
    }

    @Nested
    inner class DecadesSectionTests {
        @Test
        fun `shows only the decades that have books in order`() {
            // ----- Arrange -----
            val decades = PublicationDecades(
                booksByDecade = mapOf(2010 to 2, 1990 to 3, 2000 to 0),
                booksThisCentury = 2,
            )

            // ----- Act -----
            val result = decadesSection(
                decades = decades,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.chart.columns.map { it.label } shouldBe listOf("1990s", "2010s")
            result.chart.columns.map { it.value } shouldBe listOf("3", "2")
        }

        @Test
        fun `pools everything before the newest five into older when more than six decades are known`() {
            // ----- Arrange -----
            val decades = PublicationDecades(
                booksByDecade = mapOf(1900 to 1, 1910 to 2, 1920 to 3, 1930 to 1, 1940 to 1, 1950 to 1, 1960 to 1),
            )

            // ----- Act -----
            val result = decadesSection(
                decades = decades,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.chart.columns.map { it.label } shouldBe listOf("Older", "1920s", "1930s", "1940s", "1950s", "1960s")
            result.chart.columns.first().value shouldBe "3"
        }

        @Test
        fun `keeps all decades when exactly six are known`() {
            // ----- Arrange -----
            val decades = PublicationDecades(
                booksByDecade = (1950..2000 step 10).associateWith { 1 },
            )

            // ----- Act -----
            val result = decadesSection(
                decades = decades,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.chart.columns.map { it.label } shouldBe listOf("1950s", "1960s", "1970s", "1980s", "1990s", "2000s")
        }

        @Test
        fun `tones columns by count rank with ties going to the earliest decade`() {
            // ----- Arrange -----
            val decades = PublicationDecades(
                booksByDecade = mapOf(1970 to 5, 1980 to 9, 1990 to 5, 2000 to 2, 2010 to 1),
            )

            // ----- Act -----
            val result = decadesSection(
                decades = decades,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.chart.columns.map { it.tone } shouldBe
                listOf(ChartTone.Second, ChartTone.Lead, ChartTone.Third, ChartTone.Fourth, ChartTone.Fourth)
        }

        @Test
        fun `adds an unknown column last when years are missing and drops it when hiding`() {
            // ----- Arrange -----
            val decades = PublicationDecades(
                booksByDecade = mapOf(1990 to 3),
                unknownYear = 2,
            )

            // ----- Act -----
            val shown = decadesSection(
                decades = decades,
                hideBooksWithoutDetail = false,
            )

            val hidden = decadesSection(
                decades = decades,
                hideBooksWithoutDetail = true,
            )

            // ----- Assert -----
            shown.chart.columns.map { it.label } shouldBe listOf("1990s", "Unknown")
            shown.chart.columns.last().tone shouldBe ChartTone.Unknown
            hidden.chart.columns.map { it.label } shouldBe listOf("1990s")
        }

        @Test
        fun `says most books came out this century when more than half are from 2000 on`() {
            // ----- Arrange -----
            val decades = PublicationDecades(
                booksByDecade = mapOf(1990 to 4, 2010 to 6),
                oldestYear = 0,
                booksThisCentury = 6,
            )

            // ----- Act -----
            val result = decadesSection(
                decades = decades,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.caption shouldBe "Most of your books came out this century."
        }

        @Test
        fun `names the peak decade and the oldest year when half or fewer are from this century`() {
            // ----- Arrange -----
            val decades = PublicationDecades(
                booksByDecade = mapOf(1990 to 5, 2010 to 5),
                oldestYear = 1871,
                booksThisCentury = 5,
            )

            // ----- Act -----
            val result = decadesSection(
                decades = decades,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.caption shouldBe "More came out in the 1990s than any other decade. The oldest is from 1871."
        }

        @Test
        fun `has no data and an empty caption when no decade is known`() {
            // ----- Arrange -----
            val decades = PublicationDecades(unknownYear = 3)

            // ----- Act -----
            val result = decadesSection(
                decades = decades,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.hasData shouldBe false
            result.caption shouldBe ""
        }
    }

    @Nested
    inner class AcquisitionSectionTests {
        @Test
        fun `ranks presets by count with somewhere else then not noted last`() {
            // ----- Arrange -----
            val split = AcquisitionSplit(
                byKind = mapOf(AcquisitionKind.GIFT to 2, AcquisitionKind.LIBRARY to 5, AcquisitionKind.BORROWED to 0),
                somewhereElse = 3,
                notNoted = 4,
            )

            // ----- Act -----
            val result = acquisitionSection(
                split = split,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.chart.segments.map { it.label } shouldBe
                listOf("Library", "A gift", "Somewhere else", "Not noted")

            result.chart.segments.map { it.tone } shouldBe
                listOf(ChartTone.Lead, ChartTone.Second, ChartTone.Fifth, ChartTone.Unknown)
        }

        @Test
        fun `breaks equal counts by the order of the kinds`() {
            // ----- Arrange -----
            val split = AcquisitionSplit(
                byKind = mapOf(AcquisitionKind.GIFT to 2, AcquisitionKind.LIBRARY to 2),
            )

            // ----- Act -----
            val result = acquisitionSection(
                split = split,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.chart.segments.map { it.label } shouldBe listOf("Library", "A gift")
        }

        @Test
        fun `counts only the noted books in the caption when hiding`() {
            // ----- Arrange -----
            val split = AcquisitionSplit(
                byKind = mapOf(AcquisitionKind.LIBRARY to 4),
                somewhereElse = 2,
                notNoted = 9,
            )

            // ----- Act -----
            val result = acquisitionSection(
                split = split,
                hideBooksWithoutDetail = true,
            )

            // ----- Assert -----
            result.caption shouldBe "For the 6 books where you noted it."
            result.chart.segments.map { it.label } shouldBe listOf("Library", "Somewhere else")
        }

        @Test
        fun `uses the singular for one noted book`() {
            // ----- Arrange -----
            val split = AcquisitionSplit(byKind = mapOf(AcquisitionKind.LIBRARY to 1))

            // ----- Act -----
            val result = acquisitionSection(
                split = split,
                hideBooksWithoutDetail = true,
            )

            // ----- Assert -----
            result.caption shouldBe "For the 1 book where you noted it."
        }

        @Test
        fun `reports the noted share of all books when not noted is shown`() {
            // ----- Arrange -----
            val split = AcquisitionSplit(
                byKind = mapOf(AcquisitionKind.LIBRARY to 3),
                notNoted = 7,
            )

            // ----- Act -----
            val result = acquisitionSection(
                split = split,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.caption shouldBe "Noted for 3 of 10 books."
        }

        @Test
        fun `has no data and an empty caption when nothing is noted`() {
            // ----- Arrange -----
            val split = AcquisitionSplit(notNoted = 5)

            // ----- Act -----
            val result = acquisitionSection(
                split = split,
                hideBooksWithoutDetail = false,
            )

            // ----- Assert -----
            result.hasData shouldBe false
            result.caption shouldBe ""
        }
    }

    @Nested
    inner class HasUnknownBooksTests {
        @Test
        fun `is true when any one of the three sections has an unknown bucket`() {
            // ----- Arrange -----
            val languages = PublicationStats(languages = LanguageSplit(unknown = 1))
            val decades = PublicationStats(decades = PublicationDecades(unknownYear = 1))
            val acquisition = PublicationStats(acquisition = AcquisitionSplit(notNoted = 1))

            // ----- Act & Assert -----
            languages.hasUnknownBooks() shouldBe true
            decades.hasUnknownBooks() shouldBe true
            acquisition.hasUnknownBooks() shouldBe true
            PublicationStats().hasUnknownBooks() shouldBe false
        }
    }
}
