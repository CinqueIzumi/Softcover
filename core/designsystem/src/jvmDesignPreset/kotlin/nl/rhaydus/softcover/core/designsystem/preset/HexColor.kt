package nl.rhaydus.softcover.core.designsystem.preset

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.util.Locale

private const val OPAQUE = 0xFF

/** Lowercase `#rrggbb`, or `#rrggbbaa` when the colour is not fully opaque. */
internal fun Color.toHex(): String {
    val argb = toArgb()
    val alpha = (argb ushr 24) and OPAQUE
    val rgb = String.format(
        Locale.ROOT,
        "#%06x",
        argb and 0xFFFFFF,
    )

    return if (alpha == OPAQUE) rgb else rgb + String.format(
        Locale.ROOT,
        "%02x",
        alpha,
    )
}
