package nl.rhaydus.softcover.feature.widget.presentation.refresh

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.domain.usecase.RefreshTrendingFeedUseCase
import nl.rhaydus.softcover.feature.widget.presentation.glance.BackgroundWidgetRenderer
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidget
import kotlin.time.Duration

/**
 * Fetches feeds on the application scope, so the fetch outlives the settings screen and does not wait on
 * WorkManager constraints or quota. The repaint follows the fetch even when it failed, because a failure writes
 * nothing for the widget to observe.
 */
internal class TrendingFeedFetcher(
    private val widget: TrendingWidget,
    private val renderer: BackgroundWidgetRenderer,
    private val applicationScope: ApplicationScope,
    private val refreshTrendingFeed: RefreshTrendingFeedUseCase,
) {
    fun fetch(
        feeds: Collection<TrendingFeed>,
        maxAge: Duration,
    ) {
        applicationScope.scope.launch {
            coroutineScope {
                feeds.map { feed ->
                    async {
                        refreshTrendingFeed(
                            feed = feed,
                            maxAge = maxAge,
                        ).onFailure { error ->
                            AppLog.e(
                                error,
                                "Fetching trending books failed",
                            )
                        }
                    }
                }.awaitAll()
            }

            runCatchingCancellable { renderer.renderAll(widget) }
                .onFailure { error ->
                    AppLog.e(
                        error,
                        "Repainting the trending widget failed",
                    )
                }
        }
    }
}
