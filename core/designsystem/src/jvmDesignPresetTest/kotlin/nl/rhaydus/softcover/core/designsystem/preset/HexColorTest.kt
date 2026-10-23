package nl.rhaydus.softcover.core.designsystem.preset

import androidx.compose.ui.graphics.Color
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HexColorTest {
    @Nested
    inner class ToHex {
        @Test
        fun `renders an opaque colour as six lowercase digits`() {
            // ----- Arrange -----
            val color = Color(0xFFABCDEF)

            // ----- Act -----
            val result = color.toHex()

            // ----- Assert -----
            result shouldBe "#abcdef"
        }

        @Test
        fun `appends the alpha byte when the colour is translucent`() {
            // ----- Arrange -----
            val color = Color(0xE6112233)

            // ----- Act -----
            val result = color.toHex()

            // ----- Assert -----
            result shouldBe "#112233e6"
        }

        @Test
        fun `zero pads small channel values`() {
            // ----- Arrange -----
            val color = Color(0xFF000A0B)

            // ----- Act -----
            val result = color.toHex()

            // ----- Assert -----
            result shouldBe "#000a0b"
        }
    }
}
