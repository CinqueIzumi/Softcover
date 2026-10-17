package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.ui.graphics.Color
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class SoftcoverColorSchemeTest {
    @Nested
    inner class ContrastFloor {
        @TestFactory
        fun `every palette clears its contrast floor in every brightness`(): List<DynamicTest> =
            SchemeBrightness.entries.flatMap { brightness ->
                SpinePalette.entries.map { palette ->
                    dynamicTest(
                        "${palette.name} ${brightness.name} clears its contrast floor",
                    ) {
                        // ----- Arrange -----
                        val scheme = softcoverColorScheme(
                            brightness = brightness,
                            palette = palette,
                        )

                        // ----- Act & Assert -----
                        assertPairPasses(
                            palette = palette,
                            brightness = brightness,
                            foreground = scheme.onSurface,
                            foregroundName = "onSurface",
                            background = scheme.background,
                            backgroundName = "background",
                            minimumRatio = 4.5,
                        )
                        assertPairPasses(
                            palette = palette,
                            brightness = brightness,
                            foreground = scheme.onSurfaceVariant,
                            foregroundName = "onSurfaceVariant",
                            background = scheme.background,
                            backgroundName = "background",
                            minimumRatio = 4.5,
                        )
                        assertPairPasses(
                            palette = palette,
                            brightness = brightness,
                            foreground = scheme.primary,
                            foregroundName = "primary",
                            background = scheme.background,
                            backgroundName = "background",
                            minimumRatio = 4.5,
                        )
                        assertPairPasses(
                            palette = palette,
                            brightness = brightness,
                            foreground = scheme.tertiary,
                            foregroundName = "tertiary",
                            background = scheme.background,
                            backgroundName = "background",
                            minimumRatio = 4.5,
                        )
                        assertPairPasses(
                            palette = palette,
                            brightness = brightness,
                            foreground = scheme.onPrimary,
                            foregroundName = "onPrimary",
                            background = scheme.primary,
                            backgroundName = "primary",
                            minimumRatio = 4.5,
                        )
                        assertPairPasses(
                            palette = palette,
                            brightness = brightness,
                            foreground = scheme.onPrimaryContainer,
                            foregroundName = "onPrimaryContainer",
                            background = scheme.primaryContainer,
                            backgroundName = "primaryContainer",
                            minimumRatio = 4.5,
                        )
                        assertPairPasses(
                            palette = palette,
                            brightness = brightness,
                            foreground = scheme.onTertiaryContainer,
                            foregroundName = "onTertiaryContainer",
                            background = scheme.tertiaryContainer,
                            backgroundName = "tertiaryContainer",
                            minimumRatio = 4.5,
                        )
                        assertPairPasses(
                            palette = palette,
                            brightness = brightness,
                            foreground = scheme.onSurface,
                            foregroundName = "onSurface",
                            background = scheme.surfaceContainerHigh,
                            backgroundName = "surfaceContainerHigh",
                            minimumRatio = 4.5,
                        )
                        assertPairPasses(
                            palette = palette,
                            brightness = brightness,
                            foreground = scheme.outline,
                            foregroundName = "outline",
                            background = scheme.background,
                            backgroundName = "background",
                            minimumRatio = 3.0,
                        )
                        if (brightness == SchemeBrightness.DIM) {
                            assertPairPasses(
                                palette = palette,
                                brightness = brightness,
                                foreground = scheme.onSurface,
                                foregroundName = "onSurface",
                                background = scheme.background,
                                backgroundName = "background",
                                minimumRatio = 7.0,
                            )
                            assertPairPasses(
                                palette = palette,
                                brightness = brightness,
                                foreground = RatingGold,
                                foregroundName = "RatingGold",
                                background = scheme.background,
                                backgroundName = "background",
                                minimumRatio = 3.0,
                            )
                        }
                    }
                }
            }

        private fun assertPairPasses(
            palette: SpinePalette,
            brightness: SchemeBrightness,
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

            withClue(
                "expected ${palette.name} ${brightness.name} $foregroundName/$backgroundName contrast " +
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
                    brightness = SchemeBrightness.LIGHT,
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
                    brightness = SchemeBrightness.DARK,
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
        fun `dim mode paints the same scheme for every palette`() {
            // ----- Arrange -----
            val schemes = SpinePalette.entries.map {
                softcoverColorScheme(
                    brightness = SchemeBrightness.DIM,
                    palette = it,
                )
            }

            // ----- Act -----
            val distinctSchemes = schemes.toSet()

            // ----- Assert -----
            distinctSchemes shouldHaveSize 1
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
