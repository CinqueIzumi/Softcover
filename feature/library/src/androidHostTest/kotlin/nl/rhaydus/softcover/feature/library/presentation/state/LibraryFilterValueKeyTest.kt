package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Tag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryFilterValueKeyTest {
    private val tagFiction = Tag(
        id = 1,
        name = "Fiction",
    )

    @Nested
    inner class ChipKey {
        @Test
        fun `Tag key is facet-prefixed with the tag id`() {
            // ----- Arrange -----
            val value = LibraryFilterValue.Tag(tag = tagFiction)

            // ----- Act -----
            val key = value.chipKey()

            // ----- Assert -----
            key shouldBe "tag:1"
        }

        @Test
        fun `Format key is facet-prefixed with the format value`() {
            // ----- Arrange -----
            val value = LibraryFilterValue.Format(value = "ebook")

            // ----- Act -----
            val key = value.chipKey()

            // ----- Assert -----
            key shouldBe "format:ebook"
        }

        @Test
        fun `ReleaseYear key is facet-prefixed with the year`() {
            // ----- Arrange -----
            val value = LibraryFilterValue.ReleaseYear(year = 2021)

            // ----- Act -----
            val key = value.chipKey()

            // ----- Assert -----
            key shouldBe "releaseYear:2021"
        }

        @Test
        fun `ReadYear key is facet-prefixed with the year`() {
            // ----- Arrange -----
            val value = LibraryFilterValue.ReadYear(year = 2020)

            // ----- Act -----
            val key = value.chipKey()

            // ----- Assert -----
            key shouldBe "readYear:2020"
        }

        @Test
        fun `Owned key is facet-prefixed with the boolean`() {
            // ----- Arrange -----
            val value = LibraryFilterValue.Owned(owned = true)

            // ----- Act -----
            val key = value.chipKey()

            // ----- Assert -----
            key shouldBe "owned:true"
        }

        @Test
        fun `RatingMin key is facet-prefixed with the threshold`() {
            // ----- Arrange -----
            val value = LibraryFilterValue.RatingMin(threshold = 4.0)

            // ----- Act -----
            val key = value.chipKey()

            // ----- Assert -----
            key shouldBe "rating:4.0"
        }

        @Test
        fun `keys agree between the active-filter builder and the filter-options builder for the same value`() {
            // ----- Arrange -----
            val options = LibraryFilterOptions(
                supportsOwnedFilter = true,
                formats = listOf("ebook"),
                releaseYears = listOf(2021),
                readYears = listOf(2020),
                tags = listOf(tagFiction),
                ratingBuckets = listOf(4.0),
            )
            val filters = LibraryFilters(
                tags = setOf(tagFiction),
                formats = setOf("ebook"),
                releaseYears = setOf(2021),
                readYear = 2020,
                owned = true,
                ratingMin = 4.0,
            )

            // ----- Act -----
            val optionsChips = buildLibraryFilterChips(options = options)
            val optionsValueByKey = optionsChips.ownershipChips.payloadByKey +
                optionsChips.formatChips.payloadByKey +
                optionsChips.releaseYearChips.payloadByKey +
                optionsChips.readYearChips.payloadByKey +
                optionsChips.tagChips.payloadByKey +
                optionsChips.ratingChips.payloadByKey
            val activeValueByKey = buildLibraryActiveFilterChips(filters = filters).chips.payloadByKey

            // ----- Assert -----
            // buildLibraryFilterChips offers both Owned(true) and Owned(false) as picker options,
            // while buildLibraryActiveFilterChips only ever surfaces the one currently active value
            // — so compare per-value key agreement rather than the two maps' full key sets.
            val sharedValues = activeValueByKey.values

            sharedValues.forEach { value ->
                optionsValueByKey.entries.single { it.value == value }.key shouldBe
                    activeValueByKey.entries.single { it.value == value }.key
            }
        }
    }
}
