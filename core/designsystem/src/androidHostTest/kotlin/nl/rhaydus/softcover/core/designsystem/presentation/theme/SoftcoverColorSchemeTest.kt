package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.ui.graphics.Color
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class SoftcoverColorSchemeTest {
    @Nested
    inner class HouseLookIsUnchanged {
        @Test
        fun `SOFTCOVER light scheme keeps its shipped hexes`() {
            // ----- Arrange -----
            val palette = SpinePalette.SOFTCOVER

            // ----- Act -----
            val scheme = softcoverColorScheme(
                darkTheme = false,
                palette = palette,
            )

            // ----- Assert -----
            scheme.primary shouldBe Color(0xFF8F4C38)
            scheme.primaryContainer shouldBe Color(0xFFFFDBD1)
            scheme.tertiary shouldBe Color(0xFF6C5D2F)
            scheme.background shouldBe Color(0xFFFFF8F6)
            scheme.onSurface shouldBe Color(0xFF231917)
            scheme.surfaceContainerHigh shouldBe Color(0xFFF7E4E0)
            scheme.outlineVariant shouldBe Color(0xFFD8C2BC)
            scheme.inversePrimary shouldBe Color(0xFFFFB5A0)
        }

        @Test
        fun `SOFTCOVER dark scheme keeps its shipped hexes`() {
            // ----- Arrange -----
            val palette = SpinePalette.SOFTCOVER

            // ----- Act -----
            val scheme = softcoverColorScheme(
                darkTheme = true,
                palette = palette,
            )

            // ----- Assert -----
            scheme.primary shouldBe Color(0xFFFFB5A0)
            scheme.onPrimary shouldBe Color(0xFF561F0F)
            scheme.background shouldBe Color(0xFF1A110F)
            scheme.onSurface shouldBe Color(0xFFF1DFDA)
            scheme.surfaceContainerHigh shouldBe Color(0xFF322825)
            scheme.outline shouldBe Color(0xFFA08C87)
        }
    }

    @Nested
    inner class ContrastFloor {
        @TestFactory
        fun `every palette clears its contrast floor in both brightnesses`(): List<DynamicTest> =
            SpinePalette.entries.flatMap { palette ->
                listOf(false, true).map { darkTheme ->
                    dynamicTest(
                        "${palette.name} ${if (darkTheme) "dark" else "light"} clears its contrast floor",
                    ) {
                        val scheme = softcoverColorScheme(
                            darkTheme = darkTheme,
                            palette = palette,
                        )

                        assertPairPasses(
                            palette = palette,
                            darkTheme = darkTheme,
                            foreground = scheme.onSurface,
                            foregroundName = "onSurface",
                            background = scheme.background,
                            backgroundName = "background",
                            minimumRatio = 4.5,
                        )
                        assertPairPasses(
                            palette = palette,
                            darkTheme = darkTheme,
                            foreground = scheme.onSurfaceVariant,
                            foregroundName = "onSurfaceVariant",
                            background = scheme.background,
                            backgroundName = "background",
                            minimumRatio = 4.5,
                        )
                        assertPairPasses(
                            palette = palette,
                            darkTheme = darkTheme,
                            foreground = scheme.primary,
                            foregroundName = "primary",
                            background = scheme.background,
                            backgroundName = "background",
                            minimumRatio = 4.5,
                        )
                        assertPairPasses(
                            palette = palette,
                            darkTheme = darkTheme,
                            foreground = scheme.tertiary,
                            foregroundName = "tertiary",
                            background = scheme.background,
                            backgroundName = "background",
                            minimumRatio = 4.5,
                        )
                        assertPairPasses(
                            palette = palette,
                            darkTheme = darkTheme,
                            foreground = scheme.onPrimary,
                            foregroundName = "onPrimary",
                            background = scheme.primary,
                            backgroundName = "primary",
                            minimumRatio = 4.5,
                        )
                        assertPairPasses(
                            palette = palette,
                            darkTheme = darkTheme,
                            foreground = scheme.onPrimaryContainer,
                            foregroundName = "onPrimaryContainer",
                            background = scheme.primaryContainer,
                            backgroundName = "primaryContainer",
                            minimumRatio = 4.5,
                        )
                        assertPairPasses(
                            palette = palette,
                            darkTheme = darkTheme,
                            foreground = scheme.onTertiaryContainer,
                            foregroundName = "onTertiaryContainer",
                            background = scheme.tertiaryContainer,
                            backgroundName = "tertiaryContainer",
                            minimumRatio = 4.5,
                        )
                        assertPairPasses(
                            palette = palette,
                            darkTheme = darkTheme,
                            foreground = scheme.onSurface,
                            foregroundName = "onSurface",
                            background = scheme.surfaceContainerHigh,
                            backgroundName = "surfaceContainerHigh",
                            minimumRatio = 4.5,
                        )
                        assertPairPasses(
                            palette = palette,
                            darkTheme = darkTheme,
                            foreground = scheme.outline,
                            foregroundName = "outline",
                            background = scheme.background,
                            backgroundName = "background",
                            minimumRatio = 3.0,
                        )
                    }
                }
            }

        private fun assertPairPasses(
            palette: SpinePalette,
            darkTheme: Boolean,
            foreground: Color,
            foregroundName: String,
            background: Color,
            backgroundName: String,
            minimumRatio: Double,
        ) {
            val ratio = contrastRatio(
                a = foreground,
                b = background,
            )
            val brightness = if (darkTheme) "dark" else "light"

            withClue(
                "expected ${palette.name} $brightness $foregroundName/$backgroundName contrast " +
                    "to be at least $minimumRatio, but was $ratio",
            ) {
                ratio shouldBeGreaterThanOrEqual minimumRatio
            }
        }
    }

    @Nested
    inner class PalettesAreDistinctLooks {
        @Test
        fun `light mode primary and background differ across all five palettes`() {
            // ----- Arrange -----
            val schemes = SpinePalette.entries.map {
                softcoverColorScheme(
                    darkTheme = false,
                    palette = it,
                )
            }

            // ----- Act -----
            val primaries = schemes.map { it.primary }.toSet()
            val backgrounds = schemes.map { it.background }.toSet()

            // ----- Assert -----
            primaries shouldHaveSize 5
            backgrounds shouldHaveSize 5
        }

        @Test
        fun `dark mode primary and background differ across all five palettes`() {
            // ----- Arrange -----
            val schemes = SpinePalette.entries.map {
                softcoverColorScheme(
                    darkTheme = true,
                    palette = it,
                )
            }

            // ----- Act -----
            val primaries = schemes.map { it.primary }.toSet()
            val backgrounds = schemes.map { it.background }.toSet()

            // ----- Assert -----
            primaries shouldHaveSize 5
            backgrounds shouldHaveSize 5
        }
    }
}

/**
 * WCAG 2.x relative luminance for one sRGB channel, per the standard's piecewise gamma correction.
 */
private fun linearizeChannel(channel: Float): Double {
    val c = channel.toDouble()
    return if (c <= 0.03928) c / 12.92 else Math.pow(
        (c + 0.055) / 1.055,
        2.4,
    )
}

/** WCAG relative luminance of [color], in the 0..1 range. */
private fun relativeLuminance(color: Color): Double {
    val r = linearizeChannel(color.red)
    val g = linearizeChannel(color.green)
    val b = linearizeChannel(color.blue)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

/** WCAG contrast ratio between [a] and [b], always >= 1.0 regardless of argument order. */
private fun contrastRatio(
    a: Color,
    b: Color,
): Double {
    val luminanceA = relativeLuminance(a)
    val luminanceB = relativeLuminance(b)
    val higher = maxOf(
        luminanceA,
        luminanceB,
    )
    val lower = minOf(
        luminanceA,
        luminanceB,
    )
    return (higher + 0.05) / (lower + 0.05)
}
