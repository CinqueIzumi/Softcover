package nl.rhaydus.softcover.core.designsystem.preset

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

class SvgColorTest {
    private val colorDir = File("unused")

    @Nested
    inner class ResolveSvgColor {
        @Test
        fun `maps a theme attribute reference to currentColor`() {
            // ----- Arrange -----
            val value = "?attr/colorControlNormal"

            // ----- Act -----
            val result = resolveSvgColor(
                value,
                monochrome = false,
                colorDir = colorDir,
            )

            // ----- Assert -----
            result shouldBe SvgColor(
                paint = "currentColor",
                opacity = 1f,
            )
        }

        @Test
        fun `drops a fully transparent colour`() {
            // ----- Arrange -----
            val value = "#00000000"

            // ----- Act -----
            val result = resolveSvgColor(
                value,
                monochrome = false,
                colorDir = colorDir,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `expands a three digit colour to six digits`() {
            // ----- Arrange -----
            val value = "#F0a"

            // ----- Act -----
            val result = resolveSvgColor(
                value,
                monochrome = false,
                colorDir = colorDir,
            )

            // ----- Assert -----
            result shouldBe SvgColor(
                paint = "#ff00aa",
                opacity = 1f,
            )
        }

        @Test
        fun `splits an eight digit colour into paint and opacity`() {
            // ----- Arrange -----
            val value = "#80FF0000"

            // ----- Act -----
            val result = resolveSvgColor(
                value,
                monochrome = false,
                colorDir = colorDir,
            )

            // ----- Assert -----
            result.shouldNotBeNull()
            result.paint shouldBe "#ff0000"
            result.opacity shouldBe (128 / 255f)
        }

        @Test
        fun `turns a visible hex colour into currentColor when monochrome`() {
            // ----- Arrange -----
            val value = "#112233"

            // ----- Act -----
            val result = resolveSvgColor(
                value,
                monochrome = true,
                colorDir = colorDir,
            )

            // ----- Assert -----
            result shouldBe SvgColor(
                paint = "currentColor",
                opacity = 1f,
            )
        }

        @Test
        fun `rejects a colour of an unsupported length`() {
            // ----- Arrange -----
            val value = "#12345"

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> {
                resolveSvgColor(
                    value,
                    monochrome = false,
                    colorDir = colorDir,
                )
            }
        }
    }
}
