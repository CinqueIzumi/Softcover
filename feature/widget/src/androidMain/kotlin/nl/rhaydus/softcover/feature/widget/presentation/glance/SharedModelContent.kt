package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LocalAppWidgetOptions
import androidx.glance.appwidget.provideContent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

/**
 * Provides [content] from [models], collecting the cold [models] once for the session. An exact-size widget on
 * API 31+ composes [content] once per size the launcher reports, and collecting directly there would repeat the
 * database, cover and render work in every one. [models] must not depend on `LocalSize`.
 */
internal suspend fun <T> GlanceAppWidget.provideSharedContent(
    models: Flow<T>,
    initial: T,
    content: @Composable (T) -> Unit,
): Nothing = provideSharedContent(
    models = { models },
    initial = initial,
    content = content,
)

/**
 * [provideSharedContent] that also hands [models] the widget's launcher options, which change when the user
 * resizes it. Glance does not re-run `provideGlance` on a resize, only recomposes with new `LocalAppWidgetOptions`.
 * The empty bundle Glance provides before it has fetched the real options is not forwarded.
 */
internal suspend fun <T> GlanceAppWidget.provideSharedContent(
    models: (options: Flow<Bundle>) -> Flow<T>,
    initial: T,
    content: @Composable (T) -> Unit,
): Nothing = coroutineScope {
    val options = MutableStateFlow<Bundle?>(null)

    val shared = models(options.filterNotNull()).stateIn(
        scope = this,
        started = SharingStarted.Eagerly,
        initialValue = initial,
    )

    provideContent {
        val model by shared.collectAsState()
        val launcherOptions = LocalAppWidgetOptions.current

        LaunchedEffect(launcherOptions) {
            if (launcherOptions.isEmpty.not()) options.value = launcherOptions
        }

        content(model)
    }
}
