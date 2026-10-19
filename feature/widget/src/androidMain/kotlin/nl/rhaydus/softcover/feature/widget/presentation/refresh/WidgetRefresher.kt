package nl.rhaydus.softcover.feature.widget.presentation.refresh

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
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
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.NO_USER_ID
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveAllQuotesUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.widget.domain.model.QuoteFetchTracker
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFetchTracker
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveTrendingFeedUseCase
import nl.rhaydus.softcover.feature.widget.presentation.glance.BackgroundWidgetRenderer
import nl.rhaydus.softcover.feature.widget.presentation.glance.CurrentlyReadingWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.QuoteWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.RandomPickWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidget
import nl.rhaydus.softcover.feature.widget.presentation.image.WidgetImageStore
import nl.rhaydus.softcover.feature.widget.presentation.worker.QuoteRefreshScheduler
import nl.rhaydus.softcover.feature.widget.presentation.worker.TrendingRefreshScheduler
import kotlin.time.Duration.Companion.seconds

/**
 * Repaints each owned widget when something it renders from changes. Placement is a set of widget
 * classes because `onDisabled` fires per receiver, when the last instance of that one widget goes.
 */
internal class WidgetRefresher(
    private val context: Context,
    private val renderer: BackgroundWidgetRenderer,
    private val applicationScope: ApplicationScope,
    private val widgets: List<GlanceAppWidget>,
    private val getThemeConfiguration: GetThemeConfigurationUseCase,
    private val getUserIdAsFlow: GetUserIdAsFlowUseCase,
    private val getCurrentlyReadingUserBooks: GetCurrentlyReadingUserBooksUseCase,
    private val getWantToReadUserBooks: GetWantToReadUserBooksUseCase,
    private val getAllUserLists: GetAllUserListsUseCase,
    private val observeTrendingFeed: ObserveTrendingFeedUseCase,
    private val trendingRefreshScheduler: TrendingRefreshScheduler,
    private val observeAllQuotes: ObserveAllQuotesUseCase,
    private val quoteRefreshScheduler: QuoteRefreshScheduler,
    private val imageStore: WidgetImageStore,
    private val trendingFetchTracker: TrendingFetchTracker,
    private val quoteFetchTracker: QuoteFetchTracker,
) {
    private val placedWidgets = MutableStateFlow<Set<String>>(emptySet())

    fun start() {
        applicationScope.scope.launch { seedPlacedWidgets() }

        repaintOnChange(
            targets = null,
            changes = ::sharedSignals,
        )

        repaintOnChange(
            targets = setOf(CurrentlyReadingWidget::class.java),
            changes = { listOf(signal { getCurrentlyReadingUserBooks() }) },
        )

        repaintOnChange(
            targets = setOf(RandomPickWidget::class.java),
            changes = {
                listOf(
                    signal { getWantToReadUserBooks() },
                    signal { getAllUserLists() },
                )
            },
        )

        repaintOnChange(
            targets = setOf(TrendingWidget::class.java),
            changes = ::trendingSignals,
        )

        repaintOnChange(
            targets = setOf(QuoteWidget::class.java),
            changes = { listOf(signal { observeAllQuotes() }) },
        )

        catchUpTrendingOnSignIn()

        clearImagesOnSignOut()

        catchUpQuotesOnSignIn()
    }

    fun onEnabled(widget: GlanceAppWidget) {
        placedWidgets.update { it + widget.key() }
    }

    fun onDisabled(widget: GlanceAppWidget) {
        placedWidgets.update { it - widget.key() }
    }

    /** Cached widget images belong to the signed-in user's library, so sign-out removes them. */
    private fun clearImagesOnSignOut() {
        flow { emitAll(getUserIdAsFlow()) }
            .map { it != NO_USER_ID }
            .distinctUntilChanged()
            .drop(1)
            .filter { signedIn -> signedIn.not() }
            .onEach {
                // Repaint first: a launcher reload while the files are gone would read missing images.
                refresh(widgets)

                trendingFetchTracker.clearFailures()
                quoteFetchTracker.clearFailure()

                runCatchingCancellable { imageStore.clear() }
                    .onFailure { error ->
                        AppLog.e(
                            error,
                            "Clearing widget images failed",
                        )
                    }
            }
            .catch { error ->
                AppLog.e(
                    error,
                    "Clearing widget images on sign-out failed",
                )
            }
            .launchIn(applicationScope.scope)
    }

    /** Sign-out wipes trending, so after the next sign-in the periodic schedule alone would leave other periods empty. */
    private fun catchUpTrendingOnSignIn() {
        flow { emitAll(getUserIdAsFlow()) }
            .map { it != NO_USER_ID }
            .distinctUntilChanged()
            .drop(1)
            .filter { signedIn -> signedIn }
            .onEach { trendingRefreshScheduler.requestReschedule() }
            .catch { error ->
                AppLog.e(
                    error,
                    "Trending catch-up on sign-in failed",
                )
            }
            .launchIn(applicationScope.scope)
    }

    /** Sign-out wipes the quotes, so a placed quote widget would stay empty until the next daily fetch. */
    private fun catchUpQuotesOnSignIn() {
        flow { emitAll(getUserIdAsFlow()) }
            .map { it != NO_USER_ID }
            .distinctUntilChanged()
            .drop(1)
            .filter { signedIn -> signedIn && QuoteWidget::class.java.name in placedWidgets.value }
            .onEach { quoteRefreshScheduler.refreshNow() }
            .catch { error ->
                AppLog.e(
                    error,
                    "Quote catch-up on sign-in failed",
                )
            }
            .launchIn(applicationScope.scope)
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

    /**
     * Repaints only the [targets] classes (every widget when `null`) when [changes] emits, so a write to one
     * widget's data does not open Glance sessions for the others.
     */
    private fun repaintOnChange(
        targets: Set<Class<out GlanceAppWidget>>?,
        changes: () -> List<Flow<Unit>>,
    ) {
        refreshSignals(
            placedWidgets = placedWidgets,
            changes = { changes().merge() },
            debounce = DEBOUNCE,
        )
            .onEach { refresh(widgets.filter { targets == null || it.javaClass in targets }) }
            .launchIn(applicationScope.scope)
    }

    private fun sharedSignals(): List<Flow<Unit>> = listOf(
        signal {
            getThemeConfiguration().distinctUntilChanged { old, new ->
                old.themeMode == new.themeMode &&
                    old.colorPalette == new.colorPalette &&
                    old.useDynamicColor == new.useDynamicColor
            }
        },
        signal {
            getUserIdAsFlow()
                .map { it != NO_USER_ID }
                .distinctUntilChanged()
        },
    )

    /** Every feed, because any of them may be the one a placed widget shows. */
    private fun trendingSignals(): List<Flow<Unit>> = TrendingFeed.All.map { feed ->
        signal { observeTrendingFeed(feed = feed) }
    }

    /**
     * Builds [source] inside the flow, so a use case that throws while building its flow is caught like one that fails
     * while emitting. The first emission is the current value, which the widgets already show.
     */
    private fun signal(source: () -> Flow<*>): Flow<Unit> = flow { emitAll(source()) }
        .distinctUntilChanged()
        .drop(1)
        .map { }
        .catch { error ->
            AppLog.e(
                error,
                "Widget input observation failed",
            )
        }

    private suspend fun refresh(targets: List<GlanceAppWidget>) {
        targets.forEach { widget ->
            renderer.renderAll(widget)
        }
    }

    private fun GlanceAppWidget.key(): String = javaClass.name

    private companion object {
        val DEBOUNCE = 1.seconds
    }
}
