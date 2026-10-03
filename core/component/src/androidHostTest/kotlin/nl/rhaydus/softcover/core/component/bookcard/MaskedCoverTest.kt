package nl.rhaydus.softcover.core.component.bookcard

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MaskedCoverTest {
    private val cover = CoverUiModel(
        source = CoverSource.Local(
            path = "/fixtures/cover.jpg",
            cacheKey = null,
        ),
        coverlessTitle = "Piranesi",
        variant = CoverVariant.LibraryShelfItem,
        isLoading = false,
        sharedTransitionKey = "book-1",
    )

    @Nested
    inner class NotLoading {
        @Test
        fun `returns the cover untouched`() {
            // ----- Arrange -----
            val input = cover

            // ----- Act -----
            val result = maskedCover(
                cover = input,
                isLoading = false,
            )

            // ----- Assert -----
            result shouldBe cover
        }

        @Test
        fun `returns null for a null cover`() {
            // ----- Arrange -----
            val input: CoverUiModel? = null

            // ----- Act -----
            val result = maskedCover(
                cover = input,
                isLoading = false,
            )

            // ----- Assert -----
            result shouldBe null
        }
    }

    @Nested
    inner class Loading {
        @Test
        fun `drops the source and shared transition key and marks the cover loading`() {
            // ----- Arrange -----
            val input = cover

            // ----- Act -----
            val result = maskedCover(
                cover = input,
                isLoading = true,
            )

            // ----- Assert -----
            result shouldBe cover.copy(
                source = null,
                isLoading = true,
                sharedTransitionKey = null,
            )
        }

        @Test
        fun `keeps the variant and coverless title so the placeholder keeps its shape`() {
            // ----- Arrange -----
            val input = cover

            // ----- Act -----
            val result = maskedCover(
                cover = input,
                isLoading = true,
            )

            // ----- Assert -----
            result?.variant shouldBe CoverVariant.LibraryShelfItem
            result?.coverlessTitle shouldBe "Piranesi"
        }

        @Test
        fun `returns null for a null cover`() {
            // ----- Arrange -----
            val input: CoverUiModel? = null

            // ----- Act -----
            val result = maskedCover(
                cover = input,
                isLoading = true,
            )

            // ----- Assert -----
            result shouldBe null
        }
    }
}
