package nl.rhaydus.softcover.feature.widget.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.padding
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

@Composable
internal fun ReservedLinesTitle(
    title: String,
    reservedLines: Int,
) {
    val style = TextStyle(
        color = GlanceTheme.colors.onSurface,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Serif,
        textAlign = TextAlign.Center,
    )

    Box(
        modifier = GlanceModifier.padding(horizontal = Spacing.xs),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Glance has no minLines: this invisible text holds the box at `reservedLines` lines, at any font scale.
        Text(
            text = List(reservedLines) { " " }.joinToString(separator = "\n"),
            maxLines = reservedLines,
            style = style.copy(color = ColorProvider(Color.Transparent)),
        )

        Text(
            text = title,
            maxLines = 2,
            style = style,
        )
    }
}
