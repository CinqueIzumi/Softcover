package nl.rhaydus.softcover.feature.settings.presentation.model

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryVisibilityToggleTest {
    @Nested
    inner class LibraryVisibilityToggleFor {
        @Test
        fun `checks the toggle and words its description by visibility, always naming the tab`() {
            // ----- Arrange -----
            val label = "Favourites"

            // ----- Act -----
            val visible = libraryVisibilityToggleFor(
                label = label,
                visible = true,
            )
            val hidden = libraryVisibilityToggleFor(
                label = label,
                visible = false,
            )

            // ----- Assert -----
            visible.checked shouldBe true
            hidden.checked shouldBe false
            visible.description shouldContain "Favourites"
            hidden.description shouldContain "Favourites"
            visible.description shouldNotBe hidden.description
        }
    }
}
