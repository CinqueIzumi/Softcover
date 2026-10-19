package nl.rhaydus.softcover.feature.widget.presentation.glance

import androidx.glance.appwidget.GlanceAppWidget
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import kotlin.time.Duration.Companion.seconds

/**
 * Repaints placed widgets without a Glance session. Glance runs every session as its own JobScheduler job, and
 * background repaints through sessions use up the app's job quota, after which no background update runs.
 */
internal class BackgroundWidgetRenderer(
    private val host: WidgetHost,
    private val liveSessions: LiveWidgetSessions,
    private val sources: Map<Class<out GlanceAppWidget>, WidgetModelSource<*>>,
) {
    private val inFlight = mutableMapOf<Int, Job>()

    private val inFlightLock = Mutex()

    suspend fun renderAll(widget: GlanceAppWidget) {
        val appWidgetIds = runCatchingCancellable {
            host.placedAppWidgetIds(widget)
        }.onFailure { error ->
            AppLog.e(
                error,
                "Listing placed widgets failed",
            )
        }.getOrDefault(emptyList())

        coroutineScope {
            appWidgetIds.map { appWidgetId ->
                async {
                    render(
                        widget = widget,
                        appWidgetId = appWidgetId,
                    )
                }
            }.awaitAll()
        }
    }

    suspend fun render(
        widget: GlanceAppWidget,
        appWidgetId: Int,
    ) {
        val source = sources[widget.javaClass]

        if (source == null) {
            AppLog.e(
                IllegalStateException("No model source for ${widget.javaClass.name}"),
                "Rendering widget in the background failed",
            )

            return
        }

        coroutineScope {
            val job = inFlightLock.withLock {
                val previous = inFlight[appWidgetId]

                previous?.cancel()

                launch {
                    previous?.join()

                    renderInstance(
                        widget = widget,
                        source = source,
                        appWidgetId = appWidgetId,
                    )
                }.also { inFlight[appWidgetId] = it }
            }

            try {
                job.join()
            } finally {
                withContext(NonCancellable) {
                    inFlightLock.withLock {
                        if (inFlight[appWidgetId] === job) inFlight.remove(appWidgetId)
                    }
                }
            }
        }
    }

    private suspend fun <T> renderInstance(
        widget: GlanceAppWidget,
        source: WidgetModelSource<T>,
        appWidgetId: Int,
    ) {
        if (liveSessions.isLive(appWidgetId)) return

        runCatchingCancellable {
            val options = host.options(appWidgetId)

            withTimeoutOrNull(RENDER_TIMEOUT) {
                source.models(
                    appWidgetId = appWidgetId,
                    options = flowOf(options),
                )
                    .renderStages(source)
                    .collect { model ->
                        host.push(
                            widget = widget,
                            source = source,
                            appWidgetId = appWidgetId,
                            model = model,
                            options = options,
                        )
                    }
            }
        }.onFailure { error ->
            AppLog.e(
                error,
                "Rendering widget in the background failed",
            )
        }
    }

    private companion object {
        val RENDER_TIMEOUT = 10.seconds
    }
}
