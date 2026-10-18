package nl.rhaydus.softcover.feature.widget.presentation.glance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

/**
 * Provides [content] from [models], collecting the cold [models] once for the session. A responsive widget
 * composes [content] once per declared size, and collecting directly there would repeat the database, cover and
 * render work in every one. [models] must not depend on `LocalSize`.
 */
internal suspend fun <T> GlanceAppWidget.provideSharedContent(
    models: Flow<T>,
    initial: T,
    content: @Composable (T) -> Unit,
): Nothing = coroutineScope {
    val shared = models.stateIn(
        scope = this,
        started = SharingStarted.Eagerly,
        initialValue = initial,
    )

    provideContent {
        val model by shared.collectAsState()

        content(model)
    }
}
