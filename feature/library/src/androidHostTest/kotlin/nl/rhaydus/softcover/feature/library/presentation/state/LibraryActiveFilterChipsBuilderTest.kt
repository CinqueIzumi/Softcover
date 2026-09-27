package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.ChipVariant
import nl.rhaydus.softcover.core.domain.model.Tag

class LibraryActiveFilterChipsBuilderTest {
    private val tagFiction = Tag(
        id = 1,
        name = "Fiction",
    )
    private val tagScifi = Tag(
        id = 2,
        name = "Sci-Fi",
    )

    @Nested
    inner class BuildLibraryActiveFilterChips {
        @Test
        fun `no active filters produce no chips, no clearAll and an empty value map`() {
            // ----- Arrange -----
            val filters = LibraryFilters()

            // ----- Act -----
            val (chips, valueByKey) = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            chips.chips shouldBe emptyList()
            chips.clearAll shouldBe null
            valueByKey shouldBe emptyMap()
        }

        @Test
        fun `a single active filter produces one chip and no clearAll`() {
            // ----- Arrange -----
            val filters = LibraryFilters(formats = setOf("ebook"))

            // ----- Act -----
            val (chips, _) = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            chips.chips.map { it.key } shouldBe listOf("format:ebook")
            chips.clearAll shouldBe null
        }

        @Test
        fun `more than one active filter adds a Quiet clearAll chip with the reserved key`() {
            // ----- Arrange -----
            val filters = LibraryFilters(
                formats = setOf("ebook"),
                releaseYears = setOf(2021),
            )

            // ----- Act -----
            val (chips, _) = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            chips.clearAll shouldBe ChipUiModel(
                key = LIBRARY_CLEAR_ALL_CHIP_KEY,
                label = "Clear all",
                variant = ChipVariant.Quiet(),
            )
        }

        @Test
        fun `chips are ordered tags, formats, releaseYears, readYear, owned, rating`() {
            // ----- Arrange -----
            val filters = LibraryFilters(
                tags = setOf(tagFiction),
                formats = setOf("ebook"),
                releaseYears = setOf(2021),
                readYear = 2020,
                owned = true,
                ratingMin = 4.0,
            )

            // ----- Act -----
            val (chips, _) = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            chips.chips.map { it.key } shouldBe listOf(
                "tag:1",
                "format:ebook",
                "releaseYear:2021",
                "readYear:2020",
                "owned:true",
                "rating:4.0",
            )
        }

        @Test
        fun `builds one tag chip per tag labelled with the tag name`() {
            // ----- Arrange -----
            val filters = LibraryFilters(tags = setOf(tagFiction, tagScifi))

            // ----- Act -----
            val (chips, valueByKey) = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            chips.chips.map { it.key }.toSet() shouldBe setOf("tag:1", "tag:2")
            valueByKey["tag:1"] shouldBe LibraryFilterValue.Tag(tag = tagFiction)
            valueByKey["tag:2"] shouldBe LibraryFilterValue.Tag(tag = tagScifi)
        }

        @Test
        fun `readYear chip is labelled Finished with the year`() {
            // ----- Arrange -----
            val filters = LibraryFilters(readYear = 2022)

            // ----- Act -----
            val (chips, valueByKey) = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            chips.chips.single().label shouldBe "Finished 2022"
            valueByKey["readYear:2022"] shouldBe LibraryFilterValue.ReadYear(year = 2022)
        }

        @Test
        fun `owned true is labelled Owned`() {
            // ----- Arrange -----
            val filters = LibraryFilters(owned = true)

            // ----- Act -----
            val (chips, _) = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            chips.chips.single().label shouldBe "Owned"
        }

        @Test
        fun `owned false is labelled Unowned`() {
            // ----- Arrange -----
            val filters = LibraryFilters(owned = false)

            // ----- Act -----
            val (chips, _) = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            chips.chips.single().label shouldBe "Unowned"
        }

        @Test
        fun `a whole rating threshold collapses the fractional digit in the label`() {
            // ----- Arrange -----
            val filters = LibraryFilters(ratingMin = 4.0)

            // ----- Act -----
            val (chips, _) = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            chips.chips.single().label shouldBe "4★+"
        }

        @Test
        fun `a fractional rating threshold keeps its fractional digit in the label`() {
            // ----- Arrange -----
            val filters = LibraryFilters(ratingMin = 3.5)

            // ----- Act -----
            val (chips, _) = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            chips.chips.single().label shouldBe "3.5★+"
        }

        @Test
        fun `every chip uses a Remove variant with a label naming the filter it removes`() {
            // ----- Arrange -----
            val filters = LibraryFilters(
                tags = setOf(tagFiction),
                formats = setOf("ebook"),
            )

            // ----- Act -----
            val (chips, _) = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            chips.chips.forEach { chip ->
                chip.variant shouldBe ChipVariant.Remove(removeLabel = "Remove filter ${chip.label}")
            }
        }

        @Test
        fun `valueByKey resolves every chip key back to the LibraryFilterValue that built it`() {
            // ----- Arrange -----
            val filters = LibraryFilters(
                tags = setOf(tagFiction),
                formats = setOf("ebook"),
                releaseYears = setOf(2021),
                readYear = 2020,
                owned = true,
                ratingMin = 4.0,
            )

            // ----- Act -----
            val (chips, valueByKey) = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            chips.chips.forEach { chip -> (chip.key in valueByKey) shouldBe true }
            valueByKey["tag:1"] shouldBe LibraryFilterValue.Tag(tag = tagFiction)
            valueByKey["format:ebook"] shouldBe LibraryFilterValue.Format(value = "ebook")
            valueByKey["releaseYear:2021"] shouldBe LibraryFilterValue.ReleaseYear(year = 2021)
            valueByKey["readYear:2020"] shouldBe LibraryFilterValue.ReadYear(year = 2020)
            valueByKey["owned:true"] shouldBe LibraryFilterValue.Owned(owned = true)
            valueByKey["rating:4.0"] shouldBe LibraryFilterValue.RatingMin(threshold = 4.0)
        }
    }
}
