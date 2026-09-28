package nl.rhaydus.softcover.core.component.divider

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DividerUiModelTest {
    @Nested
    inner class Previews {
        private val expectedVariants = listOf(
            DividerVariant.DebugRoutesRow::class,
            DividerVariant.ReadingLifeShareCard::class,
            DividerVariant.SettingsRoadmap::class,
            DividerVariant.CreateListQuote::class,
            DividerVariant.OnboardingOrTypeIt::class,
        )

        @Test
        fun `there is exactly one fixture per DividerVariant subtype`() {
            // ----- Act & Assert -----
            DividerUiModel.previews.size shouldBe expectedVariants.size
        }

        @Test
        fun `the fixtures cover every subtype with no duplicates`() {
            // ----- Act -----
            val variants = DividerUiModel.previews.map { it.variant::class }

            // ----- Assert -----
            variants.toSet() shouldBe expectedVariants.toSet()
            variants.distinct().size shouldBe variants.size
        }
    }
}
