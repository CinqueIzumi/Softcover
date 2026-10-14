package nl.rhaydus.softcover.feature.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

internal class SoftcoverWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val label = context.getString(R.string.widget_label)

        provideContent { SoftcoverWidgetContent(label = label) }
    }
}

@Composable
private fun SoftcoverWidgetContent(label: String) {
    GlanceTheme {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .padding(Spacing.md),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = label)
        }
    }
}
