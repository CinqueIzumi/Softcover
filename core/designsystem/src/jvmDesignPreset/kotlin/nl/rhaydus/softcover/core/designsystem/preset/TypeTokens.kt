package nl.rhaydus.softcover.core.designsystem.preset

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.isUnspecified
import nl.rhaydus.softcover.core.designsystem.presentation.theme.EditorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.buildAppTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.buildEditorialTypography
import java.io.File

private const val DISPLAY = "display"
private const val BODY = "body"

// Material's emphasized variants are defaults the app never sets, so they carry no Softcover family.
private const val EMPHASIZED_SUFFIX = "-emphasized"

// Which bundled font is the display face is decided in Type.kt by composition; no source the exporter can read.
private const val DISPLAY_FILE_PREFIX = "fraunces"
private const val BODY_FILE_PREFIX = "inter"
private const val REGULAR_WEIGHT = 400
private const val TABULAR_FIGURES = "tnum"

/**
 * The `type` section. The real font families need Compose font resources, so the builders run with two
 * generic sentinels and each style's family is read back off them.
 */
internal object TypeTokens {
    private val displaySentinel = FontFamily.Serif
    private val bodySentinel = FontFamily.SansSerif

    fun type(fontsDir: File): Map<String, Any?> {
        val displayName = FontFiles.familyOf(
            fontsDir,
            DISPLAY_FILE_PREFIX,
        )
        val bodyName = FontFiles.familyOf(
            fontsDir,
            BODY_FILE_PREFIX,
        )

        return linkedMapOf(
            "fonts" to FontFiles.entries(fontsDir),
            "families" to linkedMapOf(
                DISPLAY to "\"$displayName\", Georgia, serif",
                BODY to "\"$bodyName\", system-ui, sans-serif",
            ),
            "groups" to listOf(
                group(
                    name = "Editorial",
                    family = DISPLAY,
                    styles = editorialStyles(),
                ),
                group(
                    name = "Material",
                    family = BODY,
                    styles = materialStyles(),
                ),
            ),
        )
    }

    private fun group(
        name: String,
        family: String,
        styles: List<Map<String, Any?>>,
    ): Map<String, Any?> = linkedMapOf("name" to name, "family" to family, "styles" to styles)

    private fun editorialStyles(): List<Map<String, Any?>> {
        val roles = readStyles(
            EditorialTypography::class.java,
            buildEditorialTypography(
                typography = buildAppTypography(
                    display = displaySentinel,
                    body = bodySentinel,
                ),
                displayFamily = displaySentinel,
            ),
            prefix = "editorial-",
        )

        return styles(
            roles,
            TypeUsage.editorial,
            groupFamily = displaySentinel,
        )
    }

    private fun materialStyles(): List<Map<String, Any?>> {
        val roles = readStyles(
            Typography::class.java,
            buildAppTypography(
                display = displaySentinel,
                body = bodySentinel,
            ),
            prefix = "",
        )

        return styles(
            roles,
            TypeUsage.material,
            groupFamily = bodySentinel,
        )
    }

    private fun readStyles(
        type: Class<*>,
        receiver: Any,
        prefix: String,
    ): Map<String, TextStyle> = readPublicGetters(
        type,
        receiver,
        TextStyle::class.java,
    ) { it as TextStyle }
        .filterKeys { it.endsWith(EMPHASIZED_SUFFIX).not() }
        .mapKeys { (name, _) -> prefix + name }

    private fun styles(
        roles: Map<String, TextStyle>,
        usage: Map<String, String>,
        groupFamily: FontFamily,
    ): List<Map<String, Any?>> = orderedByUsage(
        roles,
        usage.keys,
        source = "Type",
    ).map { (name, style) ->
        style(
            name,
            style,
            usage.getValue(name),
            groupFamily,
        )
    }

    private fun style(
        name: String,
        style: TextStyle,
        usage: String,
        groupFamily: FontFamily,
    ): Map<String, Any?> {
        check(style.fontSize.isSp && style.lineHeight.isSp) { "$name must size and space its lines in sp." }
        check(style.letterSpacing.isSp || style.letterSpacing.isUnspecified) { "$name must space its letters in sp." }
        val features = style.fontFeatureSettings
        check(features == null || features == TABULAR_FIGURES) { "$name uses unsupported font features: $features" }

        val entry = linkedMapOf<String, Any?>("name" to name)
        if (style.fontFamily != groupFamily) entry["family"] = familyKey(
            name,
            style.fontFamily,
        )
        entry["fontSize"] = style.fontSize.value.toPx()
        entry["lineHeight"] = style.lineHeight.value.toPx()
        entry["fontWeight"] = style.fontWeight?.weight ?: REGULAR_WEIGHT
        if (style.fontStyle == FontStyle.Italic) entry["fontStyle"] = "italic"
        entry["letterSpacing"] = (if (style.letterSpacing.isSp) style.letterSpacing.value else 0f).toPx()
        entry["usage"] = if (features == null) usage else "$usage Uses tabular figures (tnum)."

        return entry
    }

    private fun familyKey(
        name: String,
        family: FontFamily?,
    ): String = when (family) {
        displaySentinel -> DISPLAY
        bodySentinel -> BODY
        else -> error("$name has an unexpected font family: $family")
    }
}
