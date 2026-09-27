package nl.rhaydus.softcover.core.component.header

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import nl.rhaydus.softcover.core.designsystem.presentation.theme.EditorialTypography
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PageMastheadDimensionsTest {
    // region Fixtures
    /** Every role gets a distinct font size so a resolved style can only match its intended role. */
    private val typography = EditorialTypography(
        eyebrow = TextStyle(fontSize = 1.sp),
        eyebrowSmall = TextStyle(fontSize = 2.sp),
        pageTitle = TextStyle(fontSize = 3.sp),
        display = TextStyle(fontSize = 4.sp),
        headlineMedium = TextStyle(fontSize = 5.sp),
        headlineSmall = TextStyle(fontSize = 6.sp),
        titleLarge = TextStyle(fontSize = 7.sp),
        titleMedium = TextStyle(fontSize = 8.sp),
        titleSmall = TextStyle(fontSize = 9.sp),
        bodyLarge = TextStyle(fontSize = 10.sp),
        body = TextStyle(fontSize = 11.sp),
        bodySmall = TextStyle(fontSize = 12.sp),
        review = TextStyle(fontSize = 13.sp),
        statHero = TextStyle(fontSize = 14.sp),
        statLarge = TextStyle(fontSize = 15.sp),
        quoteGlyph = TextStyle(fontSize = 16.sp),
    )

    private fun dimensionsFor(size: PageMastheadSize) = PageMastheadDimensions.forSize(
        size = size,
        typography = typography,
    )
    // endregion
    @Nested
    inner class ForSize {
        @Test
        fun `every PageMastheadSize resolves a PageMastheadDimensions`() {
            // ----- Act -----
            val resolved = PageMastheadSize.entries.map { dimensionsFor(it) }

            // ----- Assert -----
            resolved.size shouldBe PageMastheadSize.entries.size
        }

        @Test
        fun `subtitleMaxWidth is 300dp for Regular`() {
            // ----- Act -----
            val dimensions = dimensionsFor(PageMastheadSize.Regular)

            // ----- Assert -----
            dimensions.subtitleMaxWidth shouldBe 300.dp
        }

        @Test
        fun `subtitleMaxWidth is 300dp for Compact`() {
            // ----- Act -----
            val dimensions = dimensionsFor(PageMastheadSize.Compact)

            // ----- Assert -----
            dimensions.subtitleMaxWidth shouldBe 300.dp
        }

        @Test
        fun `Regular resolves pageTitle, body and a 6dp title-to-subtitle gap`() {
            // ----- Act -----
            val dimensions = dimensionsFor(PageMastheadSize.Regular)

            // ----- Assert -----
            dimensions.titleStyle shouldBe typography.pageTitle
            dimensions.subtitleStyle shouldBe typography.body
            dimensions.titleToSubtitleGap shouldBe 6.dp
        }

        @Test
        fun `Compact resolves headlineMedium, bodySmall and a 4dp title-to-subtitle gap`() {
            // ----- Act -----
            val dimensions = dimensionsFor(PageMastheadSize.Compact)

            // ----- Assert -----
            dimensions.titleStyle shouldBe typography.headlineMedium
            dimensions.subtitleStyle shouldBe typography.bodySmall
            dimensions.titleToSubtitleGap shouldBe 4.dp
        }

        @Test
        fun `Regular and Compact resolve different title and subtitle metrics`() {
            // ----- Act -----
            val regular = dimensionsFor(PageMastheadSize.Regular)
            val compact = dimensionsFor(PageMastheadSize.Compact)

            // ----- Assert -----
            regular.titleStyle shouldNotBe compact.titleStyle
            regular.subtitleStyle shouldNotBe compact.subtitleStyle
            regular.titleToSubtitleGap shouldNotBe compact.titleToSubtitleGap
        }
    }
}
