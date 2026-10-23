package nl.rhaydus.softcover.core.designsystem.preset

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.core.designsystem.presentation.theme.softcoverColorScheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.spoilerCover
import nl.rhaydus.softcover.core.designsystem.presentation.theme.spoilerEditorHighlight

private const val THEME_PACKAGE = "nl.rhaydus.softcover.core.designsystem.presentation.theme"

/** The `color.tokens` entries: scheme roles per theme, the fixed inks, and the spoiler treatment. */
internal object ColorTokens {
    fun tokens(): List<Map<String, Any?>> = schemeTokens() + fixedInkTokens() + spoilerTokens()

    /** Every role of [scheme] in the documented order. */
    fun schemeRoles(scheme: ColorScheme) = orderedByUsage(
        found = readColors(
            ColorScheme::class.java,
            scheme,
        ),
        documented = ColorUsage.schemeRoles.keys,
        source = "ColorScheme",
    )

    private fun softcoverScheme(theme: PresetTheme): ColorScheme =
        softcoverColorScheme(
            theme.brightness,
            SpinePalette.SOFTCOVER,
        )

    private fun schemeTokens(): List<Map<String, Any?>> {
        val byTheme = PresetTheme.entries.associateWith { schemeRoles(softcoverScheme(it)) }

        return ColorUsage.schemeRoles.map { (name, usage) ->
            val values = byTheme.entries.associate { (theme, roles) -> theme.id to roles.getValue(name).toHex() }

            token(
                name = name,
                value = values,
                usage = usage,
            )
        }
    }

    private val fixedInks: Map<String, Color> = readPublicTopLevelColors(Class.forName("$THEME_PACKAGE.ColorKt"))

    private fun fixedInkTokens(): List<Map<String, Any?>> {
        val inks = orderedByUsage(
            fixedInks,
            ColorUsage.fixedInks.keys,
            source = "Color.kt fixed ink",
        )

        return inks.map { (name, color) ->
            token(
                name = name,
                value = color.toHex(),
                usage = ColorUsage.fixedInks.getValue(name),
            )
        }
    }

    private fun spoilerTokens(): List<Map<String, Any?>> {
        val schemes = PresetTheme.entries.associateWith { softcoverScheme(it) }
        val covers = schemes.entries.associate { (theme, scheme) -> theme.id to scheme.spoilerCover.toHex() }
        val highlights = schemes.entries.associate { (theme, scheme) -> theme.id to scheme.spoilerEditorHighlight.toHex() }

        return listOf(
            token(
                name = "spoiler-cover",
                value = covers,
                usage = ColorUsage.spoilerCover,
            ),
            token(
                name = "spoiler-editor-highlight",
                value = highlights,
                usage = ColorUsage.spoilerEditorHighlight,
            ),
        )
    }

    private fun token(
        name: String,
        value: Any,
        usage: String,
    ): Map<String, Any?> = linkedMapOf("name" to name, "value" to value, "usage" to usage)
}
