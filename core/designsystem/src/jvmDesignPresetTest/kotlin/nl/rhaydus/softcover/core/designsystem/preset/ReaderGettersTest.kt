package nl.rhaydus.softcover.core.designsystem.preset

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.lang.Float as JavaFloat

class ReaderGettersTest {
    @Nested
    inner class ReadPublicGetters {
        @Test
        fun `reads every Dp member of an object under its kebab-case name`() {
            // ----- Arrange -----
            val receiver = FixtureSpacing

            // ----- Act -----
            val result = readPublicGetters(
                FixtureSpacing::class.java,
                receiver,
                JavaFloat.TYPE,
            ) { it as Float }

            // ----- Assert -----
            result shouldBe mapOf("xs" to 4f, "section-gap" to 24f)
        }

        @Test
        fun `reads every TextStyle property of a data class and ignores other types`() {
            // ----- Arrange -----
            val receiver = FixtureTypography()

            // ----- Act -----
            val result = readPublicGetters(
                FixtureTypography::class.java,
                receiver,
                TextStyle::class.java,
            ) { it as TextStyle }

            // ----- Assert -----
            result.keys shouldBe setOf("body-large")
        }
    }

    @Nested
    @Suppress("NoReflection")
    inner class ReadPublicTopLevelColors {
        @Test
        fun `keeps public top-level colours and drops internal and private ones`() {
            // ----- Arrange -----
            val fileClass = Class.forName("nl.rhaydus.softcover.core.designsystem.preset.ReaderFixturesKt")

            // ----- Act -----
            val result = readPublicTopLevelColors(fileClass)

            // ----- Assert -----
            result shouldBe mapOf("fixture-ink-public" to Color(0xFF112233))
        }
    }
}
