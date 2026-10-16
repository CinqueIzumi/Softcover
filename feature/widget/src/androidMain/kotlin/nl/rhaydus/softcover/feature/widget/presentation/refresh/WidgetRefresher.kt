package nl.rhaydus.softcover.feature.widget.presentation.refresh

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
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
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveTrendingBooksUseCase
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.NO_USER_ID
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveAllQuotesUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.widget.presentation.glance.QuoteWidget
import nl.rhaydus.softcover.feature.widget.presentation.worker.QuoteRefreshScheduler
import nl.rhaydus.softcover.feature.widget.presentation.worker.TrendingRefreshScheduler
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
    private val getAllUserLists: GetAllUserListsUseCase,
    private val observeTrendingBooks: ObserveTrendingBooksUseCase,
    private val trendingRefreshScheduler: TrendingRefreshScheduler,
    private val observeAllQuotes: ObserveAllQuotesUseCase,
    private val quoteRefreshScheduler: QuoteRefreshScheduler,
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

        catchUpTrendingOnSignIn()

        catchUpQuotesOnSignIn()
    }

    fun onEnabled(widget: GlanceAppWidget) {
        placedWidgets.update { it + widget.key() }
    }

    fun onDisabled(widget: GlanceAppWidget) {
        placedWidgets.update { it - widget.key() }
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

    private fun changes(): Flow<Unit> = (appSignals() + trendingSignals()).merge()

    private fun appSignals(): List<Flow<Unit>> = listOf(
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
        signal { getCurrentlyReadingUserBooks() },
        signal { getWantToReadUserBooks() },
        signal { getAllUserLists() },
        signal { observeAllQuotes() },
    )

    /** Every period, because any of them may be the one a placed widget shows. */
    private fun trendingSignals(): List<Flow<Unit>> = TrendingPeriod.entries.map { period ->
        signal { observeTrendingBooks(period = period) }
    }

    /**
     * Builds [source] inside the flow, so a use case that throws while building its flow is caught like one that fails
     * while emitting. The first emission is the current value, which the widgets already show.
     */
    private fun signal(source: () -> Flow<*>): Flow<Unit> = flow { emitAll(source()) }
        .drop(1)
        .map { }
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
