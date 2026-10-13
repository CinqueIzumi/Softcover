package nl.rhaydus.softcover.core.component.header

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HeaderAnnotatedStringTest {
    private val primary = Color(0xFF123456)
    private val colorScheme = lightColorScheme(primary = primary)

    @Nested
    inner class Build {
        @Test
        fun `no segments produce an empty string`() {
            // ----- Arrange -----
            val segments = emptyList<HeaderTextSegment>()
            val colorScheme = colorScheme

            // ----- Act -----
            val result = headerAnnotatedString(
                segments = segments,
                colorScheme = colorScheme,
            )

            // ----- Assert -----
            result shouldBe AnnotatedString("")
        }

        @Test
        fun `plain segments concatenate with no styling`() {
            // ----- Arrange -----
            val segments = listOf(
                HeaderTextSegment.Plain(text = "Shelve "),
                HeaderTextSegment.Plain(text = "it"),
            )
            val colorScheme = colorScheme

            // ----- Act -----
            val result = headerAnnotatedString(
                segments = segments,
                colorScheme = colorScheme,
            )

            // ----- Assert -----
            result.text shouldBe "Shelve it"
            result.spanStyles shouldBe emptyList()
        }

        @Test
        fun `an emphasised run is coloured primary over exactly its own range`() {
            // ----- Arrange -----
            val segments = listOf(
                HeaderTextSegment.Plain(text = "Shelve "),
                HeaderTextSegment.Emphasised(text = "Piranesi"),
                HeaderTextSegment.Plain(text = " now"),
            )
            val colorScheme = colorScheme

            // ----- Act -----
            val result = headerAnnotatedString(
                segments = segments,
                colorScheme = colorScheme,
            )

            // ----- Assert -----
            result.text shouldBe "Shelve Piranesi now"
            val span = result.spanStyles.single()
            span.start shouldBe 7
            span.end shouldBe 15
            span.item.color shouldBe primary
        }

        @Test
        fun `two emphasised runs each get their own span`() {
            // ----- Arrange -----
            val segments = persistentListOf(
                HeaderTextSegment.Emphasised(text = "A"),
                HeaderTextSegment.Plain(text = "-"),
                HeaderTextSegment.Emphasised(text = "B"),
            )
            val colorScheme = colorScheme

            // ----- Act -----
            val result = headerAnnotatedString(
                segments = segments,
                colorScheme = colorScheme,
            )

            // ----- Assert -----
            result.spanStyles.map { it.start to it.end } shouldBe listOf(0 to 1, 2 to 3)
        }
    }
}
