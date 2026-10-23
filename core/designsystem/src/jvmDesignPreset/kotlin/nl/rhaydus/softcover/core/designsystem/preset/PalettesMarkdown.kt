package nl.rhaydus.softcover.core.designsystem.preset

import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.core.designsystem.presentation.theme.softcoverColorScheme

/** `Palettes.md`: the alternate palettes' role tables, which the token file's three themes leave out. */
internal object PalettesMarkdown {
    private val alternates = SpinePalette.entries.filter { it != SpinePalette.DEFAULT }

    fun render(): String = buildString {
        append("# Palettes\n\n")
        append(
            "Softcover ships ${SpinePalette.entries.size} palettes. ${SpinePalette.DEFAULT.label} is the default " +
                "and is the one in the token file. The other ${alternates.size} are listed here. Each retints " +
                "the page as well as the accents. Dim ignores the palette, so these tables cover light and dark.\n",
        )

        alternates.forEach { palette ->
            val light = rolesFor(
                SchemeBrightness.LIGHT,
                palette,
            )
            val dark = rolesFor(
                SchemeBrightness.DARK,
                palette,
            )

            append("\n## ${palette.label}\n\n${palette.gloss}\n\n")
            append("| Role | Light | Dark |\n| --- | --- | --- |\n")
            light.forEach { (role, color) ->
                append("| `$role` | `${color.toHex()}` | `${dark.getValue(role).toHex()}` |\n")
            }
        }
    }

    private fun rolesFor(
        brightness: SchemeBrightness,
        palette: SpinePalette,
    ) = ColorTokens.schemeRoles(softcoverColorScheme(
        brightness,
        palette,
    ),)
}
