package nl.rhaydus.softcover.core.component.divider

import androidx.compose.ui.unit.dp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DividerStyleTest {
    @Nested
    inner class Style {
        @Test
        fun `DebugRoutesRow insets 20dp with an outline ink`() {
            // ----- Act -----
            val style = HorizontalDividerSurface.DebugRoutesRow.style()

            // ----- Assert -----
            style shouldBe HorizontalDividerStyle(
                ink = DividerInk.Outline,
                inset = 20.dp,
                centerGap = 12.dp,
            )
        }

        @Test
        fun `ReadingLifeShareCard has no inset and tints the content colour at 0,25`() {
            // ----- Act -----
            val style = HorizontalDividerSurface.ReadingLifeShareCard.style()

            // ----- Assert -----
            style shouldBe HorizontalDividerStyle(
                ink = DividerInk.ContentTint(alpha = 0.25f),
                inset = 0.dp,
                centerGap = 12.dp,
            )
        }

        @Test
        fun `SettingsRoadmap has no inset and an outline ink`() {
            // ----- Act -----
            val style = HorizontalDividerSurface.SettingsRoadmap.style()

            // ----- Assert -----
            style shouldBe HorizontalDividerStyle(
                ink = DividerInk.Outline,
                inset = 0.dp,
                centerGap = 12.dp,
            )
        }

        @Test
        fun `CreateListQuote has no inset and an outline ink`() {
            // ----- Act -----
            val style = HorizontalDividerSurface.CreateListQuote.style()

            // ----- Assert -----
            style shouldBe HorizontalDividerStyle(
                ink = DividerInk.Outline,
                inset = 0.dp,
                centerGap = 12.dp,
            )
        }

        @Test
        fun `OnboardingOrTypeIt has no inset and an outline ink`() {
            // ----- Act -----
            val style = HorizontalDividerSurface.OnboardingOrTypeIt.style()

            // ----- Assert -----
            style shouldBe HorizontalDividerStyle(
                ink = DividerInk.Outline,
                inset = 0.dp,
                centerGap = 12.dp,
            )
        }

        @Test
        fun `the centre gap is 12dp for every surface`() {
            // ----- Arrange -----
            val surfaces = listOf(
                HorizontalDividerSurface.DebugRoutesRow,
                HorizontalDividerSurface.ReadingLifeShareCard,
                HorizontalDividerSurface.SettingsRoadmap,
                HorizontalDividerSurface.CreateListQuote,
                HorizontalDividerSurface.OnboardingOrTypeIt,
            )

            // ----- Act -----
            val centerGaps = surfaces.map { it.style().centerGap }.toSet()

            // ----- Assert -----
            centerGaps shouldBe setOf(12.dp)
        }
    }

    @Nested
    inner class Ink {
        @Test
        fun `WidePaneSeparator resolves to an outline ink`() {
            // ----- Act -----
            val ink = VerticalDividerSurface.WidePaneSeparator.ink()

            // ----- Assert -----
            ink shouldBe DividerInk.Outline
        }
    }
}
