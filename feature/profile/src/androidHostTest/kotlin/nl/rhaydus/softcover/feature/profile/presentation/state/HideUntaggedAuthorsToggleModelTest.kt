package nl.rhaydus.softcover.feature.profile.presentation.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HideUntaggedAuthorsToggleModelTest {
    @Nested
    inner class HideUntaggedAuthorsToggleFor {
        @Test
        fun `is checked when hiding`() {
            // ----- Arrange & Act -----
            val result = hideUntaggedAuthorsToggleFor(hide = true)

            // ----- Assert -----
            result.checked shouldBe true
        }

        @Test
        fun `is unchecked when not hiding`() {
            // ----- Arrange & Act -----
            val result = hideUntaggedAuthorsToggleFor(hide = false)

            // ----- Assert -----
            result.checked shouldBe false
        }

        @Test
        fun `keeps the same key whatever the state so the event can be routed`() {
            // ----- Arrange & Act -----
            val on = hideUntaggedAuthorsToggleFor(hide = true)
            val off = hideUntaggedAuthorsToggleFor(hide = false)

            // ----- Assert -----
            on.key shouldBe off.key
        }

        @Test
        fun `is enabled`() {
            // ----- Arrange & Act -----
            val result = hideUntaggedAuthorsToggleFor(hide = false)

            // ----- Assert -----
            result.enabled shouldBe true
        }
    }
}
