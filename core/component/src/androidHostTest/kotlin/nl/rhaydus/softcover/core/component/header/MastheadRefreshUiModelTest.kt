package nl.rhaydus.softcover.core.component.header

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MastheadRefreshUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `there are exactly two fixtures, idle and busy`() {
            // ----- Act & Assert -----
            MastheadRefreshUiModel.previews.size shouldBe 2
        }

        @Test
        fun `the idle fixture reads Refresh and is enabled`() {
            // ----- Act -----
            val idle = MastheadRefreshUiModel.previews[0]

            // ----- Assert -----
            idle.label shouldBe "Refresh"
            idle.enabled shouldBe true
        }

        @Test
        fun `the busy fixture reads Refreshing and is disabled`() {
            // ----- Act -----
            val busy = MastheadRefreshUiModel.previews[1]

            // ----- Assert -----
            busy.label shouldBe "Refreshing…"
            busy.enabled shouldBe false
        }
    }

    @Nested
    inner class Equality {
        @Test
        fun `two models with the same label and enabled state are equal`() {
            // ----- Arrange -----
            val first = MastheadRefreshUiModel(
                label = "Refresh",
                enabled = true,
            )
            val second = MastheadRefreshUiModel(
                label = "Refresh",
                enabled = true,
            )

            // ----- Act & Assert -----
            first shouldBe second
        }

        @Test
        fun `models with different labels are not equal`() {
            // ----- Arrange -----
            val refresh = MastheadRefreshUiModel(
                label = "Refresh",
                enabled = true,
            )
            val refreshing = MastheadRefreshUiModel(
                label = "Refreshing…",
                enabled = true,
            )

            // ----- Act & Assert -----
            refresh shouldNotBe refreshing
        }

        @Test
        fun `models with different enabled states are not equal`() {
            // ----- Arrange -----
            val enabled = MastheadRefreshUiModel(
                label = "Refresh",
                enabled = true,
            )
            val disabled = MastheadRefreshUiModel(
                label = "Refresh",
                enabled = false,
            )

            // ----- Act & Assert -----
            enabled shouldNotBe disabled
        }
    }
}
