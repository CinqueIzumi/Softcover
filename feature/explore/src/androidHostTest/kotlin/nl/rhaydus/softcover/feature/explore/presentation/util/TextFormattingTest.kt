package nl.rhaydus.softcover.feature.explore.presentation.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TextFormattingTest {
    @Nested
    inner class ToTitleCaseWords {
        @Test
        fun `capitalizes each whitespace-separated word`() {
            // ----- Arrange -----
            val label = "cosy comforting"

            // ----- Act -----
            val result = label.toTitleCaseWords()

            // ----- Assert -----
            result shouldBe "Cosy Comforting"
        }

        @Test
        fun `leaves a standalone ampersand as-is between capitalized words`() {
            // ----- Arrange -----
            val label = "cosy & comforting"

            // ----- Act -----
            val result = label.toTitleCaseWords()

            // ----- Assert -----
            result shouldBe "Cosy & Comforting"
        }

        @Test
        fun `an empty string stays empty`() {
            // ----- Arrange -----
            val label = ""

            // ----- Act -----
            val result = label.toTitleCaseWords()

            // ----- Assert -----
            result shouldBe ""
        }
    }
}
