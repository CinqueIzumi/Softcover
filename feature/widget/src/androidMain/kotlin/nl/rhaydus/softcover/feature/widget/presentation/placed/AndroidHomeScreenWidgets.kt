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
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.presentation.widget.HomeScreenWidgetKind
import nl.rhaydus.softcover.core.presentation.widget.HomeScreenWidgets
import nl.rhaydus.softcover.core.presentation.widget.PlacedHomeScreenWidget
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetRandomPickSettingsUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetTrendingSettingsUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveRandomPickChangesUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveTrendingChangesUseCase
import nl.rhaydus.softcover.feature.widget.presentation.glance.CurrentlyReadingWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.CurrentlyReadingWidgetReceiver
import nl.rhaydus.softcover.feature.widget.presentation.glance.OwnedWidgets
import nl.rhaydus.softcover.feature.widget.presentation.glance.QuoteWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.QuoteWidgetReceiver
import nl.rhaydus.softcover.feature.widget.presentation.glance.RandomPickWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.RandomPickWidgetReceiver
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidgetReceiver
import nl.rhaydus.softcover.feature.widget.presentation.mapper.label

private const val SUMMARY_SEPARATOR = " · "

internal class AndroidHomeScreenWidgets(
    context: Context,
    private val ownedWidgets: OwnedWidgets,
    private val observeRandomPickChanges: ObserveRandomPickChangesUseCase,
    private val observeTrendingChanges: ObserveTrendingChangesUseCase,
    private val getRandomPickSettings: GetRandomPickSettingsUseCase,
    private val getTrendingSettings: GetTrendingSettingsUseCase,
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
        observeRandomPickChanges().map { },
        observeTrendingChanges().map { },
        instanceChanges.changes,
        getAllUserLists()
            .map { }
            .catch { error ->
                AppLog.e(
                    error,
                    "Observing lists for placed widgets failed",
                )
            },
    )
        .onStart { emit(Unit) }
        .conflate()
        .map { queryPlaced() }
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
                HomeScreenWidgetKind.Quote -> QuoteWidgetReceiver::class.java
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
        val lists = runCatchingCancellable { getAllUserLists().firstOrNull().orEmpty() }
            .onFailure { error ->
                AppLog.e(
                    error,
                    "Reading lists for placed widgets failed",
                )
            }
            .getOrDefault(emptyList())

        return ownedWidgets.widgets
            .mapNotNull { widget -> widget.kind()?.let { kind -> kind to widget } }
            .flatMap { (kind, widget) ->
                runCatchingCancellable {
                    manager.getGlanceIds(widget.javaClass).map { glanceId -> manager.getAppWidgetId(glanceId) }
                }
                    .onFailure { error ->
                        AppLog.e(
                            error,
                            "Listing placed widgets failed",
                        )
                    }
                    .getOrDefault(emptyList())
                    .sorted()
                    .mapNotNull { id ->
                        runCatchingCancellable {
                            placed(
                                id = id,
                                kind = kind,
                                lists = lists,
                            )
                        }
                            .onFailure { error ->
                                AppLog.e(
                                    error,
                                    "Placed widget query failed",
                                )
                            }
                            .getOrNull()
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
            HomeScreenWidgetKind.CurrentlyReading,
            HomeScreenWidgetKind.Quote,
            -> null

            HomeScreenWidgetKind.RandomPick -> getRandomPickSettings(appWidgetId = id).getOrThrow().let { settings ->
                listOf(
                    settings.source.label(lists),
                    settings.cadence.label(),
                ).joinToString(SUMMARY_SEPARATOR)
            }

            HomeScreenWidgetKind.Trending -> getTrendingSettings(appWidgetId = id).getOrThrow().let { settings ->
                listOf(
                    settings.feed.label(),
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
        is QuoteWidget -> HomeScreenWidgetKind.Quote
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
