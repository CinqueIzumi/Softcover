package nl.rhaydus.softcover.feature.settings.presentation.model

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryVisibilityToggleTest {
    @Nested
    inner class LibraryVisibilityToggleFor {
        @Test
        fun `is checked while the tab is visible`() {
            // ----- Arrange & Act -----
            val result = libraryVisibilityToggleFor(
                label = "Reading",
                visible = true,
            )

            // ----- Assert -----
            result.checked shouldBe true
        }

        @Test
        fun `is unchecked while the tab is hidden`() {
            // ----- Arrange & Act -----
            val result = libraryVisibilityToggleFor(
                label = "Reading",
                visible = false,
            )

            // ----- Assert -----
            result.checked shouldBe false
        }

        @Test
        fun `describes hiding the named tab while it is visible`() {
            // ----- Arrange & Act -----
            val result = libraryVisibilityToggleFor(
                label = "Reading",
                visible = true,
            )

            // ----- Assert -----
            result.description shouldBe "Hide Reading from the library tabs"
        }

        @Test
        fun `describes showing the named tab while it is hidden`() {
            // ----- Arrange & Act -----
            val result = libraryVisibilityToggleFor(
                label = "Favourites",
                visible = false,
            )

            // ----- Assert -----
            result.description shouldContain "Favourites"
            result.description shouldBe "Show Favourites on the library tabs"
        }

        @Test
        fun `keeps one key for visible and hidden so the event can be routed`() {
            // ----- Arrange & Act -----
            val visible = libraryVisibilityToggleFor(
                label = "Reading",
                visible = true,
            )
            val hidden = libraryVisibilityToggleFor(
                label = "Reading",
                visible = false,
            )

            // ----- Assert -----
            visible.key shouldBe hidden.key
        }
    }
}
