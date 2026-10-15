package nl.rhaydus.softcover.feature.widget.refresh

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetWantToReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveTrendingBooksUseCase
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.NO_USER_ID
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import kotlin.time.Duration.Companion.seconds

/**
 * Repaints every owned widget when something they render from changes. Placement is a set of widget
 * classes because `onDisabled` fires per receiver, when the last instance of that one widget goes.
 */
internal class WidgetRefresher(
    private val context: Context,
    private val applicationScope: ApplicationScope,
    private val widgets: List<GlanceAppWidget>,
    private val getThemeConfiguration: GetThemeConfigurationUseCase,
    private val getUserIdAsFlow: GetUserIdAsFlowUseCase,
    private val getCurrentlyReadingUserBooks: GetCurrentlyReadingUserBooksUseCase,
    private val getWantToReadUserBooks: GetWantToReadUserBooksUseCase,
    private val observeTrendingBooks: ObserveTrendingBooksUseCase,
) {
    private val placedWidgets = MutableStateFlow<Set<String>>(emptySet())

    fun start() {
        applicationScope.scope.launch { seedPlacedWidgets() }

        refreshSignals(
            placedWidgets = placedWidgets,
            changes = ::changes,
            debounce = DEBOUNCE,
        )
            .onEach { refreshAll() }
            .launchIn(applicationScope.scope)
    }

    fun onEnabled(widget: GlanceAppWidget) {
        placedWidgets.update { it + widget.key() }
    }

    fun onDisabled(widget: GlanceAppWidget) {
        placedWidgets.update { it - widget.key() }
    }

    private suspend fun seedPlacedWidgets() {
        runCatchingCancellable {
            val manager = GlanceAppWidgetManager(context)

            val placed = widgets
                .filter { manager.getGlanceIds(it.javaClass).isNotEmpty() }
                .map { it.key() }

            placedWidgets.update { it + placed }
        }.onFailure { error ->
            AppLog.e(
                error,
                "Seeding placed widgets failed",
            )
        }
    }

    private fun changes(): Flow<Unit> = merge(
        getThemeConfiguration()
            .distinctUntilChanged { old, new ->
                old.themeMode == new.themeMode &&
                    old.colorPalette == new.colorPalette &&
                    old.useDynamicColor == new.useDynamicColor
            }
            .asSignal(),
        getUserIdAsFlow()
            .map { it != NO_USER_ID }
            .distinctUntilChanged()
            .asSignal(),
        getCurrentlyReadingUserBooks().asSignal(),
        getWantToReadUserBooks().asSignal(),
        observeTrendingBooks().asSignal(),
    )

    private fun Flow<*>.asSignal(): Flow<Unit> = map { }
        .catch { error ->
            AppLog.e(
                error,
                "Widget input observation failed",
            )
        }

    private suspend fun refreshAll() {
        widgets.forEach { widget ->
            runCatchingCancellable { widget.updateAll(context) }
                .onFailure { error ->
                    AppLog.e(
                        error,
                        "Updating widget failed",
                    )
                }
        }
    }

    private fun GlanceAppWidget.key(): String = javaClass.name

    private companion object {
        val DEBOUNCE = 1.seconds
    }
}
