package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.Tag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
        fun `no active filters produce no chips, no clearAll and an empty payload map`() {
            // ----- Arrange -----
            val filters = LibraryFilters()

            // ----- Act -----
            val result = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            result.chips.chips shouldBe emptyList()
            result.clearAll shouldBe null
            result.chips.payloadByKey shouldBe emptyMap()
        }

        @Test
        fun `a single active filter produces one chip and no clearAll`() {
            // ----- Arrange -----
            val filters = LibraryFilters(formats = setOf("ebook"))

            // ----- Act -----
            val result = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            result.chips.chips.map { it.key } shouldBe listOf("format:ebook")
            result.clearAll shouldBe null
        }

        @Test
        fun `more than one active filter adds a Tonal clearAll chip with the reserved key`() {
            // ----- Arrange -----
            val filters = LibraryFilters(
                formats = setOf("ebook"),
                releaseYears = setOf(2021),
            )

            // ----- Act -----
            val result = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            result.clearAll shouldBe ChipUiModel(
                key = LIBRARY_CLEAR_ALL_CHIP_KEY,
                label = "Clear all",
                tone = ChipTone.Tonal,
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
            val result = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            result.chips.chips.map { it.key } shouldBe listOf(
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
            val result = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            result.chips.chips.map { it.key }.toSet() shouldBe setOf("tag:1", "tag:2")
            result.chips["tag:1"] shouldBe LibraryFilterValue.Tag(tag = tagFiction)
            result.chips["tag:2"] shouldBe LibraryFilterValue.Tag(tag = tagScifi)
        }

        @Test
        fun `readYear chip is labelled Finished with the year`() {
            // ----- Arrange -----
            val filters = LibraryFilters(readYear = 2022)

            // ----- Act -----
            val result = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            result.chips.chips.single().label shouldBe "Finished 2022"
            result.chips["readYear:2022"] shouldBe LibraryFilterValue.ReadYear(year = 2022)
        }

        @Test
        fun `owned true is labelled Owned`() {
            // ----- Arrange -----
            val filters = LibraryFilters(owned = true)

            // ----- Act -----
            val result = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            result.chips.chips.single().label shouldBe "Owned"
        }

        @Test
        fun `owned false is labelled Unowned`() {
            // ----- Arrange -----
            val filters = LibraryFilters(owned = false)

            // ----- Act -----
            val result = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            result.chips.chips.single().label shouldBe "Unowned"
        }

        @Test
        fun `a whole rating threshold collapses the fractional digit in the label`() {
            // ----- Arrange -----
            val filters = LibraryFilters(ratingMin = 4.0)

            // ----- Act -----
            val result = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            result.chips.chips.single().label shouldBe "4★+"
        }

        @Test
        fun `a fractional rating threshold keeps its fractional digit in the label`() {
            // ----- Arrange -----
            val filters = LibraryFilters(ratingMin = 3.5)

            // ----- Act -----
            val result = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            result.chips.chips.single().label shouldBe "3.5★+"
        }

        @Test
        fun `every chip uses a Container tone with a trailing close icon naming the filter it removes`() {
            // ----- Arrange -----
            val filters = LibraryFilters(
                tags = setOf(tagFiction),
                formats = setOf("ebook"),
            )

            // ----- Act -----
            val result = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            result.chips.chips.forEach { chip ->
                chip.tone shouldBe ChipTone.Container
                chip.trailing shouldBe ChipTrailing.Icon(
                    icon = SoftcoverIcon.Close,
                    description = "Remove filter ${chip.label}",
                )
            }
        }

        @Test
        fun `the ChipSet resolves every chip key back to the LibraryFilterValue that built it`() {
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
            val result = buildLibraryActiveFilterChips(filters = filters)

            // ----- Assert -----
            result.chips.chips.forEach { chip -> (result.chips[chip.key] != null) shouldBe true }
            result.chips["tag:1"] shouldBe LibraryFilterValue.Tag(tag = tagFiction)
            result.chips["format:ebook"] shouldBe LibraryFilterValue.Format(value = "ebook")
            result.chips["releaseYear:2021"] shouldBe LibraryFilterValue.ReleaseYear(year = 2021)
            result.chips["readYear:2020"] shouldBe LibraryFilterValue.ReadYear(year = 2020)
            result.chips["owned:true"] shouldBe LibraryFilterValue.Owned(owned = true)
            result.chips["rating:4.0"] shouldBe LibraryFilterValue.RatingMin(threshold = 4.0)
        }
    }
}
