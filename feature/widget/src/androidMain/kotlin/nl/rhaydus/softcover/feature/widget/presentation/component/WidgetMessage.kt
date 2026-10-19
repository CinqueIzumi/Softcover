package nl.rhaydus.softcover.feature.widget.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

@Composable
internal fun WidgetMessage(
    headline: String,
    subline: String?,
    header: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {},
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(Spacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        header()

        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .padding(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MessageText(
                headline = headline,
                subline = subline,
            )

            footer()
        }
    }
}

@Composable
internal fun MessageText(
    headline: String,
    subline: String?,
) {
    Text(
        text = headline,
        style = TextStyle(
            color = GlanceTheme.colors.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center,
        ),
    )

    if (subline != null) {
        Spacer(modifier = GlanceModifier.height(Spacing.xxs))

        Text(
            text = subline,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            ),
        )
    }
}
