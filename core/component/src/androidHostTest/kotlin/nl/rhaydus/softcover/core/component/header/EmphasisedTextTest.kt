package nl.rhaydus.softcover.core.component.header

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EmphasisedTextTest {
    private val colorScheme = lightColorScheme(primary = Color.Red)

    private fun emphasis(substring: String) = TextEmphasis(
        substring = substring,
        treatment = EmphasisTreatment.Accent,
    )

    @Nested
    inner class Matching {
        @Test
        fun `substring found is styled over exactly its range`() {
            // ----- Act -----
            val result = emphasisedText(
                "Shelve Piranesi",
                emphasis("Piranesi"),
                colorScheme,
            )

            // ----- Assert -----
            result.text shouldBe "Shelve Piranesi"
            result.spanStyles shouldHaveSize 1
            result.spanStyles.single().start shouldBe 7
            result.spanStyles.single().end shouldBe 15
            result.spanStyles.single().item.color shouldBe Color.Red
        }

        @Test
        fun `repeated substring is styled at its last occurrence`() {
            // ----- Act -----
            val result = emphasisedText(
                "Dune and Dune",
                emphasis("Dune"),
                colorScheme,
            )

            // ----- Assert -----
            result.spanStyles.single().start shouldBe 9
            result.spanStyles.single().end shouldBe 13
        }
    }

    @Nested
    inner class NoEmphasis {
        @Test
        fun `substring not found returns plain text`() {
            // ----- Act -----
            val result = emphasisedText(
                "Shelve Piranesi",
                emphasis("Dune"),
                colorScheme,
            )

            // ----- Assert -----
            result shouldBe AnnotatedString("Shelve Piranesi")
            result.spanStyles.shouldBeEmpty()
        }

        @Test
        fun `null emphasis returns plain text`() {
            // ----- Act -----
            val result = emphasisedText(
                "Shelve Piranesi",
                emphasis = null,
                colorScheme = colorScheme,
            )

            // ----- Assert -----
            result shouldBe AnnotatedString("Shelve Piranesi")
        }

        @Test
        fun `empty substring returns plain text`() {
            // ----- Act -----
            val result = emphasisedText(
                "Shelve Piranesi",
                emphasis(""),
                colorScheme,
            )

            // ----- Assert -----
            result.spanStyles.shouldBeEmpty()
        }
    }
}
