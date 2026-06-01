package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Tag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryFilterValueTest {

    // region Fixtures

    private val tagFiction = Tag(id = 1, name = "Fiction")
    private val tagScifi = Tag(id = 2, name = "Sci-Fi")

    // endregion

    @Nested
    inner class ToggleTag {

        @Test
        fun `adds tag when absent`() {
            // ----- Arrange -----
            val filters = LibraryFilters()

            // ----- Act -----
            val result = filters.toggle(value = LibraryFilterValue.Tag(tagFiction))

            // ----- Assert -----
            result.tags shouldBe setOf(tagFiction)
        }

        @Test
        fun `removes tag when already present`() {
            // ----- Arrange -----
            val filters = LibraryFilters(tags = setOf(tagFiction))

            // ----- Act -----
            val result = filters.toggle(value = LibraryFilterValue.Tag(tagFiction))

            // ----- Assert -----
            result.tags shouldBe emptySet()
        }

        @Test
        fun `toggling a second tag adds it without removing the first`() {
            // ----- Arrange -----
            val filters = LibraryFilters(tags = setOf(tagFiction))

            // ----- Act -----
            val result = filters.toggle(value = LibraryFilterValue.Tag(tagScifi))

            // ----- Assert -----
            result.tags shouldBe setOf(tagFiction, tagScifi)
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

        @Test
        fun `removes format when already present`() {
            // ----- Arrange -----
            val filters = LibraryFilters(formats = setOf("ebook"))

            // ----- Act -----
            val result = filters.toggle(value = LibraryFilterValue.Format("ebook"))

            // ----- Assert -----
            result.formats shouldBe emptySet()
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

        @Test
        fun `removes year when already present`() {
            // ----- Arrange -----
            val filters = LibraryFilters(releaseYears = setOf(2021))

            // ----- Act -----
            val result = filters.toggle(value = LibraryFilterValue.ReleaseYear(2021))

            // ----- Assert -----
            result.releaseYears shouldBe emptySet()
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
                tags = setOf(tagFiction),
                formats = setOf("paperback"),
                releaseYears = setOf(2020),
                owned = true,
                ratingMin = 4.0,
            )

            // ----- Act -----
            val result = filters.toggle(value = LibraryFilterValue.ReleaseYear(2021))

            // ----- Assert -----
            result.tags shouldBe setOf(tagFiction)
            result.formats shouldBe setOf("paperback")
            result.owned shouldBe true
            result.ratingMin shouldBe 4.0
        }
    }
}
