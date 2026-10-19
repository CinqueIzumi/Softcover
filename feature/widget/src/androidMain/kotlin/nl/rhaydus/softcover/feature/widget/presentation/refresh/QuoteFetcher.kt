package nl.rhaydus.softcover.feature.widget.presentation.refresh

import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.personal.domain.usecase.RefreshAllQuotesUseCase
import nl.rhaydus.softcover.feature.widget.domain.model.QuoteFetchTracker
import nl.rhaydus.softcover.feature.widget.presentation.glance.BackgroundWidgetRenderer
import nl.rhaydus.softcover.feature.widget.presentation.glance.QuoteWidget

/**
 * Fetches quotes on the application scope, so a retry does not wait on WorkManager constraints or quota. The repaint
 * follows the fetch even when it failed, because a failure writes nothing for the widget to observe.
 */
internal class QuoteFetcher(
    private val widget: QuoteWidget,
    private val renderer: BackgroundWidgetRenderer,
    private val applicationScope: ApplicationScope,
    private val refreshAllQuotes: RefreshAllQuotesUseCase,
    private val fetchTracker: QuoteFetchTracker,
) {
    fun fetch() {
        applicationScope.scope.launch {
            fetchTracker.track { refreshAllQuotes() }
                .onFailure { error ->
                    AppLog.e(
                        error,
                        "Fetching quotes failed",
                    )
                }

            runCatchingCancellable { renderer.renderAll(widget) }
                .onFailure { error ->
                    AppLog.e(
                        error,
                        "Repainting the quote widget failed",
                    )
                }
        }
    }
}
