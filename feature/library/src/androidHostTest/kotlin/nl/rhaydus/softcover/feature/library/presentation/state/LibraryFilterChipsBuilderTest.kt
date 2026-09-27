package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryFilterChipsBuilderTest {
    private val tagFiction = Tag(
        id = 1,
        name = "Fiction",
        category = TagCategory.GENRE,
    )
    private val tagScifi = Tag(
        id = 2,
        name = "Sci-Fi",
        category = TagCategory.GENRE,
    )

    @Nested
    inner class BuildLibraryFilterChips {
        @Test
        fun `empty options produce empty chips per facet and an empty payload map`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions()

            // ----- Act -----
            val chips = buildLibraryFilterChips(options = options)

            // ----- Assert -----
            chips.isEmpty shouldBe true
            chips.ownershipChips.payloadByKey shouldBe emptyMap()
        }

        @Test
        fun `builds one owned and one unowned chip in that order when supportsOwnedFilter is true`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(supportsOwnedFilter = true)

            // ----- Act -----
            val chips = buildLibraryFilterChips(options = options)

            // ----- Assert -----
            chips.ownershipChips.chips.map { it.key } shouldBe listOf("owned:true", "owned:false")
            chips.ownershipChips.chips.map { it.label } shouldBe listOf("Owned", "Unowned")
            chips.ownershipChips["owned:true"] shouldBe LibraryFilterValue.Owned(owned = true)
            chips.ownershipChips["owned:false"] shouldBe LibraryFilterValue.Owned(owned = false)
        }

        @Test
        fun `omits ownership chips when supportsOwnedFilter is false`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(supportsOwnedFilter = false)

            // ----- Act -----
            val chips = buildLibraryFilterChips(options = options)

            // ----- Assert -----
            chips.ownershipChips.chips shouldBe emptyList()
        }

        @Test
        fun `builds one format chip per option in source order`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(formats = listOf("hardcover", "ebook", "audiobook"))

            // ----- Act -----
            val chips = buildLibraryFilterChips(options = options)

            // ----- Assert -----
            chips.formatChips.chips.map { it.key } shouldBe
                listOf("format:hardcover", "format:ebook", "format:audiobook")
            chips.formatChips.chips.map { it.label } shouldBe listOf("hardcover", "ebook", "audiobook")
            chips.formatChips["format:hardcover"] shouldBe LibraryFilterValue.Format(value = "hardcover")
        }

        @Test
        fun `builds one releaseYear chip per option in source order`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(releaseYears = listOf(2021, 2019))

            // ----- Act -----
            val chips = buildLibraryFilterChips(options = options)

            // ----- Assert -----
            chips.releaseYearChips.chips.map { it.key } shouldBe listOf("releaseYear:2021", "releaseYear:2019")
            chips.releaseYearChips.chips.map { it.label } shouldBe listOf("2021", "2019")
            chips.releaseYearChips["releaseYear:2021"] shouldBe LibraryFilterValue.ReleaseYear(year = 2021)
        }

        @Test
        fun `builds one readYear chip per option in source order`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(readYears = listOf(2022, 2020))

            // ----- Act -----
            val chips = buildLibraryFilterChips(options = options)

            // ----- Assert -----
            chips.readYearChips.chips.map { it.key } shouldBe listOf("readYear:2022", "readYear:2020")
            chips.readYearChips.chips.map { it.label } shouldBe listOf("2022", "2020")
            chips.readYearChips["readYear:2022"] shouldBe LibraryFilterValue.ReadYear(year = 2022)
        }

        @Test
        fun `builds one tag chip per option in source order`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(tags = listOf(tagScifi, tagFiction))

            // ----- Act -----
            val chips = buildLibraryFilterChips(options = options)

            // ----- Assert -----
            chips.tagChips.chips.map { it.key } shouldBe listOf("tag:2", "tag:1")
            chips.tagChips.chips.map { it.label } shouldBe listOf("Sci-Fi", "Fiction")
            chips.tagChips["tag:2"] shouldBe LibraryFilterValue.Tag(tag = tagScifi)
        }

        @Test
        fun `builds one rating chip per bucket in source order`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(ratingBuckets = listOf(4.0, 3.5, 3.0))

            // ----- Act -----
            val chips = buildLibraryFilterChips(options = options)

            // ----- Assert -----
            chips.ratingChips.chips.map { it.key } shouldBe listOf("rating:4.0", "rating:3.5", "rating:3.0")
            chips.ratingChips["rating:4.0"] shouldBe LibraryFilterValue.RatingMin(threshold = 4.0)
        }

        @Test
        fun `rating chip label collapses a whole rating to an integer`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(ratingBuckets = listOf(4.0))

            // ----- Act -----
            val chips = buildLibraryFilterChips(options = options)

            // ----- Assert -----
            chips.ratingChips.chips.single().label shouldBe "4★ and up"
        }

        @Test
        fun `rating chip label keeps the fractional digit for a half rating`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(ratingBuckets = listOf(3.5))

            // ----- Act -----
            val chips = buildLibraryFilterChips(options = options)

            // ----- Assert -----
            chips.ratingChips.chips.single().label shouldBe "3.5★ and up"
        }

        @Test
        fun `two facets sharing a display label still get distinct facet-prefixed keys`() {
            // ----- Arrange -----
            // "2020" is both a format's own name and a release year's label — each facet's own
            // ChipSet can only disambiguate them because each key carries its facet prefix.
            val options = LibraryFilterOptions(
                formats = listOf("2020"),
                releaseYears = listOf(2020),
            )

            // ----- Act -----
            val chips = buildLibraryFilterChips(options = options)

            // ----- Assert -----
            val formatKey = chips.formatChips.chips.single().key
            val releaseYearKey = chips.releaseYearChips.chips.single().key
            formatKey shouldBe "format:2020"
            releaseYearKey shouldBe "releaseYear:2020"
            (formatKey == releaseYearKey) shouldBe false
            chips.formatChips[formatKey] shouldBe LibraryFilterValue.Format(value = "2020")
            chips.releaseYearChips[releaseYearKey] shouldBe LibraryFilterValue.ReleaseYear(year = 2020)
        }

        @Test
        fun `selected is always false on every built chip because the sheet owns its own draft selection`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(
                supportsOwnedFilter = true,
                formats = listOf("ebook"),
                releaseYears = listOf(2020),
                readYears = listOf(2020),
                tags = listOf(tagFiction),
                ratingBuckets = listOf(4.0),
            )

            // ----- Act -----
            val chips = buildLibraryFilterChips(options = options)

            // ----- Assert -----
            val allChips = chips.ownershipChips.chips + chips.formatChips.chips + chips.releaseYearChips.chips +
                chips.readYearChips.chips + chips.tagChips.chips + chips.ratingChips.chips
            allChips.map { it.tone } shouldBe allChips.map { ChipTone.Tonal }
            allChips.map { it.selected } shouldBe allChips.map { false }
        }
    }

    @Nested
    inner class RoundedRatingThreshold {
        @Test
        fun `integer threshold collapses to a whole number`() {
            // ----- Act -----
            val result = roundedRatingThreshold(threshold = 4.0)

            // ----- Assert -----
            result shouldBe "4"
        }

        @Test
        fun `fractional threshold keeps the fractional digit`() {
            // ----- Act -----
            val result = roundedRatingThreshold(threshold = 3.5)

            // ----- Assert -----
            result shouldBe "3.5"
        }
    }
}
