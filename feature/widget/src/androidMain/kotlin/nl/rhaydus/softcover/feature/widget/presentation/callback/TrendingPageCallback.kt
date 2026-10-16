package nl.rhaydus.softcover.feature.widget.presentation.callback

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.softcover.feature.widget.data.mapper.toTrendingPage
import nl.rhaydus.softcover.feature.widget.data.mapper.toTrendingPeriod
import nl.rhaydus.softcover.feature.widget.data.mapper.writeTrendingPage
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidgetDependencies
import nl.rhaydus.softcover.feature.widget.presentation.util.nextPage
import nl.rhaydus.softcover.feature.widget.presentation.util.pageCount
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class TrendingPageCallback : ActionCallback, KoinComponent {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val step = parameters[StepKey] ?: return
        val dependencies = get<TrendingWidgetDependencies>()

        val period = getAppWidgetState(
            context = context,
            definition = PreferencesGlanceStateDefinition,
            glanceId = glanceId,
        ).toTrendingPeriod()

        val books = dependencies.observeTrendingBooks(period = period).firstOrNull() ?: return
        val pages = pageCount(books.size)

        updateAppWidgetState(
            context = context,
            glanceId = glanceId,
        ) { preferences ->
            preferences.writeTrendingPage(
                nextPage(
                    stored = preferences.toTrendingPage(),
                    pageCount = pages,
                    step = step,
                ),
            )
        }

        dependencies.store.changes.tryEmit(glanceId)

        TrendingWidget().update(
            context = context,
            id = glanceId,
        )
    }

    companion object {
        val StepKey = ActionParameters.Key<Int>("step")
    }
}
