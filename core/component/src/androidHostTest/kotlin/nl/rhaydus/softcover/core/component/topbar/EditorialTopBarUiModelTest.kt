package nl.rhaydus.softcover.core.component.topbar

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EditorialTopBarUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `includes a titled fixture`() {
            // ----- Arrange & Act -----
            val hasTitled = EditorialTopBarUiModel.previews.any { it.title != null }

            // ----- Assert -----
            hasTitled shouldBe true
        }

        @Test
        fun `includes an untitled fixture`() {
            // ----- Arrange & Act -----
            val hasUntitled = EditorialTopBarUiModel.previews.any { it.title == null }

            // ----- Assert -----
            hasUntitled shouldBe true
        }
    }
}
