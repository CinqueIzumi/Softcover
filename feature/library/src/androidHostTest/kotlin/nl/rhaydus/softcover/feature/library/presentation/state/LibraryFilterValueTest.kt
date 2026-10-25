package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.core.domain.model.TagCategory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryFilterValueTest {
    // region Fixtures
    private val tagFiction = HardcoverTagKey(
        id = 1,
        name = "Fiction",
    )
    private val tagScifi = HardcoverTagKey(
        id = 2,
        name = "Sci-Fi",
    )
    private val yoursCozy = UserTagKey(
        category = TagCategory.MOOD,
        name = "cozy",
    )
    // endregion
    @Nested
    inner class ToggleHardcoverTag {
        @Test
        fun `includes tag when absent`() {
            // ----- Arrange -----
            val filters = LibraryFilters()

            // ----- Act -----
            val result = filters.toggle(
                value = LibraryFilterValue.HardcoverTag(
                    key = tagFiction,
                    direction = TagDirection.INCLUDE,
                ),
            )

            // ----- Assert -----
            result.hardcoverTags.included shouldBe setOf(tagFiction)
        }

        @Test
        fun `clears an included tag when included again`() {
            // ----- Arrange -----
            val filters = LibraryFilters(hardcoverTags = TagSelection(included = persistentSetOf(tagFiction)))

            // ----- Act -----
            val result = filters.toggle(
                value = LibraryFilterValue.HardcoverTag(
                    key = tagFiction,
                    direction = TagDirection.INCLUDE,
                ),
            )

            // ----- Assert -----
            result.hardcoverTags.isEmpty shouldBe true
        }

        @Test
        fun `moves an included tag to excluded when excluded`() {
            // ----- Arrange -----
            val filters = LibraryFilters(hardcoverTags = TagSelection(included = persistentSetOf(tagFiction)))

            // ----- Act -----
            val result = filters.toggle(
                value = LibraryFilterValue.HardcoverTag(
                    key = tagFiction,
                    direction = TagDirection.EXCLUDE,
                ),
            )

            // ----- Assert -----
            result.hardcoverTags.included shouldBe emptySet()
            result.hardcoverTags.excluded shouldBe setOf(tagFiction)
        }

        @Test
        fun `toggling a second tag keeps the first`() {
            // ----- Arrange -----
            val filters = LibraryFilters(hardcoverTags = TagSelection(included = persistentSetOf(tagFiction)))

            // ----- Act -----
            val result = filters.toggle(
                value = LibraryFilterValue.HardcoverTag(
                    key = tagScifi,
                    direction = TagDirection.INCLUDE,
                ),
            )

            // ----- Assert -----
            result.hardcoverTags.included shouldBe setOf(tagFiction, tagScifi)
        }

        @Test
        fun `leaves the Yours selection alone`() {
            // ----- Arrange -----
            val filters = LibraryFilters(yourTags = TagSelection(included = persistentSetOf(yoursCozy)))

            // ----- Act -----
            val result = filters.toggle(
                value = LibraryFilterValue.HardcoverTag(
                    key = tagFiction,
                    direction = TagDirection.INCLUDE,
                ),
            )

            // ----- Assert -----
            result.yourTags.included shouldBe setOf(yoursCozy)
        }
    }

    @Nested
    inner class ToggleYourTag {
        @Test
        fun `excludes tag when absent`() {
            // ----- Arrange -----
            val filters = LibraryFilters()

            // ----- Act -----
            val result = filters.toggle(
                value = LibraryFilterValue.YourTag(
                    key = yoursCozy,
                    direction = TagDirection.EXCLUDE,
                ),
            )

            // ----- Assert -----
            result.yourTags.excluded shouldBe setOf(yoursCozy)
        }

        @Test
        fun `clears an excluded tag when excluded again`() {
            // ----- Arrange -----
            val filters = LibraryFilters(yourTags = TagSelection(excluded = persistentSetOf(yoursCozy)))

            // ----- Act -----
            val result = filters.toggle(
                value = LibraryFilterValue.YourTag(
                    key = yoursCozy,
                    direction = TagDirection.EXCLUDE,
                ),
            )

            // ----- Assert -----
            result.yourTags.isEmpty shouldBe true
        }

        @Test
        fun `leaves the Hardcover selection alone`() {
            // ----- Arrange -----
            val filters = LibraryFilters(hardcoverTags = TagSelection(included = persistentSetOf(tagFiction)))

            // ----- Act -----
            val result = filters.toggle(
                value = LibraryFilterValue.YourTag(
                    key = yoursCozy,
                    direction = TagDirection.INCLUDE,
                ),
            )

            // ----- Assert -----
            result.hardcoverTags.included shouldBe setOf(tagFiction)
        }
    }

    @Nested
    inner class ToggleFormat {
        @Test
        fun `adds format when absent`() {
            // ----- Arrange -----
            val filters = LibraryFilters()

            // ----- Act -----
            val result = filters.toggle(value = LibraryFilterValue.Format("ebook"))

            // ----- Assert -----
            result.formats shouldBe setOf("ebook")
        }
    }

    @Nested
    inner class ToggleReleaseYear {
        @Test
        fun `adds year when absent`() {
            // ----- Arrange -----
            val filters = LibraryFilters()

            // ----- Act -----
            val result = filters.toggle(value = LibraryFilterValue.ReleaseYear(2021))

            // ----- Assert -----
            result.releaseYears shouldBe setOf(2021)
        }
    }

    @Nested
    inner class ToggleReadYear {
        @Test
        fun `sets readYear when previously null`() {
            // ----- Arrange -----
            val filters = LibraryFilters(readYear = null)

            // ----- Act -----
            val result = filters.toggle(value = LibraryFilterValue.ReadYear(2021))

            // ----- Assert -----
            result.readYear shouldBe 2021
        }

        @Test
        fun `clears readYear to null when toggling the same year twice`() {
            // ----- Arrange -----
            val filters = LibraryFilters(readYear = 2021)

            // ----- Act -----
            val result = filters.toggle(value = LibraryFilterValue.ReadYear(2021))

            // ----- Assert -----
            result.readYear shouldBe null
        }

        @Test
        fun `replaces readYear when toggling a different year — single-select`() {
            // ----- Arrange -----
            val filters = LibraryFilters(readYear = 2021)

            // ----- Act -----
            val result = filters.toggle(value = LibraryFilterValue.ReadYear(2019))

            // ----- Assert -----
            result.readYear shouldBe 2019
        }
    }

    @Nested
    inner class ToggleOwned {
        @Test
        fun `sets owned to true when previously null`() {
            // ----- Arrange -----
            val filters = LibraryFilters(owned = null)

            // ----- Act -----
            val result = filters.toggle(value = LibraryFilterValue.Owned(true))

            // ----- Assert -----
            result.owned shouldBe true
        }

        @Test
        fun `clears owned to null when toggling same value twice (Owned=true then Owned=true)`() {
            // ----- Arrange -----
            val filters = LibraryFilters(owned = true)

            // ----- Act -----
            val result = filters.toggle(value = LibraryFilterValue.Owned(true))

            // ----- Assert -----
            result.owned shouldBe null
        }

        @Test
        fun `switches from Owned(true) to Owned(false) when toggling the opposite value`() {
            // ----- Arrange -----
            val filters = LibraryFilters(owned = true)

            // ----- Act -----
            val result = filters.toggle(value = LibraryFilterValue.Owned(false))

            // ----- Assert -----
            result.owned shouldBe false
        }
    }

    @Nested
    inner class ToggleRatingMin {
        @Test
        fun `sets ratingMin when previously null`() {
            // ----- Arrange -----
            val filters = LibraryFilters(ratingMin = null)

            // ----- Act -----
            val result = filters.toggle(value = LibraryFilterValue.RatingMin(4.0))

            // ----- Assert -----
            result.ratingMin shouldBe 4.0
        }

        @Test
        fun `clears ratingMin to null when toggling the same threshold twice`() {
            // ----- Arrange -----
            val filters = LibraryFilters(ratingMin = 4.0)

            // ----- Act -----
            val result = filters.toggle(value = LibraryFilterValue.RatingMin(4.0))

            // ----- Assert -----
            result.ratingMin shouldBe null
        }

        @Test
        fun `replaces ratingMin when toggling a different threshold`() {
            // ----- Arrange -----
            val filters = LibraryFilters(ratingMin = 4.0)

            // ----- Act -----
            val result = filters.toggle(value = LibraryFilterValue.RatingMin(4.5))

            // ----- Assert -----
            result.ratingMin shouldBe 4.5
        }
    }

    @Nested
    inner class OtherFacetsPreserved {
        @Test
        fun `toggling one facet does not change other facets`() {
            // ----- Arrange -----
            val filters = LibraryFilters(
                hardcoverTags = TagSelection(included = persistentSetOf(tagFiction)),
                formats = persistentSetOf("paperback"),
                releaseYears = persistentSetOf(2020),
                readYear = 2019,
                owned = true,
                ratingMin = 4.0,
            )

            // ----- Act -----
            val result = filters.toggle(value = LibraryFilterValue.ReleaseYear(2021))

            // ----- Assert -----
            result.hardcoverTags.included shouldBe setOf(tagFiction)
            result.formats shouldBe setOf("paperback")
            result.readYear shouldBe 2019
            result.owned shouldBe true
            result.ratingMin shouldBe 4.0
        }
    }
}
