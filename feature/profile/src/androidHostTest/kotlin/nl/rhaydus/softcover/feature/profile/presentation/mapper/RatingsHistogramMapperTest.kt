package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RatingsHistogramMapperTest {
    @Nested
    inner class ToRatingsHistogramHeader {
        @Test
        fun `GENEROUS band header carries the shared eyebrow and its own headline`() {
            // ----- Arrange & Act -----
            val result = RatingBand.GENEROUS.toRatingsHistogramHeader()

            // ----- Assert -----
            result shouldBe SectionHeaderUiModel.Section(
                eyebrow = "How you score",
                headline = "A generous but honest hand",
            )
        }

        @Test
        fun `SUPERFAN band header carries the shared eyebrow and its own headline`() {
            // ----- Arrange & Act -----
            val result = RatingBand.SUPERFAN.toRatingsHistogramHeader()

            // ----- Assert -----
            result shouldBe SectionHeaderUiModel.Section(
                eyebrow = "How you score",
                headline = "Easily, happily impressed",
            )
        }

        @Test
        fun `CENTRIST band header carries the shared eyebrow and its own headline`() {
            // ----- Arrange & Act -----
            val result = RatingBand.CENTRIST.toRatingsHistogramHeader()

            // ----- Assert -----
            result shouldBe SectionHeaderUiModel.Section(
                eyebrow = "How you score",
                headline = "You keep to the middle of the shelf",
            )
        }

        @Test
        fun `EXACTING band header carries the shared eyebrow and its own headline`() {
            // ----- Arrange & Act -----
            val result = RatingBand.EXACTING.toRatingsHistogramHeader()

            // ----- Assert -----
            result shouldBe SectionHeaderUiModel.Section(
                eyebrow = "How you score",
                headline = "A hard star to earn",
            )
        }

        @Test
        fun `POLARISED band header carries the shared eyebrow and its own headline`() {
            // ----- Arrange & Act -----
            val result = RatingBand.POLARISED.toRatingsHistogramHeader()

            // ----- Assert -----
            result shouldBe SectionHeaderUiModel.Section(
                eyebrow = "How you score",
                headline = "Love it or leave it",
            )
        }

        @Test
        fun `NEW_READER band header carries the shared eyebrow and its own headline`() {
            // ----- Arrange & Act -----
            val result = RatingBand.NEW_READER.toRatingsHistogramHeader()

            // ----- Assert -----
            result shouldBe SectionHeaderUiModel.Section(
                eyebrow = "How you score",
                headline = "Still finding your scale",
            )
        }

        @Test
        fun `header never carries a description`() {
            // ----- Arrange & Act -----
            val result = RatingBand.CENTRIST.toRatingsHistogramHeader()

            // ----- Assert -----
            result.description shouldBe null
        }
    }

    @Nested
    inner class ToRatingsHistogramCaption {
        @Test
        fun `GENEROUS band caption describes the half-star rounding pattern`() {
            // ----- Arrange & Act -----
            val result = RatingBand.GENEROUS.toRatingsHistogramCaption()

            // ----- Assert -----
            result shouldBe "Half-stars count. Most of yours land between four and five — you round up " +
                "when a book earns it."
        }

        @Test
        fun `SUPERFAN band caption describes the above four-and-a-half average`() {
            // ----- Arrange & Act -----
            val result = RatingBand.SUPERFAN.toRatingsHistogramCaption()

            // ----- Assert -----
            result shouldBe "Your average sits north of four and a half — you finish what you start, " +
                "and it usually earns the top shelf."
        }

        @Test
        fun `CENTRIST band caption describes the clustering around three and a half`() {
            // ----- Arrange & Act -----
            val result = RatingBand.CENTRIST.toRatingsHistogramCaption()

            // ----- Assert -----
            result shouldBe "Your ratings cluster around three and a half — steady and considered, " +
                "rarely swinging to an extreme."
        }

        @Test
        fun `EXACTING band caption describes the low average`() {
            // ----- Arrange & Act -----
            val result = RatingBand.EXACTING.toRatingsHistogramCaption()

            // ----- Assert -----
            result shouldBe "Your average runs low — a five-star read is rare here, which makes the " +
                "ones you give out mean something."
        }

        @Test
        fun `POLARISED band caption describes the two-camp split`() {
            // ----- Arrange & Act -----
            val result = RatingBand.POLARISED.toRatingsHistogramCaption()

            // ----- Assert -----
            result shouldBe "Your ratings split into two camps, low and high, with little in between " +
                "— a book either wins you over or it doesn't."
        }

        @Test
        fun `NEW_READER band caption asks for more ratings`() {
            // ----- Arrange & Act -----
            val result = RatingBand.NEW_READER.toRatingsHistogramCaption()

            // ----- Assert -----
            result shouldBe "Not enough ratings yet to say much about your taste — a few more books " +
                "will fill this in."
        }
    }
}
