package nl.rhaydus.softcover.feature.widget.presentation.callback

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import nl.rhaydus.softcover.feature.widget.presentation.glance.QuoteWidgetDependencies
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/** Refetches the quotes. The fetch marks them running at once, which swaps the failure for the loader. */
internal class QuoteRetryCallback : ActionCallback, KoinComponent {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        get<QuoteWidgetDependencies>().fetcher.fetch()
    }
}
