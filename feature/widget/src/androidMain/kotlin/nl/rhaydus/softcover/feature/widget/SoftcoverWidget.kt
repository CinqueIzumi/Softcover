package nl.rhaydus.softcover.feature.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.glance.text.TextStyle
import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.theme.SoftcoverWidgetTheme
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class SoftcoverWidget : GlanceAppWidget(), KoinComponent {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val dependencies = get<SoftcoverWidgetDependencies>()

        val themeConfigurations = dependencies.getThemeConfiguration()
        val initialThemeConfiguration = themeConfigurations.firstOrNull() ?: ThemeConfiguration()
        val label = context.getString(R.string.widget_label)

        provideContent {
            val themeConfiguration by themeConfigurations.collectAsState(initial = initialThemeConfiguration)

            SoftcoverWidgetContent(
                label = label,
                themeConfiguration = themeConfiguration,
            )
        }
    }
}

@Composable
private fun SoftcoverWidgetContent(
    label: String,
    themeConfiguration: ThemeConfiguration,
) {
    SoftcoverWidgetTheme(themeConfiguration = themeConfiguration) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(Spacing.md),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = TextStyle(color = GlanceTheme.colors.onBackground),
            )
        }
    }
}
