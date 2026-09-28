package nl.rhaydus.softcover.core.component.divider

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DividerUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `every HorizontalDividerSurface value appears in at least one Horizontal fixture`() {
            // ----- Arrange -----
            val expectedSurfaces = setOf(
                HorizontalDividerSurface.DebugRoutesRow,
                HorizontalDividerSurface.ReadingLifeShareCard,
                HorizontalDividerSurface.SettingsRoadmap,
                HorizontalDividerSurface.CreateListQuote,
                HorizontalDividerSurface.OnboardingOrTypeIt,
            )

            // ----- Act -----
            val coveredSurfaces = DividerUiModel.previews
                .filterIsInstance<DividerUiModel.Horizontal>()
                .map { it.surface }
                .toSet()

            // ----- Assert -----
            coveredSurfaces shouldBe expectedSurfaces
        }

        @Test
        fun `at least one fixture carries a QuoteMark centre`() {
            // ----- Arrange & Act -----
            val withQuoteMark = DividerUiModel.previews
                .filterIsInstance<DividerUiModel.Horizontal>()
                .filter { it.center == DividerCenter.QuoteMark }

            // ----- Assert -----
            withQuoteMark.size shouldBe 1
        }

        @Test
        fun `at least one fixture carries a Label centre`() {
            // ----- Arrange & Act -----
            val withLabel = DividerUiModel.previews
                .filterIsInstance<DividerUiModel.Horizontal>()
                .filter { it.center is DividerCenter.Label }

            // ----- Assert -----
            withLabel.size shouldBe 1
        }

        @Test
        fun `at least one fixture is Vertical`() {
            // ----- Arrange & Act -----
            val vertical = DividerUiModel.previews.filterIsInstance<DividerUiModel.Vertical>()

            // ----- Assert -----
            vertical.size shouldBe 1
        }
    }
}
