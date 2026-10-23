package nl.rhaydus.softcover.core.designsystem.preset

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TokensCssTest {
    private val baseSelector = ".sc-theme, :root {"
    private val dimSelector = ".sc-theme[data-theme=\"dim\"], :root[data-theme=\"dim\"] {"
    private val darkSelector = ".sc-theme[data-theme=\"dark\"], :root[data-theme=\"dark\"] {"

    private fun document(
        colors: List<Map<String, Any?>> = emptyList(),
        styles: List<Map<String, Any?>> = emptyList(),
        fonts: List<Map<String, Any?>> = emptyList(),
    ): Map<String, Any?> = mapOf(
        "color" to mapOf(
            "themes" to listOf("light", "dim", "dark").map { mapOf("id" to it) },
            "tokens" to colors,
        ),
        "spacing" to mapOf("tokens" to emptyList<Any>()),
        "radius" to mapOf("tokens" to emptyList<Any>()),
        "type" to mapOf(
            "families" to mapOf(
                "display" to "DisplayFace",
                "body" to "BodyFace",
            ),
            "fonts" to fonts,
            "groups" to listOf(mapOf("styles" to styles)),
        ),
    )

    private fun color(
        name: String,
        value: Any,
    ): Map<String, Any?> = mapOf(
        "name" to name,
        "value" to value,
    )

    private fun style(
        name: String,
        family: String? = null,
        fontStyle: String? = null,
        fontFeatureSettings: String? = null,
    ): Map<String, Any?> = mapOf(
        "name" to name,
        "family" to family,
        "fontStyle" to fontStyle,
        "fontFeatureSettings" to fontFeatureSettings,
        "fontSize" to "1px",
        "lineHeight" to "1px",
        "fontWeight" to 400,
        "letterSpacing" to "0px",
    )

    private fun String.blockOf(selectorLine: String): String {
        val start = indexOf(selectorLine)
        return substring(
            start,
            indexOf(
                "}",
                start,
            ),
        )
    }

    @Nested
    inner class ThemeColors {
        @Test
        fun `writes a per-theme colour into the base block and each override block`() {
            // ----- Arrange -----
            val document = document(
                colors = listOf(
                    color(
                        "paper",
                        mapOf(
                            "light" to "#111111",
                            "dim" to "#222222",
                            "dark" to "#333333",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val css = TokensCss.render(document)

            // ----- Assert -----
            css.blockOf(baseSelector) shouldContain "--paper: #111111;"
            css.blockOf(dimSelector) shouldContain "--paper: #222222;"
            css.blockOf(darkSelector) shouldContain "--paper: #333333;"
        }

        @Test
        fun `keeps a colour that is the same in every theme out of the override blocks`() {
            // ----- Arrange -----
            val document = document(
                colors = listOf(
                    color(
                        "ink",
                        mapOf(
                            "light" to "#000000",
                            "dim" to "#000000",
                            "dark" to "#000000",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val css = TokensCss.render(document)

            // ----- Assert -----
            css.blockOf(baseSelector) shouldContain "--ink: #000000;"
            css.blockOf(dimSelector) shouldNotContain "--ink"
            css.blockOf(darkSelector) shouldNotContain "--ink"
        }

        @Test
        fun `keeps a plain string colour out of the override blocks`() {
            // ----- Arrange -----
            val fixed = color(
                "fixed",
                "#abcdef",
            )
            val document = document(colors = listOf(fixed))

            // ----- Act -----
            val css = TokensCss.render(document)

            // ----- Assert -----
            css.blockOf(baseSelector) shouldContain "--fixed: #abcdef;"
            css.blockOf(dimSelector) shouldNotContain "--fixed"
            css.blockOf(darkSelector) shouldNotContain "--fixed"
        }

        @Test
        fun `overrides only the theme whose value differs from light`() {
            // ----- Arrange -----
            val document = document(
                colors = listOf(
                    color(
                        "accent",
                        mapOf(
                            "light" to "#aaaaaa",
                            "dim" to "#aaaaaa",
                            "dark" to "#bbbbbb",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val css = TokensCss.render(document)

            // ----- Assert -----
            css.blockOf(dimSelector) shouldNotContain "--accent"
            css.blockOf(darkSelector) shouldContain "--accent: #bbbbbb;"
        }
    }

    @Nested
    inner class TypeStyles {
        @Test
        fun `renders a style without a family in the display family`() {
            // ----- Arrange -----
            val document = document(styles = listOf(style("headline")))

            // ----- Act -----
            val css = TokensCss.render(document)

            // ----- Assert -----
            css.blockOf(".headline {") shouldContain "font-family: DisplayFace;"
        }

        @Test
        fun `renders a body-family style in the body family`() {
            // ----- Arrange -----
            val prose = style(
                "prose",
                family = "body",
            )
            val document = document(styles = listOf(prose))

            // ----- Act -----
            val css = TokensCss.render(document)

            // ----- Assert -----
            css.blockOf(".prose {") shouldContain "font-family: BodyFace;"
        }

        @Test
        fun `adds font-style to an italic style only`() {
            // ----- Arrange -----
            val document = document(
                styles = listOf(
                    style(
                        "slanted",
                        fontStyle = "italic",
                    ),
                    style("upright"),
                ),
            )

            // ----- Act -----
            val css = TokensCss.render(document)

            // ----- Assert -----
            css.blockOf(".slanted {") shouldContain "font-style: italic;"
            css.blockOf(".upright {") shouldNotContain "font-style"
        }
    }

    @Nested
    inner class FontFaces {
        @Test
        fun `renders a font file as a font-face with its family, weight, style and relative source`() {
            // ----- Arrange -----
            val document = document(
                fonts = listOf(
                    mapOf(
                        "family" to "Inkwell",
                        "file" to "fonts/Inkwell-Italic.ttf",
                        "weight" to 700,
                        "style" to "italic",
                    ),
                ),
            )

            // ----- Act -----
            val css = TokensCss.render(document)

            // ----- Assert -----
            val face = css.blockOf("@font-face {")
            face shouldContain "font-family: \"Inkwell\";"
            face shouldContain "src: url(\"../fonts/Inkwell-Italic.ttf\");"
            face shouldContain "font-weight: 700;"
            face shouldContain "font-style: italic;"
        }

        @Test
        fun `escapes quotes and backslashes in a family name`() {
            // ----- Arrange -----
            val document = document(
                fonts = listOf(
                    mapOf(
                        "family" to "Odd\"Name\\X",
                        "file" to "fonts/odd.ttf",
                        "weight" to 400,
                        "style" to "normal",
                    ),
                ),
            )

            // ----- Act -----
            val css = TokensCss.render(document)

            // ----- Assert -----
            css.blockOf("@font-face {") shouldContain "font-family: \"Odd\\\"Name\\\\X\";"
        }
    }

    @Nested
    inner class FontFeatures {
        @Test
        fun `emits font-feature-settings only for a style that carries them`() {
            // ----- Arrange -----
            val document = document(
                styles = listOf(
                    style(
                        "numeric",
                        fontFeatureSettings = "\"tnum\"",
                    ),
                    style("plain"),
                ),
            )

            // ----- Act -----
            val css = TokensCss.render(document)

            // ----- Assert -----
            css.blockOf(".numeric {") shouldContain "font-feature-settings: \"tnum\";"
            css.blockOf(".plain {") shouldNotContain "font-feature-settings"
        }
    }

    @Nested
    inner class Validation {
        @Test
        fun `fails naming a colour token that has no value`() {
            // ----- Arrange -----
            val document = document(colors = listOf(mapOf("name" to "ghost")))

            // ----- Act -----
            val error = shouldThrow<IllegalArgumentException> { TokensCss.render(document) }

            // ----- Assert -----
            error.message shouldContain "ghost"
        }

        @Test
        fun `fails naming a colour token that lacks a theme value`() {
            // ----- Arrange -----
            val document = document(
                colors = listOf(
                    color(
                        "partial",
                        mapOf(
                            "light" to "#111111",
                            "dim" to "#222222",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val error = shouldThrow<IllegalArgumentException> { TokensCss.render(document) }

            // ----- Assert -----
            error.message shouldContain "partial"
        }
    }
}
