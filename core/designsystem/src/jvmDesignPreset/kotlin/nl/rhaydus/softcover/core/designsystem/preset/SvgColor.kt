package nl.rhaydus.softcover.core.designsystem.preset

import java.io.File
import java.util.Locale

internal const val CURRENT_COLOR = "currentColor"

private const val OPAQUE = 255f
private const val ARGB_LENGTH = 9
private const val RGB_LENGTH = 7
private const val SHORT_ARGB_LENGTH = 5
private const val SHORT_RGB_LENGTH = 4

/** An Android colour resolved for SVG: a paint (`#rrggbb`, `currentColor`) and its own opacity. */
internal data class SvgColor(
    val paint: String,
    val opacity: Float,
)

/**
 * Resolves an Android vector colour attribute. A `?attr/...` reference, and a `@color/...` selector that
 * ends in one, are theme-tinted and become `currentColor`. When [monochrome] every visible colour does,
 * because an icon is tinted at its call site. Returns `null` for a fully transparent colour.
 */
internal fun resolveSvgColor(
    value: String,
    monochrome: Boolean,
    colorDir: File,
): SvgColor? = when {
    value.startsWith("?attr/") -> SvgColor(
        CURRENT_COLOR,
        1f,
    )
    value.startsWith("@color/") -> resolveSvgColor(
        selectorColor(
            colorDir,
            value.removePrefix("@color/"),
        ),
        monochrome,
        colorDir,
    )
    value.startsWith("#") -> hexColor(value)?.let { if (monochrome) it.copy(paint = CURRENT_COLOR) else it }
    else -> error("Unsupported vector colour: $value")
}

private fun selectorColor(
    colorDir: File,
    name: String,
): String {
    val file = File(
        colorDir,
        "$name.xml",
    )
    check(file.isFile) { "No colour resource for @color/$name in $colorDir" }
    val items = Regex("android:color=\"([^\"]+)\"").findAll(file.readText()).map { it.groupValues[1] }.toList()

    return items.singleOrNull() ?: error("@color/$name must hold exactly one item to convert; found ${items.size}")
}

private fun hexColor(value: String): SvgColor? {
    val digits = value.removePrefix("#")
    check(digits.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) { "Malformed colour: $value" }
    val expanded = if (value.length == SHORT_RGB_LENGTH || value.length == SHORT_ARGB_LENGTH) {
        digits.map { "$it$it" }.joinToString("")
    } else {
        digits
    }
    val argb = when (value.length) {
        RGB_LENGTH, SHORT_RGB_LENGTH -> "ff$expanded"
        ARGB_LENGTH, SHORT_ARGB_LENGTH -> expanded
        else -> error("Malformed colour: $value")
    }.lowercase(Locale.ROOT)
    val alpha = argb.take(2).toInt(radix = 16) / OPAQUE

    return if (alpha == 0f) null else SvgColor(
        "#" + argb.drop(2),
        alpha,
    )
}
