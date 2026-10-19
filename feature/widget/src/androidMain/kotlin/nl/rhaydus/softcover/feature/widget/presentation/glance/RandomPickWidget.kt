package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.state.PreferencesGlanceStateDefinition
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class RandomPickWidget : GlanceAppWidget(), KoinComponent {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideSharedContent(
            source = get<RandomPickWidgetModelSource>(),
            appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id),
            liveSessions = get(),
        )
    }
}
