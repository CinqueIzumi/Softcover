package nl.rhaydus.softcover.feature.widget.presentation.placed

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.presentation.widget.HomeScreenWidgetKind
import nl.rhaydus.softcover.core.presentation.widget.HomeScreenWidgets
import nl.rhaydus.softcover.core.presentation.widget.PlacedHomeScreenWidget
import nl.rhaydus.softcover.feature.widget.data.datastore.RandomPickStore
import nl.rhaydus.softcover.feature.widget.data.datastore.TrendingStore
import nl.rhaydus.softcover.feature.widget.presentation.glance.CurrentlyReadingWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.CurrentlyReadingWidgetReceiver
import nl.rhaydus.softcover.feature.widget.presentation.glance.OwnedWidgets
import nl.rhaydus.softcover.feature.widget.presentation.glance.RandomPickWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.RandomPickWidgetReceiver
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidgetReceiver
import nl.rhaydus.softcover.feature.widget.presentation.mapper.label

private const val SUMMARY_SEPARATOR = " · "

internal class AndroidHomeScreenWidgets(
    context: Context,
    private val ownedWidgets: OwnedWidgets,
    private val randomPickStore: RandomPickStore,
    private val trendingStore: TrendingStore,
    private val getAllUserLists: GetAllUserListsUseCase,
    private val instanceChanges: WidgetInstanceChanges,
    private val appDispatchers: AppDispatchers,
) : HomeScreenWidgets {
    private val context: Context = context.applicationContext

    override val isSupported: Boolean = true

    override fun canPin(): Boolean = guarded(fallback = false) {
        AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported
    }

    override fun observePlaced(): Flow<List<PlacedHomeScreenWidget>> = merge(
        randomPickStore.changes.map { },
        trendingStore.changes.map { },
        instanceChanges.changes,
        getAllUserLists().map { },
    )
        .onStart { emit(Unit) }
        .conflate()
        .map { queryPlaced() }
        .catch { error ->
            AppLog.e(
                error,
                "Placed widget observation failed",
            )

            emit(emptyList())
        }
        .distinctUntilChanged()
        .flowOn(appDispatchers.io)

    override fun openSettings(widgetId: Int) {
        guarded(fallback = Unit) {
            val configure = AppWidgetManager.getInstance(context).getAppWidgetInfo(widgetId)?.configure
                ?: return@guarded

            context.startActivity(
                Intent()
                    .setComponent(configure)
                    .putExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        widgetId,
                    )
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    override fun requestPin(kind: HomeScreenWidgetKind) {
        guarded(fallback = Unit) {
            val receiver = when (kind) {
                HomeScreenWidgetKind.CurrentlyReading -> CurrentlyReadingWidgetReceiver::class.java
                HomeScreenWidgetKind.RandomPick -> RandomPickWidgetReceiver::class.java
                HomeScreenWidgetKind.Trending -> TrendingWidgetReceiver::class.java
            }

            AppWidgetManager.getInstance(context).requestPinAppWidget(
                ComponentName(
                    context,
                    receiver,
                ),
                null,
                null,
            )
        }
    }

    private suspend fun queryPlaced(): List<PlacedHomeScreenWidget> {
        val manager = GlanceAppWidgetManager(context)
        val lists = getAllUserLists().firstOrNull().orEmpty()

        return ownedWidgets.widgets
            .mapNotNull { widget -> widget.kind()?.let { kind -> kind to widget } }
            .flatMap { (kind, widget) ->
                manager.getGlanceIds(widget.javaClass)
                    .map { glanceId -> manager.getAppWidgetId(glanceId) }
                    .sorted()
                    .map { id ->
                        placed(
                            id = id,
                            kind = kind,
                            lists = lists,
                        )
                    }
            }
            .sortedBy { it.kind.ordinal }
    }

    private suspend fun placed(
        id: Int,
        kind: HomeScreenWidgetKind,
        lists: List<BookList>,
    ): PlacedHomeScreenWidget {
        val summary = when (kind) {
            HomeScreenWidgetKind.CurrentlyReading -> null

            HomeScreenWidgetKind.RandomPick -> randomPickStore.settings(id).let { settings ->
                listOf(
                    settings.source.label(lists),
                    settings.cadence.label(),
                ).joinToString(SUMMARY_SEPARATOR)
            }

            HomeScreenWidgetKind.Trending -> trendingStore.settings(id).let { settings ->
                listOf(
                    settings.period.label(),
                    settings.cadence.label(),
                ).joinToString(SUMMARY_SEPARATOR)
            }
        }

        return PlacedHomeScreenWidget(
            id = id,
            kind = kind,
            summary = summary,
            hasSettings = summary != null,
        )
    }

    private fun GlanceAppWidget.kind(): HomeScreenWidgetKind? = when (this) {
        is CurrentlyReadingWidget -> HomeScreenWidgetKind.CurrentlyReading
        is RandomPickWidget -> HomeScreenWidgetKind.RandomPick
        is TrendingWidget -> HomeScreenWidgetKind.Trending
        else -> null
    }

    /** Glance and AppWidgetManager throw unchecked on a stale id or a launcher that refuses; Settings must survive either. */
    @Suppress("TooGenericExceptionCaught")
    private inline fun <T> guarded(
        fallback: T,
        block: () -> T,
    ): T = try {
        block()
    } catch (error: RuntimeException) {
        AppLog.e(
            error,
            "Home screen widget call failed",
        )

        fallback
    }
}
